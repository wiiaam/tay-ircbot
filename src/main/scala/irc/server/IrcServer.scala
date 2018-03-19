package irc.server

import java.io.PrintStream
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util
import java.util.Scanner
import javax.net.ssl._

import irc.config.Configs
import irc.listeners.OnMessageListener
import irc.message.{Message, MessageCommands}
import ircbot.BotCommand
import out.Out


class IrcServer(name: String, address: String, port: Int, useSSL: Boolean) {
  private var listeners: Map[String, OnMessageListener] = Map()
  private var socket: Option[Socket] = None
  private var in: Option[Scanner] = None
  private var out: Option[PrintStream] = None
  private var toSend = new util.ArrayDeque[String]
  private var toSendLP = new util.ArrayDeque[String]
  private var connected = false
  private var sendThread: Thread = _
  private var inThread: Thread = _
  private var inQueue = new util.ArrayDeque[Message]
  var serverName = name
  val fileName = name

  def getIrcServer: IrcServer = this

  startSendQueueThread()
  startInQueueThread()

  def addListener(name: String, onMessageListener: OnMessageListener): Unit = {
    listeners += (name -> onMessageListener)
  }

  private def onMessageReceived(message: String) = {
    val m = new Message(message, fileName)
    if(m.command == MessageCommands.PING){
      inQueue.addFirst(m)
    }
    else{
      inQueue.add(m)
    }

  }

  def connect(): Boolean = {
    try {
      if (Configs.get(fileName).get.useSSL) {
        val tm = new X509TrustManager {
          override def getAcceptedIssuers: Array[X509Certificate] = null

          override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

          override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
        }
        val tmarray: Array[TrustManager] = Array(tm)
        val context = SSLContext.getInstance("SSL")
        context.init(new Array[KeyManager](0), tmarray, new SecureRandom())
        val sslfact: SSLSocketFactory = context.getSocketFactory
        socket = Some(sslfact.createSocket(address, port).asInstanceOf[SSLSocket])
      }
      else socket = Some(new Socket(address, port))
      in = Some(new Scanner(socket.get.getInputStream))
      out = Some(new PrintStream(socket.get.getOutputStream))
      connected = true
      true
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def login(): Boolean = {
    val config = Configs.get(fileName).getOrElse(throw new RuntimeException("No config"))
    send("NICK " + config.getNickname)
    send("USER " + config.getUsername + " " + config.getUsername + " " + config.getServer + " :" + config.getRealname)

    if(config.getServerPassword != ""){
      send("PASS " + config.getServerPassword)
    }
    var loggedIn = false
    while (!loggedIn) {
      if (in.getOrElse(return false).hasNextLine) {


        val next = in.get.nextLine()
        Out.println(s"$fileName/$serverName --> $next")

        val message = new Message(next, fileName)



        if(message.toString.toLowerCase.contains("error") || message.toString.toLowerCase.contains("closing link")){
          return false
        }

        message.command match {
          case MessageCommands.PING =>
            send("PONG :" + message.trailing)
          case MessageCommands.CONNECTED =>
            Out.println(s"$fileName/$serverName !!! Connected")
            loggedIn = true
          case MessageCommands.NICKINUSE =>
            val nick = config.getNickname
            send("NICK " + nick + "_")
            if (config.useNickServ && config.ghostExisting) {
              send("NICK " + nick + "_")
              Thread.sleep(2000)
              send("PRIVMSG NickServ :GHOST " + nick + " " + config.getPassword)
              Thread.sleep(2000)
              send("NICK " + nick)
            }
          case _ =>
        }
      }
      Thread.sleep(50)
    }
    true
  }

  def listenOnSocket(): Unit = {
    val thread = new Thread(new Runnable {
      override def run(): Unit = {
        while (connected) {
          in.foreach(stream => {
            if (stream.hasNextLine) {
              onMessageReceived(stream.nextLine())
            }

          })
          Thread.sleep(5)
        }
      }
    })
    thread.setName(s"Listening on $serverName")
    thread.start()
  }

  private def startSendQueueThread(): Unit = {
    var spammed = 0
    sendThread = new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          Thread.sleep(20)
          if (!toSend.isEmpty) {
            val tosend = toSend.poll()
            Out.println(s"$fileName/$serverName <-- $tosend")
            out.foreach(_.print(tosend + "\r\n"))
            if (spammed > 4) Thread.sleep(500)
            else spammed += 1
          }
          else if (!toSendLP.isEmpty) {
            out.foreach(_.print(toSendLP.poll() + "\r\n"))
            if (spammed > 4) Thread.sleep(500)
            else spammed += 1
          }
          else if (spammed != 0) spammed = 0
        }
      }
    })
    sendThread.setName(s"Send queue $serverName")
    sendThread.start()
  }

  private def startInQueueThread(): Unit = {
    inThread = new Thread(new Runnable {
      override def run(): Unit = {
        while (true){
          Thread.sleep(20)
          if(!inQueue.isEmpty) {
            val newMessage: Message = inQueue.poll()


            val b = new BotCommand(newMessage, Configs.get(fileName).get.getCommandPrefix)
            val r = new ServerResponder(getIrcServer, newMessage.params.first)
            Out.println(s"$fileName/$serverName --> ${newMessage.toString}")
            for ((k, v) <- listeners) {
              v.onMessage(newMessage, b, r)
            }
          }
        }
      }
    })
    inThread.setName(s"Read queue $serverName")
    inThread.start()
  }


  def send(message: String, priority: Priorities.Value = Priorities.STANDARD_PRIORITY) {
    val msg = message.replaceAll("\r", "").replaceAll("\n", "")
    if ((message.startsWith("PRIVMSG") || message.startsWith("NOTICE")) && message.length > 320) {
      val split = msg.split(" ")
      var tosend = ""
      var i = 0
      while (i < split.length) {
        tosend += split(i) + " "
        if (tosend.length() > 300) {
          priority match {
            case Priorities.HIGH_PRIORITY => toSend.addFirst(tosend.substring(0, tosend.length() - 1))
            case Priorities.LOW_PRIORITY => toSendLP.add(tosend.substring(0, tosend.length() - 1))
            case Priorities.STANDARD_PRIORITY => toSend.add(tosend.substring(0, tosend.length() - 1))

          }
          var next = split(0) + " " + split(1) + " :"
          for (j <- i + 1 until split.length) {
            next += split(j) + " "
          }
          send(next.substring(0, next.length() - 1), priority)
          i = split.length
        }
        i = i + 1
      }
    }
    else {
      priority match {
        case Priorities.HIGH_PRIORITY => toSend.addFirst(msg)
        case Priorities.LOW_PRIORITY => toSendLP.add(msg)
        case Priorities.STANDARD_PRIORITY => toSend.add(msg)

      }
    }
  }

  def disconnect(): Unit = {
    connected = false
    out.foreach(_.print("QUIT :No response from server\r\n"))
    out.foreach(_.flush())
    out.foreach(_.close())
    out = None
    in.foreach(_.close())
    in = None
    socket.get.close()
    socket = None
  }

  def clearListener(listener: String) ={
    listeners = listeners.filterKeys(_ == listener)
  }

}
