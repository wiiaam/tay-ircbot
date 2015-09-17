package irc.message

object MessageCommands extends Enumeration{
  type MessageCommand = Value
  val UNKNOWN = Value("UNKNOWN")
  val PRIVMSG = Value("PRIVMSG")
  val NOTICE = Value("NOTICE")
  val AWAY = Value("AWAY")
  val JOIN = Value("JOIN")
  val KICK = Value("KICK")
  val MODE = Value("MODE")
  val QUIT = Value("QUIT")
  val NICK = Value("NICK")
  val PING = Value("PING")
  val PONG = Value("PONG")
  val INVITE = Value("INVITE")
  val TOPIC = Value("TOPIC")
  val CONNECTED = Value("001")
  val NICKINUSE = Value("433")

  def valueOf(name: String): MessageCommands.Value = values.find(_.toString == name).getOrElse(MessageCommands.UNKNOWN)
}
