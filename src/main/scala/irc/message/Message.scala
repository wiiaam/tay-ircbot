package irc.message

class Message(ircFormattedString: String, serverName:String) {
  val server = serverName
  private var msgSender = new MessageSender("fakenick!fakeuser@fakehost")
  private var msgCommand = MessageCommands.UNKNOWN
  private var msgParams = new MessageParams(new Array[String](1))
  if(ircFormattedString.startsWith(":")) {
    msgSender = new MessageSender(ircFormattedString.split("\\s+")(0).substring(1))
  }
  else{
    val split = ircFormattedString.split(":")
    val prefix = split(0)
    val prefixSplit = prefix.split("\\s+")
    msgCommand = MessageCommands.valueOf(prefixSplit(0))

  }

  val sender = msgSender
  val command = msgCommand

}
