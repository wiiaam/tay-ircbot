package irc.listeners

import irc.message.Message
import irc.server.ServerResponder
import ircbot.BotCommand


trait OnMessageListener {
  def onMessage(m: Message, b: BotCommand, r: ServerResponder)
}
