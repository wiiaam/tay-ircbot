package irc.message

class MessageParams(params: Array[String]) {
  val array = params
  def first = params(0)
}
