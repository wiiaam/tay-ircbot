package irc.message

import java.util

class Message(ircFormattedString: String, serverName:String) {
  val server = serverName
  private var msgSender = new MessageSender("fakenick!fakeuser@fakehost")
  private var msgCommand = MessageCommands.UNKNOWN
  private var msgParams: MessageParams = _
  private var msgTrailing: String = ""
  if(ircFormattedString.startsWith(":")) {
    msgSender = new MessageSender(ircFormattedString.split("\\s+")(0).substring(1))
    val split = ircFormattedString.split(":")
    if(split.length > 2) {
      for(i: Int <- 2 until split.length) {
        msgTrailing += split(i) + ":"
      }
      msgTrailing = msgTrailing.substring(0, msgTrailing.length-1)
    }
    val prefixSplit = split(1).split("\\s+")
    msgCommand = MessageCommands.valueOf(prefixSplit(1))


    val paramarray: Array[String] = {
      val list = new util.ArrayList[String]()
      for(i <- 2 until prefixSplit.length){
        list.add(prefixSplit(i))
      }
      list.toArray(new Array[String](0))
    }
    msgParams = new MessageParams(paramarray)
  }
  else{
    val split = ircFormattedString.split(":")
    val prefix = split(0)
    val prefixSplit = prefix.split("\\s+")
    msgCommand = MessageCommands.valueOf(prefixSplit(0))
    val paramarray: Array[String] = {
      val list = new util.ArrayList[String]()
      for(i <- 1 until prefixSplit.length){
        list.add(prefixSplit(i))
      }
      list.toArray(new Array[String](0))
    }
    msgParams = new MessageParams(paramarray)
    if(split.length > 1) {
      for(i: Int <- 1 until split.length) {
        msgTrailing += split(i) + ":"
      }
      msgTrailing = msgTrailing.substring(0, msgTrailing.length-1)
    }
  }

  val sender = msgSender
  val command = msgCommand
  val params = msgParams
  val trailing = msgTrailing
}
