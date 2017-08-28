package modules

import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}

import scala.util.Random

class Responses extends BotModule{

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    /*
    if(m.command == MessageCommands.PRIVMSG){
      for(trigger <- Set("hi", "hey")){
        if((m.trailing.toLowerCase.contains(" " + trigger + " ") || m.trailing.toLowerCase.startsWith(trigger + " "))
          && m.trailing.contains(m.config.getNickname)){
          val responses = Array("hi", "hey")
          Thread.sleep(1200)
          if(Random.nextDouble() > 0.3)r.reply(responses(Random.nextInt(responses.length)) + " " + m.sender.nickname)
        }
      }
    }
    */
  }
}
