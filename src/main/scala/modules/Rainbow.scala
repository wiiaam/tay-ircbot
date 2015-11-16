package modules

import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class Rainbow extends Module{
  override val commands: Map[String, Array[String]] = Map("rb" -> Array("Convert text to rainbow"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(b.command == "rb" || b.command == "rainbow"){

      if(b.hasParams){
        val chars = b.paramsString.toCharArray
        var message = ""
        var sequence = 0
        for(i: Int <- 0 until chars.length){
          if(i == 7*sequence + 0) message += "\u000304" + chars(i)
          if(i == 7*sequence + 1) message += "\u000307" + chars(i)
          if(i == 7*sequence + 2) message += "\u000308" + chars(i)
          if(i == 7*sequence + 3) message += "\u000303" + chars(i)
          if(i == 7*sequence + 4) message += "\u000302" + chars(i)
          if(i == 7*sequence + 5) message += "\u000306" + chars(i)
          if(i == 7*sequence + 6) {
            message += "\u000313" + chars(i)
            sequence += 1
          }
        }
        r.say(target, message)
      }
    }
  }
}
