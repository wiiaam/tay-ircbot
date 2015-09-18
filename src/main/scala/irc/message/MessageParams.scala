package irc.message

class MessageParams(params: Array[String]) {
  val array = params
  val first = {
    if(params.length > 0) params(0)
    else ""
  }
}
