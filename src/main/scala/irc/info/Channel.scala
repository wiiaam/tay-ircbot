package irc.info

import irc.info.Rank.Rank

class Channel {

  var users: Map[String, User] = Map()

  def parseUser(info: Map[String, String]): Unit = {
    if(users.contains(info("nick"))){
      users(info("nick")).parse(info)
    }
    else{
      val user = new User(info("nick"),"","","","")
      user.parse(info)
      users = users ++ Map(info("nick") -> user)
    }
  }

  def findUser(nick: String): Option[User] = {
    if(users.contains(nick)) None
    Some(users(nick))
  }

  def getRank(nick: String): Rank ={
    if(!users.contains(nick)) return Rank.UNKNOWN
    val user = users(nick)
    val modes = user.modes
    if(modes.contains("~")) return Rank.OWNER
    if(modes.contains("&")) return Rank.SOP
    if(modes.contains("@")) return Rank.AOP
    if(modes.contains("%")) return Rank.HOP
    if(modes.contains("+")) return Rank.VOICE
    Rank.USER
  }
}
