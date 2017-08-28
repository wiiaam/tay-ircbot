package irc.info


class User(nick: String, user: String, hostAddress: String, real: String, usermodes: String){

  var nickname: String = nick
  var username: String = user
  var realname: String = real
  var host: String = hostAddress
  var modes: String = usermodes

  def parse(info: Map[String, String]): Unit ={
    username = Info.getMappedInfo("user", info).getOrElse(username)
    modes = Info.getMappedInfo("modes", info).getOrElse(modes)
    realname = Info.getMappedInfo("realname", info).getOrElse(realname)
    host = Info.getMappedInfo("host", info).getOrElse(host)
  }

  // Currently only working on rizon
  def isRegistered: Boolean = {
    modes.contains("r")
  }
}
