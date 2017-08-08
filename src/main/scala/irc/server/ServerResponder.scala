package irc.server

import irc.info.Info


class ServerResponder(ircServer: IrcServer, sender: String) {

  def send(message: String): Unit = {
    ircServer.send(message)
  }

  def announce(message: String): Unit = {
    val channels = Info.get(ircServer.fileName).get.getChannels
    if(message.length > 0) {
      for ((channelName, channel) <- channels) {
        if (channelName != "*") {
          say(channelName, message)
        }
      }
    }
  }

  def reply(message: String): Unit ={
    pm(sender, message)
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

  def part(channel: String, reason: String = "Leaving"): Unit ={
    var cnl = channel
    if(!cnl.startsWith("#")) cnl = "#" + cnl
    ircServer.send(s"PART $cnl :$reason")
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

  def kick(channel: String, user: String): Unit ={
    ircServer.send(s"KICK $channel $user :$user")
  }

  def kick(channel: String, user: String, message: String): Unit ={
    ircServer.send(s"KICK $channel $user :$message")
  }

  def ban(channel: String, ban: String): Unit ={
    ircServer.send(s"MODE $channel +b $ban")
  }

  def unban(channel: String, ban: String): Unit ={
    ircServer.send(s"MODE $channel -b $ban")
  }

  def action(target: String, action: String): Unit ={
    ircServer.send(s"PRIVMSG $target :\u0001ACTION ${action}\u0001")
  }

}
