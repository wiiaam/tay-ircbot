package coremodules

import irc.config.Configs
import irc.message.Message
import irc.server.{ConnectionManager, ServerResponder}
import ircbot.{BotCommand, BotModule}

import scala.collection.mutable




class Ignore extends BotModule{

  private val ignoreTime = 30 * 1000
  private val checkInterval = 7 * 1000
  private val maxChecks = 5
  private val checks = mutable.Map[String, Int]()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(b.command == "ignore" && m.sender.isAdmin){
      if(b.hasParams){
        ConnectionManager.servers(m.server).ignores = ConnectionManager.servers(m.server).ignores :+ b.paramsArray(0)
      }

    }

    if(m.trailing.startsWith(b.commandPrefix)){
      addCheck(m.sender.nickname, m.server)
    }
  }

  private def addCheck(nick: String, server: String): Unit ={
    new Thread(new Runnable {
      override def run(): Unit = {
        if(checks.keySet.contains(nick)){
          checks += nick -> (checks(nick) + 1)
        } else {
          checks += nick -> 1
        }

        if(checks(nick) == maxChecks){
          println("ignored user")
          ConnectionManager.servers(server).ignores = ConnectionManager.servers(server).ignores :+ nick
          Thread.sleep(ignoreTime)
          ConnectionManager.servers(server).ignores = ConnectionManager.servers(server).ignores.filter(_ != nick)
          checks += nick -> 0
          println("unignored user")
        } else {
          Thread.sleep(checkInterval)
          checks += nick -> (checks(nick) - 1)
        }
      }
    }).start()
  }
}
