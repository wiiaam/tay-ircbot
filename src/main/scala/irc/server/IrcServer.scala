package irc.server

import java.io.PrintStream
import java.net.Socket
import java.security.SecureRandom
import java.util
import java.util.Scanner
import irc.config.Configs
import irc.listeners.OnMessageListener
import irc.message.{MessageCommands, Message}
import ircbot.BotCommand
import out.Out
import javax.net.ssl._
import java.security.cert.X509Certificate
import java.security.cert.CertificateException


class IrcServer(name: String, address: String, port: Int, useSSL: Boolean) {
  private var listeners: Map[String, OnMessageListener] = Map()
  private var socket: Option[Socket] = None
  private var in: Option[Scanner] = None
  private var out: Option[PrintStream] = None
  private var toSend = new util.ArrayDeque[String]
  private var toSendLP = new util.ArrayDeque[String]
  private var connected = false
  var serverName = name
  val fileName = name

  sendQueueToSocket()

  def addListener(name: String, onMessageListener: OnMessageListener): Unit = {
    listeners += (name -> onMessageListener)
  }

  private def onMessageReceived(message: String) = {

    val m = new Message(message, fileName)

    val b = new BotCommand(m, Configs.get(fileName).get.getCommandPrefix)
    val r = new ServerResponder(this)
    for ((k, v) <- listeners) {
      Out.println(s"$fileName/$serverName --> $message")
      v.onMessage(m, b, r)
    }
  }

  def connect(): Boolean = {
    try {
      if (Configs.get(serverName).get.useSSL) {
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
        false
    }
  }

  def login(): Unit = {
    val config = Configs.get(fileName).getOrElse(throw new RuntimeException("No config"))
    send("NICK " + config.getNickname)
    send("USER " + config.getUsername + " " + config.getUsername + " " + config.getServer + " :" + config.getRealname)
    if(config.getServerPassword != ""){
      send("PASS " + config.getServerPassword)
    }
    var loggedIn = false
    while (!loggedIn) {
      if (in.getOrElse(throw new RuntimeException("Not connected")).hasNextLine) {
        val next = in.get.nextLine()

        val message = new Message(next, fileName)


        Out.println(s"$fileName/$serverName --> $message")


        message.command match {
          case MessageCommands.CONNECTED =>
            Out.println(s"Connected to $fileName")
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
              loggedIn = true
            }
          case _ =>
        }
      }
    }
  }

  def listenOnSocket(): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        while (connected) {
          in.foreach(stream => {
            if (stream.hasNextLine) {
              onMessageReceived(stream.nextLine())
            }

          })
          Thread.sleep(10)
        }
      }
    }).start()
  }

  private def sendQueueToSocket(): Unit = {
    var spammed = 0
    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          Thread.sleep(20)
          if (!toSend.isEmpty) {
            val tosend = toSend.poll()
            Out.println(s"$fileName/$serverName <-- $tosend")
            out.foreach(_.print(tosend + "\r\n"))
            out.foreach(_.flush())
            if (spammed > 4) Thread.sleep(500)
            else spammed += 1
          }
          else if (!toSendLP.isEmpty) {
            out.foreach(_.print(toSendLP.poll() + "\r\n"))
            out.foreach(_.flush())
            if (spammed > 4) Thread.sleep(500)
            else spammed += 1
          }
          else if (spammed != 0) spammed = 0
        }
      }
    }).start()
  }

  def send(message: String) {
    send(message, Priorities.STANDARD_PRIORITY)
  }

  def send(message: String, priority: Priorities.Value) {
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
    out = None
    in = None
    socket.get.close()
    socket = None
  }

}
