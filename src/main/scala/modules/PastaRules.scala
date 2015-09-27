package modules

import irc.config.UserConfig
import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class PastaRules extends Module{
  override val commands: Map[String, Array[String]] = Map("rules" -> Array("Only works in #pasta. Displays the current rules"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val bAsDot = new BotCommand(m, ".")
    val bAsTilde = new BotCommand(m,"~")
    if(m.params.first == "#pasta" && (bAsDot.command == "rules" || bAsTilde.command == "rules")){
      val rules = UserConfig.getJson.getJSONArray("pastarules")
      for(i <- 0 until rules.length()){
        r.notice(m.sender.nickname, s"Rule $i: ${rules.getString(i)}")
      }
    }
  }
}
