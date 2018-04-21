package coremodules

import irc.info.Info
import irc.message.{Message, MessageCommands}
import irc.server.{Priorities, ServerResponder}
import ircbot.{BotCommand, BotModule, Constants}

import scala.util.Random


class IBIP extends BotModule {

  private var cooldown = System.currentTimeMillis()

  private val strings = Array("h-hey", "hi", "hows it going,", "whats up", "salutations,", "yoza", "h-hi", "sup",
  "gidday", "s-sempai")


  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if((m.trailing.startsWith(".bots ") || m.trailing == ".bots") && m.command == MessageCommands.PRIVMSG &&
    cooldown < System.currentTimeMillis()){
      val target = if(m.params.first.startsWith("#")) m.params.first else m.sender.nickname
      val desc = strings(Random.nextInt(strings.length)) + " " + m.sender.nickname

      r.pm(target, s"Reporting in! [Scala] $desc", Priorities.HIGH_PRIORITY)
      cooldown = System.currentTimeMillis() + 5000
    }
  }


}
