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


class IrcServer(val name: String, address: String, port: Int, useSSL: Boolean) {
  private var listeners: Map[String, OnMessageListener] = Map()
  private var socket: Option[Socket] = None
  private var in: Option[Scanner] = None
  private var out: Option[PrintStream] = None
  private var toSend = new util.ArrayDeque[String]
  private var toSendLP = new util.ArrayDeque[String]
  private var connected = false

  sendQueueToSocket()

  def addListener(name: String, onMessageListener: OnMessageListener): Unit ={
    listeners += (name -> onMessageListener)
  }

  private def onMessageReceived(message: String) = {
    for((k,v) <- listeners){
      Out.println(s"$name --> $message")
      val m = new Message(message, name)
      Out.println(s"sender is registered: ${m.sender.isRegistered}, sender is admin: ${m.sender.isAdmin}")
      v.onMessage(m,new BotCommand(m, Configs.get(name).get.getCommandPrefix), new ServerResponder(this))
    }
  }

  def connect(): Unit ={
    if(Configs.get(name).get.useSSL) {
      val tm = new X509TrustManager {
        override def getAcceptedIssuers: Array[X509Certificate] = null

        override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

        override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
      }
      val tmarray: Array[TrustManager] = Array(tm)
      val context = SSLContext.getInstance ("SSL")
      context.init( new Array[KeyManager](0), tmarray, new SecureRandom( ) )
      val sslfact: SSLSocketFactory = context.getSocketFactory
      socket = Some(sslfact.createSocket(address,port).asInstanceOf[SSLSocket])
    }
    else socket = Some(new Socket(address, port))
    in = Some(new Scanner(socket.get.getInputStream))
    out = Some(new PrintStream(socket.get.getOutputStream))
    connected = true
  }

  def login(): Unit = {
    val config = Configs.get(name).getOrElse(throw new RuntimeException("No config"))
    send("NICK " + config.getNickname)
    send("USER " + config.getUsername + " " + config.getUsername + " " + config.getServer + " :" + config.getRealname)
    var loggedIn = false
    while(!loggedIn){
      if(in.getOrElse(throw new RuntimeException("Not connected")).hasNextLine){
        val next = in.get.nextLine()

        val message = new Message(next, name)


        Out.println(String.format("%s --> %s %s", name, message.command.toString, message.trailing))


        message.command match {
          case MessageCommands.CONNECTED =>
            Out.println(s"Connected to $name")
            if(config.useNickServ){
              send("PRIVMSG NickServ :IDENTIFY " + config.getPassword)
            }
            loggedIn = true
          case MessageCommands.NICKINUSE =>
            val nick = config.getNickname
            send("NICK " + nick + "_")
            config.setNickname(nick + "_")
            if(config.useNickServ && config.ghostExisting){
              send("NICK " + nick + "_")
              Thread.sleep(2000)
              send("PRIVMSG NickServ :GHOST " + nick + " " + config.getPassword)
              Thread.sleep(2000)
              send("NICK " + nick)
              send("PRIVMSG NickServ :IDENTIFY " + config.getPassword)
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
        while(connected){
          if(in.get.hasNextLine){
            onMessageReceived(in.get.nextLine())
          }
          Thread.sleep(10)
        }
      }
    }).start()
  }

  private def sendQueueToSocket(): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        while(true){
          Thread.sleep(50)
          if(!toSend.isEmpty){
            val tosend = toSend.poll()
            Out.println(s"$name <-- $tosend")
            out.get.print(tosend + "\r\n")
          }
          else if(!toSendLP.isEmpty){
            out.get.print(toSendLP.poll() + "\r\n")
            out.get.flush()
          }
        }
      }
    }).start()
  }

  def send(message: String){
    send(message, Priorities.STANDARD_PRIORITY)
  }

  def send(message: String, priority: Priorities.Value){
    val msg = message.replaceAll("\r", "").replaceAll("\n", "")
    if((message.startsWith("PRIVMSG") || message.startsWith("NOTICE")) && message.length > 320){
      val split = msg.split(" ")
      var tosend = ""
      var hitLimit = false
      var i = 0
      while(i < split.length){
        tosend += split(i) + " "
        if(tosend.length() > 300){
          priority match {
            case Priorities.HIGH_PRIORITY => toSend.addFirst(tosend.substring(0, tosend.length() - 1))
            case Priorities.LOW_PRIORITY => toSendLP.add(tosend.substring(0, tosend.length() - 1))
            case Priorities.STANDARD_PRIORITY => toSend.add(tosend.substring(0, tosend.length() - 1))

          }
          hitLimit = true
          var next = split(0) + " " + split(1) + " :"
          for( j <- i+1 until split.length){
            next += split(j) + " "
          }
          send(next.substring(0, next.length()-1), priority)
          i = split.length
        }
        i = i + 1
      }
      /* TODO make sure this works
      if(!hitLimit){
        priority match {
          case Priorities.HIGH_PRIORITY => toSend.addFirst(tosend.substring(0, tosend.length() - 1))
          case Priorities.LOW_PRIORITY => toSendLP.add(tosend.substring(0, tosend.length() - 1))
          case Priorities.STANDARD_PRIORITY => toSend.add(tosend.substring(0, tosend.length() - 1))
        }
      }*/
    }
    else {
      priority match {
        case Priorities.HIGH_PRIORITY => toSend.addFirst(msg)
        case Priorities.LOW_PRIORITY => toSendLP.add(msg)
        case Priorities.STANDARD_PRIORITY => toSend.add(msg)

      }
    }
  }

  def disconnect(): Unit ={
    connected = false
    out.get.print("QUIT :No response from server\r\n")
    out.get.flush()
    out = None
    in = None
    socket.get.close()
    socket = None
  }

}
