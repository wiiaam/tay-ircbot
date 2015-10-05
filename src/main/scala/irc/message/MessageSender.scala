package irc.message

import irc.config.Configs
import irc.info.Info
import out.Out
import scala.collection.JavaConversions._

class MessageSender(sender: String, serverName: String) {
  val isServer = !sender.contains("@")


  val whole = sender
  private var split = sender.split("!")
  val nickname = if(isServer){
    sender
  }
  else {
    split(0)
  }

  val username = if(isServer){
    sender
  }
  else {
    split = split(1).split("@")
    split(0)
  }
  val host = if(isServer){
    sender
  }
  else {
    split(1)
  }

  def isAdmin: Boolean = {
    val config = Configs.get(serverName).get
    val admins = config.getAdmins
    val isRegistered = if(Info.get(serverName).isDefined){
      if(Info.get(serverName).get.findUser(nickname).isEmpty) return false
      else{
        Info.get(serverName).get.findUser(nickname).get.isRegistered
      }

    }
    else false

    for(admin: String <- admins){
      if(admin.startsWith("@") && admin == "@" + host) return true
      if(isRegistered && nickname == admin) return true
    }
    false
  }

  def isRegistered: Boolean = {
    if(Info.get(serverName).isDefined) {
      if (Info.get(serverName).get.findUser(nickname).isEmpty) false
      else {
        Info.get(serverName).get.findUser(nickname).get.isRegistered
      }
    }
    else false
  }
}
