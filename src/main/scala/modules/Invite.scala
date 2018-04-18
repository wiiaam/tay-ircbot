package modules

import irc.config.Configs
import irc.info.Info
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}
import scala.collection.JavaConversions._


class Invite extends BotModule{

  private var cooldown: Long = System.currentTimeMillis()/1000

  private val minUsers = 30

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(m.command == MessageCommands.INVITE){
      if(m.sender.isAdmin) {
        r.join(m.trailing)
        r.say(m.trailing, s"${m.sender.nickname} has invited me here. To see my commands, use .help")
        return
      }
      if(cooldown <= System.currentTimeMillis()/1000 && m.config.isInviteable){
        r.join(m.trailing)
        r.say(m.trailing, s"${m.sender.nickname} has invited me here. A minimum of $minUsers users are required " +
          s"for me to stay in the channel. To see my commands, use .help")
        cooldown = System.currentTimeMillis()/1000 + 30
        Thread.sleep(10000)
        val channelOption = Info.get(m.server).get.getChannels.get(m.trailing)
        if(channelOption.isEmpty){ // May occur if info wasn't fetched properly.
          r.say(m.trailing, "An error has occured when checking for users. Please try and invite me later.")
          r.part(m.trailing)
          return
        }
        val userlist = channelOption.get.users
        if(userlist.size < minUsers - 1){ //Account for bot as a user as well
          r.say(m.trailing, s"You do not have the required usercount for me to stay in this channel. " +
            s"This requirement is to stop invite spam and bot manipulation. Please try again when you have at least " +
            s"$minUsers users in the channel.")
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

          if(adminString.contains(",")){
            r.say(m.trailing, "An exemption can be made by messaging an admin. The current admins are: " + adminString)
          }
          else if(adminString.length != 0){
            r.say(m.trailing, "An exemption can be made by messaging an admin. The current admin is: " + adminString)
          }

          r.part(m.trailing)
        }

      }
      else if(m.config.isInviteable){
        val timeleft = Math.ceil(cooldown - (System.currentTimeMillis()/1000)).asInstanceOf[Int]
        r.notice(m.sender.nickname, s"I am currently on invite cooldown. Please wait another $timeleft seconds and invite me again")
      }
    }
  }
}
