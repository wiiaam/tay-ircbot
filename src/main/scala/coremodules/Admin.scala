package coremodules

import irc.config.Configs
import irc.info.Info
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}
import scala.collection.JavaConversions._


class Admin extends BotModule{

  override val commands: Map[String, Array[String]] = Map("admins" -> Array("Lists the current admins"))

  override val adminCommands: Map[String, Array[String]] = Map("join" -> Array("Tell the bot to join a channel", "To use: %pjoin <channels>"),
    "nick" -> Array("Change the bots nickname", "To use: %pnick <nickname>"),
    "leave" -> Array("Tell the bot to leave the current channel"),
    "part" -> Array("Tell the bot to part a specific channel", "To use: %ppart <channels>"),
    "pm" -> Array("Tell the bot to PRIVMSG a channel", "To use: %ppm <channel> <message>"),
    "raw" -> Array("Tell the bot to send a raw IRC message", "To use: %praw <message>"),
    "admin" -> Array("Add or delete admins", "To use: %padmin <add/del> user"))


  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.PRIVMSG || m.command == MessageCommands.NOTICE){

      if (b.command == "admins"){
        val config = Configs.get(m.server).get
        val admins = config.getAdmins
        var adminString = ""
        for(admin <- admins){
          if(!admin.startsWith("@")){
            if(adminString.length == 0){
              adminString = admin
            }
            else {
              adminString = adminString + ", " + admin
            }
          }
        }
        if(adminString.length == 0){
          r.reply("There are currently no admins set")
        }
        else if(adminString.contains(",")){
          r.reply("Current admins are: " + adminString)
        }
        else{
          r.reply("Current admin is: " + adminString)
        }
      }

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

        if(b.command == "info"){
          if(b.hasParams){
            if(b.paramsArray.length > 1){
              val user = b.paramsArray(0)
              val channel = b.paramsArray(1)
              val info = Info.get(m.server).get
              val userinfoOption = info.findInChannel(user, channel)
              userinfoOption match {
                case None =>
                  r.reply("Could not find user: " + user)

                case _ =>
                  val userinfo = userinfoOption.get
                  r.reply("Info for user: " + user)
                  r.reply("Modes: " + userinfo.modes)

              }
            }
            else {
              val user = b.paramsArray(0)
              val info = Info.get(m.server).get
              val userinfoOption = info.findUser(user)
              userinfoOption match {
                case None =>
                  r.reply("Could not find user: " + user)

                case _ =>
                  val userinfo = userinfoOption.get
                  r.reply("Info for user: " + user)
                  r.reply("Channels: " + userinfo.modes)

              }
            }

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
              r.part(channelName, "Cleaning channel list")
              cleaned = cleaned + " " + channelName
            }
          }
          r.reply(m.sender.nickname + ": left channels " + cleaned)
        }

        if(b.command == "announce"){
          val channels = Info.get(m.server).get.getChannels
          if(b.paramsArray.length > 0) {
            for ((channelName, channel) <- channels) {
              if (channelName != "*") {
                r.say(channelName, b.paramsString)
              }
            }
          }
        }

        if(b.command == "channels") {
          var list = "Current channel list:  "
          val channels = Info.get(m.server).get.getChannels
          if(b.paramsArray.length > 0){
            if(b.paramsArray(0) == "verbose"){
              for ((channelName, channel) <- channels) {
                if(channelName != "*"){
                  var users = channel.users.size
                  var owner = 0
                  var sop = 0
                  var aop = 0
                  var hop = 0
                  var vop = 0
                  var currentRank = "none"
                  for((userName, user) <- channel.users){
                    if(user.modes.contains("~")){
                      owner += 1
                      if(userName == m.config.getNickname) currentRank = "owner"
                    }
                    if(user.modes.contains("&")){
                      sop += 1
                      if(userName == m.config.getNickname) currentRank = "sop"
                    }
                    if(user.modes.contains("@")){
                      aop += 1
                      if(userName == m.config.getNickname) currentRank = "aop"
                    }
                    if(user.modes.contains("%")){
                      hop += 1
                      if(userName == m.config.getNickname) currentRank = "hop"
                    }
                    if(user.modes.contains("+")){
                      vop += 1
                      if(userName == m.config.getNickname) currentRank = "vop"
                    }
                  }
                  list = list + "\u0002" + channelName + s"\u0002 users:$users own:$owner sop:$sop aop:$aop hop:$hop vop:$vop myrank:$currentRank | "
                }


              }
              list = list.substring(0, list.length - 2)
              r.notice(m.sender.nickname, list)
            }
          }
          else{
            for ((channelName, channel) <- channels) {
              if(channelName != "*") list = list + channelName + " "
            }
            r.notice(m.sender.nickname, list)
          }
        }
      }
    }
  }
}
