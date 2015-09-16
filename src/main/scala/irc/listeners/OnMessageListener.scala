package irc.listeners

import irc.message.Message
import irc.server.ServerResponder


trait OnMessageListener {
  def onMessage(m: Message, r: ServerResponder)
}
