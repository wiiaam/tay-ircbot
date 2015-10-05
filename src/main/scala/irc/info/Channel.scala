package irc.info


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

  /**
   *
   * Returns the user's rank
   *
   * 5 = owner
   * 4 = protectop
   * 3 = op
   * 2 = halfop
   * 1 = voice
   * 0 = normal
   * -1 = user does not exist
   */
  def getRank(nick: String): Int ={
    if(!users.contains(nick)) return -1
    val user = users(nick)
    val modes = user.modes
    if(modes.contains("~")) return 5
    if(modes.contains("&")) return 4
    if(modes.contains("@")) return 3
    if(modes.contains("%")) return 2
    if(modes.contains("+")) return 1
    0
  }
}
