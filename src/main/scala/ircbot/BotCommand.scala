package ircbot

import irc.message.Message


class BotCommand(message: Message) {
  val char = Configs.get
}
