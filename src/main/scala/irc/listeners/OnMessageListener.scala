package irc.listeners

import irc.message.Message
/**
 * Created by william on 9/15/15.
 */
trait OnMessageListener {
  def onMessage(message: Message)
}
