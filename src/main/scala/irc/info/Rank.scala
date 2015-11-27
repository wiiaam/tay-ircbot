package irc.info


object Rank extends Enumeration {
  type Rank = Value
  val UNKNOWN, USER, VOICE, HOP, OP, AOP, SOP, OWNER = Value
}
