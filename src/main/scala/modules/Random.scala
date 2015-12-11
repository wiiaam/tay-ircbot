package modules

import irc.info.Info
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class Random extends Module{
  override val commands: Map[String, Array[String]] = Map("slap" -> Array("Slap some sense into a user"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(b.command == "slap"){
      if(b.hasParams) r.action(target,s"slaps ${b.paramsString}")
    }

    if(m.command == MessageCommands.PRIVMSG && m.params.first == "#pasta" && m.trailing.trim == "^") r.say("#pasta","can confirm")

    if(m.trailing.startsWith("\u0001ACTION") && m.params.first == "#pasta"){
      val action = m.trailing.substring("\u0001ACTION".length).replace("\u0001","").trim

      var highlight = false
      for((username, user) <- Info.get(m.server).get.findChannel(m.params.first).get.users){
        if(!highlight){
          if(action.equals(username) || action.startsWith(username + " ") || action.endsWith(" " + username) || action.contains(" " + username + " ")){
            highlight = true
          }
        }
      }
      if(!highlight){
        if(action.startsWith("is ")){
          r.action(target, "is also" + action.substring(2))
        }
        else {
          r.action(target, "also " + action)
        }
      }
    }

    if(b.command == "triggergen2" && m.sender.isAdmin){
      for {
        info <- Info.get(m.server)
        channel <- info.findChannel(m.params.first)
      } yield {
        val users = channel.users
        users.foreach(user => {
          val username = user._1
          if (user._2.modes.contains("~")) {
            r.send(s"MODE ${m.params.first} +aohv $username $username $username $username")
          }
          else if (user._2.modes.contains("&")) {
            r.send(s"MODE ${m.params.first} +ohv $username $username $username")
          }
          else if (user._2.modes.contains("@")) {
            r.send(s"MODE ${m.params.first} +hv $username $username")
          }
          else if (user._2.modes.contains("%")) {
            r.send(s"MODE ${m.params.first} +v $username")
          }
        })
      }
    }

    if(b.command == "banall" && m.sender.isAdmin){
      for {
        info <- Info.get(m.server)
        channel <- info.findChannel(m.params.first)
      } yield {
        val users = channel.users
        users.foreach(user => {
          r.send(s"MODE ${m.params.first} +bb ${user._2.nickname}!*@* @${user._2.host}")
        })
      }
    }

    if(m.params.first.startsWith("#")){
      if(m.trailing.toLowerCase.trim == s"ayy ${m.config.getNickname.toLowerCase}"){
        r.pm(m.target, m.trailing.toLowerCase.replace(m.config.getNickname.toLowerCase, m.sender.nickname))
      }
    }
  }
}
