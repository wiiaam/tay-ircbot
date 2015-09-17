package ircbot

import java.util

import irc.message.Message


class BotCommand(m: Message, prefix: String) {
  private var isBotCommandHolder = false
  private var hasParamsHolder = false
  private var commandHolder = ""
  private var paramsArrayHolder: Array[String] = Array()
  private var paramsHolder = ""

  if(prefix == "self"){

  }
  else{
    isBotCommandHolder = m.trailing.startsWith(prefix)
    if(isBotCommandHolder) {
      var split = m.trailing.substring(prefix.length).split("\\s+")
      val commandHolder = split(0)
      val list = new util.ArrayList[String]()
      for(i <- 1 until split.length){
        list.add(split(i))
      }
      paramsArrayHolder = list.toArray(new Array[String](0))
    }
  }

  val isBotCommand = isBotCommandHolder
  val hasParams = hasParamsHolder
  val command = commandHolder
  val paramsArray = paramsArrayHolder
  val paramsString = paramsHolder
}
