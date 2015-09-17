package irc.message

import java.util

import irc.message.MessageCommands.MessageCommand
import out.Out

class Message(ircFormattedString: String, serverName:String) {
  val server = serverName
  private var msgSender = new MessageSender("fakenick!fakeuser@fakehost")
  private var msgCommand = MessageCommands.UNKNOWN
  private var msgParams: MessageParams = _
  private var msgTrailing: String = ""
  if(ircFormattedString.startsWith(":")) {
    msgSender = new MessageSender(ircFormattedString.split("\\s+")(0).substring(1))
    val split = ircFormattedString.split("\\s+")
    var isTrailing = false
    val paramsList = new util.ArrayList[String]()
    msgCommand = MessageCommands.valueOf(split(1))
    for(i <- 2 until split.length){

      val next = split(i)
      if(isTrailing) {
        msgTrailing += (" " + next)
      }
      else if (next.startsWith(":")) {
        isTrailing = true
        msgTrailing += next.substring(1)
      }
      else paramsList.add(next)
    }
    msgParams = new MessageParams(paramsList.toArray(new Array[String](0)))
  }
  else{
    val split = ircFormattedString.split("\\s+")
    var isTrailing = false
    val paramsList = new util.ArrayList[String]()
    msgCommand = MessageCommands.valueOf(split(0))
    for(i <- 1 until split.length){

      val next = split(i)
      if(isTrailing) {
        msgTrailing += (" " + next)
      }
      else if (next.startsWith(":")) {
        isTrailing = true
        msgTrailing += next.substring(1)
      }
      else paramsList.add(next)
    }
    msgParams = new MessageParams(paramsList.toArray(new Array[String](0)))
  }

  val sender = msgSender
  val command = msgCommand
  val params = msgParams
  val trailing = msgTrailing
}
