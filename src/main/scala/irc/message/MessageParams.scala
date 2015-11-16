package irc.message

class MessageParams(params: Array[String]) {
  val array = params
  val first = {
    if(params.length > 0) params(0)
    else ""
  }
  val all = {
    if(params.length == 0) ""
    else {
      var str = ""
      for (param <- params) {
        str += " " + param
      }
      str.substring(1)
    }
  }
}
