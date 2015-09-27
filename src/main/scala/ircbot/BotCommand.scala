package ircbot

import java.util

import irc.config.Configs
import irc.message.Message
import out.Out


class BotCommand(m: Message, prefix: String) {
  private var isBotCommandHolder = false
  private var commandHolder = ""
  private var paramsArrayHolder: Array[String] = Array()
  private var paramsHolder = ""

  if(prefix == Configs.get(m.server).get.getNickname + ": "){

  }
  else{
    isBotCommandHolder = m.trailing.startsWith(prefix)
    if(isBotCommandHolder) {
      var split = m.trailing.substring(prefix.length).split("\\s+")
      commandHolder = split(0)
      val list = new util.ArrayList[String]()
      for(i <- 1 until split.length){
        list.add(split(i))
      }
      paramsArrayHolder = list.toArray(new Array[String](0))
    }
  }

  val isBotCommand = isBotCommandHolder
  val hasParams = paramsArrayHolder.length > 0
  val command = commandHolder
  val paramsArray = paramsArrayHolder
  val paramsString = paramsHolder
}
