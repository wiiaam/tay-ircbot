package ircbot.module

import irc.message.Message

trait Module {

  def parse(m: Message)
}
