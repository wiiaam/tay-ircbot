package coremodules

import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class IBIP extends Module {

  override val commands: Map[String, String] = Map("bots" -> "Respond to IBIP. See github.com/Teknikode/IBIP for more info")

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.trailing.startsWith(".bots ") || m.trailing == ".bots"){
      r.pm(m.params.first, "Reporting in! [Scala] wip")
    }
  }


}
