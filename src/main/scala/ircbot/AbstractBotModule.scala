package ircbot


abstract class AbstractBotModule extends BotModule{

  override val commands: Map[String, Array[String]] = Map()

  override val adminCommands: Map[String, Array[String]] = Map()
}
