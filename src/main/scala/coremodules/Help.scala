package coremodules

import irc.config.Configs
import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}
import out.Out


class Help extends Module{

  override val commands: Map[String, String] = Map("help" -> "Displays help information. Use .help <command> for more info" )

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val bAsDot = new BotCommand(m,".")

    Out.println(m.command + "")
    Out.println(m.trailing)
  }
}
