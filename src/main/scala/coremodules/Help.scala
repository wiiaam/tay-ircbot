package coremodules

import irc.message.Message
import irc.server.ServerResponder
import ircbot.{Modules, Module, BotCommand}
import out.Out
import scala.collection.JavaConversions._



class Help extends Module{

  override val commands: Map[String, String] = Map("help" -> "Displays help information. Use .help <command> for more info" )

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val bAsDot = new BotCommand(m,".")
    Out.println(s"bot command: ${bAsDot.isBotCommand}, command: ${bAsDot.command}")
    if(bAsDot.command == "help"){
      if(bAsDot.hasParams){
        var allCommands: Map[String,String] = Map()
        for(module <- Modules.modules){
          allCommands = allCommands ++ module.commands
        }
        for(module <- Modules.coreModules){
          allCommands  = allCommands ++ module.commands
        }
        val command = bAsDot.paramsArray(0)
        for((k,v) <- allCommands){
          Out.println(s"command $k, val $v")
        }
        if(allCommands.contains(command)){
          r.notice(m.sender.nickname, s"[$command] ${allCommands(command)}")
        }
        else{
          r.notice(m.sender.nickname, s"$command is not a valid command")
        }
      }
      else{
        var commands = ""
        for(module <- Modules.modules){
          for((k,v) <- module.commands){
            commands += k + " "
          }
        }
        for(module <- Modules.coreModules){
          for((k,v) <- module.commands){
            commands += k + " "
          }
        }
      }
    }
  }
}
