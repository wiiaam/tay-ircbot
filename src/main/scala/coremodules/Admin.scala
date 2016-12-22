package coremodules

import irc.info.Info
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class Admin extends BotModule{

  override val adminCommands: Map[String, Array[String]] = Map("join" -> Array("Tell the bot to join a channel", "To use: %pjoin <channels>"),
    "nick" -> Array("Change the bots nickname", "To use: %pnick <nickname>"),
    "leave" -> Array("Tell the bot to leave the current channel"),
    "part" -> Array("Tell the bot to part a specific channel", "To use: %ppart <channels>"),
    "pm" -> Array("Tell the bot to PRIVMSG a channel", "To use: %ppm <channel> <message>"),
    "raw" -> Array("Tell the bot to send a raw IRC message", "To use: %praw <message>"),
    "admin" -> Array("Add or delete admins", "To use: %padmin <add/del> user"))


  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.PRIVMSG || m.command == MessageCommands.NOTICE){
      if(m.sender.isAdmin){
        if(b.command == "join"){
          for(channel <- b.paramsArray){
            r.join(channel)
          }
        }

        if(b.command == "cycle"){
          if(m.params.first.startsWith("#")) {
            r.part(m.params.first)
            r.join(m.params.first)
          }
        }

        if(b.command == "nick"){
          if(b.hasParams) r.nick(b.paramsArray(0))
          m.config.setNickname(b.paramsArray(0))
        }

        if(b.command == "leave"){
          if(m.params.first.startsWith("#")) r.part(m.params.first)
        }

        if(b.command == "part"){
          for(channel <- b.paramsArray){
            r.part(channel)
          }
        }

        if(b.command == "pm"){
          if(b.paramsArray.length > 1)r.pm(b.paramsArray(0), b.paramsString.substring(b.paramsArray(0).length + 1))
        }

        if(b.command == "raw"){
          r.send(b.paramsString)
        }

        if(b.command == "admin"){
          if(b.paramsArray.length > 1){
            b.paramsArray(0) match {
              case "add" =>
                for(i <- 1 until b.paramsArray.length){
                  m.config.addAdmin(b.paramsArray(i))
                }
              case "del" =>
                for(i <- 1 until b.paramsArray.length) {
                  m.config.removeAdmin(b.paramsArray(i))
                }
              case _ =>
                r.say(m.target, s"Usage: ${m.config.getCommandPrefix}admin <add/del> user")
            }
          }
          else{
            r.say(m.target, s"Usage: ${m.config.getCommandPrefix}admin <add/del> user")
          }
        }

        if(b.command == "clean"){
          var cleaned = ""
          var minUsers = 2
          if(b.paramsArray.length > 0){
            try{
              minUsers = b.paramsArray(0).toInt
              if(minUsers < 2) throw new Exception()
            }
            catch {
              case e: Exception => r.reply(m.sender.nickname + ": param must be a number more than 1")
                return
            }
          }
          val channels = Info.get(m.server).get.getChannels
          for((channelName,channel) <- channels){
            println(channelName + " " + channel.users.size)

            if(channel.users.size < minUsers && channelName != "*"){
              r.part(channelName)
              cleaned = cleaned + " " + channelName
            }
          }
          r.reply(m.sender.nickname + ": left channels " + cleaned)
        }

      }
    }
  }
}
