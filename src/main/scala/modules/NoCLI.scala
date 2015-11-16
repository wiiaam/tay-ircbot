package modules

import irc.info.Info
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class NoCLI extends Module{
  override val commands: Map[String, Array[String]] = Map()

  val on = false

  var checks: Set[String] = Set()
  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

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
