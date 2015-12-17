package coremodules

import irc.info.Info
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotModule, BotCommand}


class InfoParser extends BotModule{

  // :irc.x2x.cc 352 wiiaam #taylorswift ~t oh.my.what.a.marvellous.tune * taylorswift Hr@ :0 taylorswift
  //                  0 yourname  1 channel 2 username 3 host      4  5 nickname 6 modes
  // :irc.x2x.cc 352 wiiaam #taylorswift ~wiiaam systemd.is.a.virus * wiiaam Hr~ :0 william
  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(m.command == MessageCommands.JOIN){
      r.send("WHO " + m.trailing)
    }

    if(m.command == MessageCommands.MODE){
      r.send("WHO " + m.params.first)
    }


    if(m.command == MessageCommands.WHO_OUTPUT){
      val pa = m.params.array
      val info = Map("nick" -> pa(5), "user" -> pa(2), "modes" -> pa(6), "host" -> pa(3), "realname" -> m.trailing.split("\\s+")(1))
      Info.parse(m.server, info)
      Info.parseToChannel(m.server, pa(1), info)
    }
  }

}
