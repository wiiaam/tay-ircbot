package coremodules

import irc.info.Info
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class InfoParser extends BotModule{

  // :irc.x2x.cc 352 wiiaam #taylorswift ~t oh.my.what.a.marvellous.tune * taylorswift Hr@ :0 taylorswift
  //                  0 yourname  1 channel 2 username 3 host      4  5 nickname 6 modes
  // :irc.x2x.cc 352 wiiaam #taylorswift ~wiiaam systemd.is.a.virus * wiiaam Hr~ :0 william

  // >> :wiiaam!~wiiaam@systemd.is.a.virus MODE #taylorswift +h wiiaam
  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(m.command == MessageCommands.JOIN){
      r.send("WHO " + m.trailing)
    }

    if(m.command == MessageCommands.MODE){
      r.send("WHO" + m.params.first)

      /** WIP
      if(m.params.first.startsWith("#") && m.params.array.length > 2){
        var modeChange = m.params.array(1)
        if(modeChange.startsWith("+")) modeChange = modeChange.substring(1)
        var added = modeChange.split("\\+")
        var count = 2
        for(i <- added.indices){
          println(Info.get(m.server).get.getChannels.get(m.params.first).get.users.get("wiiaam"))
          var toModify = added(i)
          toModify = toModify.replace("q", "~").replace("a", "&").replace("o", "@").replace("h", "%").replace("v", "+")
          if(toModify.contains("-")){
            if(toModify.startsWith("-")){
              var info = Info.get(m.server).get.findInChannel(m.params.array(count), m.params.first).get
              var modes = info.modes
              modes = modes.replace(toModify.replace("-", ""), "")
              var newInfo = Map("nick" -> info.nickname, "user" -> info.username, "modes" -> modes, "host" -> info.host,
                "realname" -> info.realname)
              Info.parseToChannel(m.server, m.params.first, newInfo)
              count += 1
            }
            else {
              var split = toModify.split("\\-")
              var info = Info.get(m.server).get.findInChannel(m.params.array(count), m.params.first).get
              var modes = info.modes
              modes = modes + split(0)
              info.modes = modes
              var newInfo = Map("nick" -> info.nickname, "user" -> info.username, "modes" -> modes, "host" -> info.host,
                "realname" -> info.realname)
              Info.parseToChannel(m.server, m.params.first, newInfo)
              count += 1


              info = Info.get(m.server).get.findInChannel(m.params.array(count), m.params.first).get
              modes = info.modes
              modes = modes.replace(split(1), "")
              newInfo = Map("nick" -> info.nickname, "user" -> info.username, "modes" -> modes, "host" -> info.host,
                "realname" -> info.realname)
              Info.parseToChannel(m.server, m.params.first, newInfo)
              count += 1
            }
          }
          else {
            var info = Info.get(m.server).get.findInChannel(m.params.array(count), m.params.first).get
            var modes = info.modes
            modes = modes + toModify
            val newInfo = Map("nick" -> info.nickname, "user" -> info.username, "modes" -> modes, "host" -> info.host,
              "realname" -> info.realname)
            Info.parseToChannel(m.server, m.params.first, newInfo)
            count += 1
          }
        }

      }
        **/
    }

    if(m.command == MessageCommands.NICK){

    }

    /*if(m.command == MessageCommands.NICK){
      Info.changeNick(m.server, m.sender.nickname, m.trailing)
    }*/

    if(m.command == MessageCommands.WHO_OUTPUT){
      val pa = m.params.array
      val info = Map("nick" -> pa(5), "user" -> pa(2), "modes" -> pa(6), "host" -> pa(3), "realname" -> m.trailing.split("\\s+")(1))
      Info.parse(m.server, info)
      if(pa(1) != "*") Info.parseToChannel(m.server, pa(1), info)
    }
  }

}
