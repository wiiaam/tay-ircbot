package modules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}
import out.Out

class Invite extends BotModule{

  private var cooldown: Long = System.currentTimeMillis()/1000

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(m.command == MessageCommands.INVITE){
      if(m.sender.isAdmin){ //cooldown <= System.currentTimeMillis()/1000
        r.join(m.trailing)
        r.say(m.trailing, s"${m.sender.nickname} has invited me here. To see my commands, use .help")
        cooldown = System.currentTimeMillis()/1000 + 30
      }
      else{
        val timeleft = Math.ceil(cooldown - (System.currentTimeMillis()/1000)).asInstanceOf[Int]
        r.notice(m.sender.nickname, s"I am currently on invite cooldown. Please wait another $timeleft seconds and invite me again")
      }
    }
  }
}
