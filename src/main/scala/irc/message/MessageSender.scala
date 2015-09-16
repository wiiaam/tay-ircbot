package irc.message

class MessageSender(sender: String) {
  val isServer = !sender.contains("@")

  val whole = sender
  private var split = sender.split("!")
  val nickname = if(isServer){
    sender
  }
  else {
    split(0)
  }
  split = split(1).split("@")
  val username = if(isServer){
    sender
  }
  else {
    split(0)
  }
  val address = if(isServer){
    sender
  }
  else {
    split(1)
  }
}
