package irc.server

object Priorities extends Enumeration{
  type IrcSendPriority = Value
  val HIGH_PRIORITY = Value(1)
  val STANDARD_PRIORITY = Value(2)
  val LOW_PRIORITY = Value(3)
}
