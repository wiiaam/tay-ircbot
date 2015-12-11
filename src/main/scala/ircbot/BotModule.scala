package ircbot

import irc.message.Message
import irc.server.ServerResponder

trait BotModule {

  val commands: Map[String,Array[String]]

  val adminCommands: Map[String, Array[String]]

  def parse(m: Message, b: BotCommand, r: ServerResponder)
}
