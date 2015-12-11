package modules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{AbstractBotModule, BotCommand, BotModule}

import scala.util.Random


class Responses extends AbstractBotModule{

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(m.command == MessageCommands.PRIVMSG){
      for(trigger <- Set("hi", "hey")){
        if((m.trailing.toLowerCase.contains(" " + trigger + " ") || m.trailing.toLowerCase.startsWith(trigger + " "))
          && m.trailing.contains(m.config.getNickname)){
          val responses = Array("hi", "hey")
          Thread.sleep(1200)
          if(Random.nextDouble() > 0.3)r.say(target, responses(Random.nextInt(responses.length)) + " " + m.sender.nickname)
        }
      }
    }
  }
}
