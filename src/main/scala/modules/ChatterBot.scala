package modules

import com.google.code.chatterbotapi.{ChatterBotFactory, ChatterBotType}
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}

class ChatterBot extends BotModule{

  val factory = new ChatterBotFactory
  val bot = factory.create(ChatterBotType.CLEVERBOT).createSession()


  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.PRIVMSG){
      if(m.trailing.startsWith(m.config.getNickname + ": ") || m.trailing.startsWith(m.config.getNickname + ", ")) {
        val target: String = if (!m.params.first.startsWith("#")) m.sender.nickname else m.params.first
        val msg = m.trailing.substring((m.config.getNickname + ": ").length)
        r.say(target, m.sender.nickname + ": " + bot.think(msg))
      }
    }
  }
}
