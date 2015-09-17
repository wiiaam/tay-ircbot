package ircbot

import java.util

import coremodules.{Help, IBIP, CTCP, Ping}
import irc.message.Message
import irc.server.ServerResponder


object Modules {

  var modules = new util.ArrayList[Module]()
  var coreModules = new util.ArrayList[Module]()

  def parseToAllModules(m: Message, b: BotCommand, r: ServerResponder): Unit ={
    new Thread(new Runnable {
      override def run(): Unit = {
        for(i <- 0 until modules.size()){
          new Thread(new Runnable {
            override def run(): Unit = {
              modules.get(i).parse(m,b,r)
            }
          }).start()
        }
      }
    }).start()
    new Thread(new Runnable {
      override def run(): Unit = {
        for(i <- 0 until coreModules.size()){
          new Thread(new Runnable {
            override def run(): Unit = {
              coreModules.get(i).parse(m,b,r)
            }
          }).start()
        }
      }
    }).start()
  }

  def loadAll(): Unit ={
    modules.add(new Ping)
    modules.add(new CTCP)
    modules.add(new IBIP)
    modules.add(new Help)
  }
}
