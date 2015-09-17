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

  def start(): Unit ={
    Modules.loadAll()
    Configs.load()
    Out.println("Configs loaded")
    var servernames = ""
    for((k,v) <- Configs.configs){
      servernames += k + " "
      servers.put(k,IrcServerCreator.create(k,v.getServer,v.getPort,v.useSSL))
    }
    Out.println(s"Found servers: $servernames")

    for((k,v) <- servers){
      connectToServer(k)
    }
  }

  def connectToServer(name: String){
    val server: IrcServer = servers.get(name)
    server.connect()
    server.login()
    Out.println("Logged in")
    server.addListener(new OnMessageListener {
      override def onMessage(m: Message, b: BotCommand, r: ServerResponder): Unit =
      Modules.parseToAllModules(m,b,r)
    })
    new Thread(new Runnable {
      override def run(): Unit = {
        server.listenOnSocket()
      }
    }).start()
    joinChannels(name)
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
}
