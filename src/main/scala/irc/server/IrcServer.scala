package irc.server

import java.io.PrintStream
import java.net.Socket
import java.util
import java.util.Scanner
import irc.listeners.OnMessageListener
import irc.message.Message


class IrcServer(val name: String, address: String, port: Int, useSSL: Boolean) {
  private var listeners = new util.ArrayList[OnMessageListener]
  private var socket: Option[Socket] = None
  private var in: Option[Scanner] = None
  private var out: Option[PrintStream] = None
  private var toSend = new util.ArrayDeque[String]
  private var toSendLP = new util.ArrayDeque[String]

  def addListener(onMessageListener: OnMessageListener): Unit ={
    listeners.add(onMessageListener)
  }

  private def onMessageReceived(message: String) = {
    for(i <- 0 until listeners.size()){
      listeners.get(i).onMessage(new Message(message, name))
    }
  }

  def connect(): Unit ={
    socket = Some(new Socket(address, port))
    in = Some(new Scanner(socket.get.getInputStream))
    out = Some(new PrintStream(socket.get.getOutputStream))
  }

  private def listenOnSocket(): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        while(true){
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
          if(!toSend.isEmpty){
            // TODO make sure that anything sent is printed to console
            out.get.print(toSend.poll() + "\r\n")
          }
          else if(!toSendLP.isEmpty){
            out.get.print(toSendLP.poll() + "\r\n")
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

}
