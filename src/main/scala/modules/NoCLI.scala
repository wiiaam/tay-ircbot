package modules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotModule, BotCommand}


class NoCLI extends BotModule{
  override val adminCommands: Map[String, Array[String]] = Map("nocli" -> Array("Turn on nocli in #pasta.",
    "nocli bans all users that try to enter with a command line IRC client"))

  var on = false

  var checks: Set[String] = Set()
  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(b.command == "nocli" && m.sender.isAdmin){
      on = !on
      r.say(m.target, s"nocli is now ${if(on)"on" else "off"}")
    }

    if(on) {
      if (m.command == MessageCommands.JOIN && m.trailing == "#pasta") {
        checks = checks + m.sender.nickname
        r.CTCP(m.sender.nickname, "VERSION")
      }
      if (m.command == MessageCommands.NOTICE && m.trailing.contains("VERSION") && checks.contains(m.sender.nickname)) {
        if (m.trailing.toLowerCase.contains("weechat")) {
          r.send("MODE #pasta +b @" + m.sender.host)
          r.send("KICK #pasta " + m.sender.nickname + " :get a better client autist faggot")
        }
      }
    }
  }
}
