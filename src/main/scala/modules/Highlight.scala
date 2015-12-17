package modules

import irc.info.Info
import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class Highlight extends BotModule{

  override val adminCommands: Map[String, Array[String]] = Map("highlight" -> Array("Highlight certain users in the current channel.",
    "Note that use of this command can get you banned from the channel, and ",
    "use %phighlight <ops/all> to highlight operators or all users respectively"))

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
          var spam = b.paramsString
          for(i <- 0 until 1000){
            spam += s" ${b.paramsString}"
          }
          spam = spam.substring(0,2000)
          r.say(target,spam)
        }
      }
    }
  }
}
