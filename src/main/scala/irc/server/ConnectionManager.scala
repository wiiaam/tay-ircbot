package irc.server

import java.util

import irc.config.Configs
import irc.listeners.OnMessageListener
import irc.message.Message
import ircbot.{Modules, BotCommand}
import out.Out
import scala.collection.JavaConversions._


object ConnectionManager {

  private val servers = new util.HashMap[String, IrcServer]()

  private val PING_TIMEOUT = 30

  var pings: Map[String, Boolean] = Map()

  def start(): Unit ={
    Modules.loadAll()
    Configs.load()
    Out.println("Configs loaded")
    var servernames = ""
    for((k,v) <- Configs.configs){
      servernames += k + " "
      servers.put(k,IrcServerCreator.create(k, v.getServer, v.getPort, v.useSSL))
    }
    Out.println(s"Found servers: $servernames")

    for((k,v) <- servers){
      new Thread(new Runnable {
        override def run(): Unit = {
          connectToServer(k)
        }
      }).start()

    }
  }

  def connectToServer(name: String){
    if(!servers.containsKey(name)){
      Out.println(s"Cannot connect to server $name (server not loaded)")
    }
    val server: IrcServer = servers.get(name)
    var connected = false
    while(!connected){
      connected = server.connect()
      if(!connected){
        Out.println(s"$name !!! Could not connect, retrying in 10 seconds")
        Thread.sleep(10000)
      }
    }
    server.login()
    Out.println(s"Logged in to $name")
    server.addListener("main", new OnMessageListener {
      override def onMessage(m: Message, b: BotCommand, r: ServerResponder): Unit =
      Modules.parseToAllModules(m,b,r)
    })
    new Thread(new Runnable {
      override def run(): Unit = {
        server.listenOnSocket()
      }
    }).start()
    if(Configs.get(name).get.useNickServ) Thread.sleep(1000)
    joinChannels(name)
    checkPing(name)
  }

  def joinChannels(name: String): Unit ={
    new Thread(new Runnable {
      override def run(): Unit = {
        val server: IrcServer = servers.get(name)
        val config = Configs.get(name).get
        for(channel <- config.getChannels){
          server.send("JOIN " + channel)
        }
      }
    }).start()
  }

  def checkPing(name: String): Unit ={
    Thread.sleep(5000)
    var connected = true
    while(connected){
      servers.get(name).send("PING :" + (System.currentTimeMillis()/1000).asInstanceOf[Int], Priorities.HIGH_PRIORITY)
      pings += (name -> false)
      Thread.sleep(PING_TIMEOUT*1000)
      if(!pings(name)){
        Out.println(servers.get(name).name + " !!! Ping timeout")
        servers.get(name).disconnect()
        connectToServer(name)
        connected = false
      }
    }
  }
}
