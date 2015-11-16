package modules

import irc.info.Info
import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class Highlight extends Module{
  override val commands: Map[String, Array[String]] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(b.command == "highlight" && m.params.first.startsWith("#") && m.sender.isAdmin){
      if(b.hasParams){
        if(b.paramsArray(0) == "ops"){
          var msg = ""
          for((username, user) <- Info.get(m.server).get.findChannel(m.params.first).get.users){
            if(user.modes.contains("~") || user.modes.contains("&") || user.modes.contains("@") || user.modes.contains("%")){
              msg += username + " "
            }
          }
          if(msg != "")r.say(target, msg)
        }

        if(b.paramsArray(0) == "all"){
          var msg = ""
          for((username, user) <- Info.get(m.server).get.findChannel(m.params.first).get.users){
            msg += username + " "
          }
          if(msg != "")r.say(target, msg)
        }

        else{
          var spam = b.paramsArray(0)
          for(i <- 0 until 1000){
            spam += s" ${b.paramsArray(0)}"
          }
          spam = spam.substring(0,2000)
          r.say(target,spam)
        }
      }
    }
  }
}
