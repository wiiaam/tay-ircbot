package ircbot

import irc.message.Message

trait Module {

  def parse(m: Message)
}
