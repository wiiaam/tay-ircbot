package coremodules

import irc.info.Info
import irc.message.{Message, MessageCommands}
import irc.server.{Priorities, ServerResponder}
import ircbot.{BotCommand, BotModule, Constants}

import scala.util.Random


class IBIP extends BotModule {

  override val commands: Map[String, Array[String]] = Map("bots" -> Array("Respond to IBIP. See github.com/Teknikode/IBIP for more info"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if((m.trailing.startsWith(".bots ") || m.trailing == ".bots") && m.command == MessageCommands.PRIVMSG){
      val target = if(m.params.first.startsWith("#")) m.params.first else m.sender.nickname
      val desc = "sauce @ " + Constants.REPO/**(for{
        info <- Info.get(m.server)
        channel <- info.findChannel(m.params.first)
      } yield {
        var name = m.config.getNickname
        while(name == m.config.getNickname){
          name = channel.users.keySet.toVector(Random.nextInt(channel.users.keySet.size))
        }
        "hey " + name
      }).getOrElse("taylor swift is bae")**/



      r.pm(target, s"Reporting in! [Scala] $desc", Priorities.HIGH_PRIORITY)
    }
  }


}
