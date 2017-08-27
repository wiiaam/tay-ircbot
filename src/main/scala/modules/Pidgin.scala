package modules

import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}

class Pidgin extends BotModule{

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(b.command == "pidgin"){

    }
  }


  private def startRssMonitor(): Unit ={

  }

}
