package modules

import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class Should extends BotModule{
  override val commands: Map[String, Array[String]] = Map("should" -> Array("Use %pshould <question> to ask the bot a question",
  "Example: %pshould I grow a beard?"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target: String = if (!m.params.first.startsWith("#")) m.sender.nickname else m.params.first
    if(b.command == "should"){
      if(b.hasParams){
        if(Math.random() > 0.5){
          r.say(target, m.sender.nickname + ": Yes")
        }
        else{
          r.say(target, m.sender.nickname + ": No")
        }
      }
    }
  }
}
