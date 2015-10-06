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

  def say(target: String, message: String): Unit = {
    if(target.startsWith("#")) ircServer.send(s"PRIVMSG $target :$message")
    else ircServer.send(s"NOTICE $target :$message")
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

  def say(target: String, message: String, priority: Priorities.Value): Unit = {
    if(target.startsWith("#")) ircServer.send(s"PRIVMSG $target :$message", priority)
    else ircServer.send(s"NOTICE $target :$message", priority)
  }

  def join(channel :String): Unit ={
    ircServer.send(s"JOIN $channel")
  }

  def part(channel: String): Unit ={
    ircServer.send(s"PART $channel")
  }

  def topic(channel: String, topic: String): Unit ={
    ircServer.send(s"TOPIC $channel :$topic")
  }

  def nick(nick: String): Unit ={
    ircServer.send("NICK " + nick)
  }

  def CTCP(target: String, message: String): Unit ={
    pm(target, "\u0001" + message + "\u0001")
  }

}
