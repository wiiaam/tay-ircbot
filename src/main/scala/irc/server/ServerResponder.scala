package irc.server


class ServerResponder(ircServer: IrcServer) {

  def send(message: String): Unit = {
    ircServer.send(message)
  }

  def pm(target: String, message: String): Unit = {
    ircServer.send(s"PRIVMSG $target :$message")
  }

  def notice(target: String, message: String): Unit ={
    ircServer.send(s"NOTICE $target :$message")
  }

  def send(message: String, priority: Priorities.Value): Unit = {
    ircServer.send(message, priority)
  }

  def pm(target: String, message: String, priority: Priorities.Value): Unit = {
    ircServer.send(s"PRIVMSG $target :$message", priority)
  }

  def notice(target: String, message: String, priority: Priorities.Value): Unit ={
    ircServer.send(s"NOTICE $target :$message", priority)
  }

  def join(room :String): Unit ={
    ircServer.send(s"JOIN $room")
  }

  def part(room: String): Unit ={
    ircServer.send(s"PART $room")
  }

}
