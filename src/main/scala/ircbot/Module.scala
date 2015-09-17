package ircbot

import irc.message.Message
import irc.server.ServerResponder

trait Module {

  val commands: Map[String,String]

  def parse(m: Message, b: BotCommand, r: ServerResponder)
}
