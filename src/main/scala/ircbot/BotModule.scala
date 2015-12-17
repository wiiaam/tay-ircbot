package ircbot

import irc.message.Message
import irc.server.ServerResponder

trait BotModule {

  val commands: Map[String,Array[String]] = Map()

  val adminCommands: Map[String, Array[String]] = Map()

  def parse(m: Message, b: BotCommand, r: ServerResponder)

}
