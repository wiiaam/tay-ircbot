package irc.info

import out.Out

object Info {
  private var infos: Map[String, Info] = Map()

  def get(server: String): Option[Info] ={
    if(infos.contains(server)) {
      Some(infos(server))
    }
    else None
  }

  def parse(server: String, info: Map[String, String]): Unit ={
    if(infos.contains(server)) infos(server).parse(info)
    else {
      val serverinfo = new Info()
      serverinfo.parse(info)
      infos = infos ++ Map(server -> serverinfo)
    }
  }

  def parseToChannel(server: String, channel: String, info: Map[String, String]): Unit ={
    if(infos.contains(server)) infos(server).parseToChannel(channel, info)
    else {
      val serverinfo = new Info()
      serverinfo.parseToChannel(channel, info)
      infos = infos ++ Map(server -> serverinfo)
    }
  }

  def getMappedInfo(key: String, info: Map[String, String]): Option[String] = {
    if(info.contains(key)) Some(info(key)) else None
  }
}

class Info {

  private var channels: Map[String, Channel] = Map()
  private var users: Map[String, User] = Map()

  /**
   * Parses a map of info
   *
   * key "nick" is required
   *
   * user = Username
   * host = Host address
   * realname = Realname
   * modes = User Modes
   */
  def parse(info: Map[String, String]): Unit ={
    if(!info.contains("nick")) return
    val nick = info("nick")
    if(users.contains(nick)){
      users(nick).parse(info)
    }
    else {
      val user = new User(nick,"","","","")
      user.parse(info)
      users = users ++ Map(nick -> user)
    }
  }

  /**
   * Parses a map of info to a specific channel
   *
   * key "nick" is required
   *
   * user = Username
   * host = Host address
   * realname = Realname
   * modes = User Modes
   */
  def parseToChannel(channelName: String, info: Map[String, String]): Unit ={
    if(!channels.contains(channelName)) {
      val channel = new Channel
      channel.parseUser(info)
      channels = channels ++ Map(channelName -> channel)
    }
    else{
      channels(channelName).parseUser(info)
    }
  }

  def findUser(nick: String): Option[User] = {
    if(!users.contains(nick)) None
    else Some(users(nick))
  }

  def findChannel(channel: String): Option[Channel] = {
    if(!channels.contains(channel)) None
    else Some(channels(channel))
  }

  def findInChannel(nick: String, channel: String): Option[User] = {
    if(!channels.contains(channel)) None
    if(!channels(channel).users.contains(nick)) None
    else Some(channels(channel).users(nick))
  }
}
