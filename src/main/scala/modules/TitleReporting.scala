package modules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import irc.utilities.URLParser
import ircbot.{BotCommand, Module}
import out.Out


class TitleReporting extends Module{
  override val commands: Map[String, String] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(m.command == MessageCommands.PRIVMSG && !m.trailing.contains("Reporting in!")){
      if(m.trailing.contains("http://") || m.trailing.contains("https://")){
        val messageSplit: Array[String] = m.trailing.split("\\s+")
        for(i <- 0 until messageSplit.length){
          if(messageSplit(i).startsWith("http://") || messageSplit(i).startsWith("https://")){
            var title = URLParser.find(messageSplit(i))
            if(title != null) {
              title = title.replace("http://", "").replace("https://", "")
              r.say(target, title)
            }
          }
        }
      }
    }
  }
}
