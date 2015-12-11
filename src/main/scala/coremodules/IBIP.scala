package coremodules

import irc.message.Message
import irc.server.{Priorities, ServerResponder}
import ircbot.{AbstractBotModule, BotCommand}


class IBIP extends AbstractBotModule {

  override val commands: Map[String, Array[String]] = Map("bots" -> Array("Respond to IBIP. See github.com/Teknikode/IBIP for more info"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.trailing.startsWith(".bots ") || m.trailing == ".bots"){
      val target = if(m.params.first.startsWith("#")) m.params.first else m.sender.nickname
      r.pm(target, "Reporting in! [Scala] wip", Priorities.HIGH_PRIORITY)
    }
  }


}
