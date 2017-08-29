package irc.config

import java.io.{BufferedReader, File, FileReader, FileWriter}
import java.util

import org.json.{JSONArray, JSONObject}

import scala.collection.JavaConversions._


class Config(jsonFile: File)  {
  private val reader = new BufferedReader( new FileReader (jsonFile))
  private var jsonString = ""
  while(reader.ready()){
    jsonString += reader.readLine() + "\n"
  }



  private var json = new JSONObject(jsonString)

  private var adminset = new util.HashSet[String]
  private val admins = json.getJSONArray("admins")
  for(i <- 0 until admins.length()){
    adminset.add(admins.getString(i))
  }

  private var ignoreset = new util.HashSet[String]
  private val ignores = json.getJSONArray("ignores")
  for(i <- 0 until ignores.length()){
    ignoreset.add(ignores.getString(i))
  }

  private var channelset = new util.HashSet[String]
  private val channels = json.getJSONArray("channels")
  for(i <- 0 until channels.length()){
    channelset.add(channels.getString(i))
  }

  var networkName = ""

  def getServerPassword: String = json.getString("serverPassword")

  def getServer: String = json.getString("server")

  def getPort: Int = json.getInt("port")

  def useSSL: Boolean = json.getBoolean("ssl")

  def getNickname: String = json.getString("nickname")

  def getUsername: String = json.getString("username")

  def getRealname: String = json.getString("realname")

  def getCommandPrefix: String = json.getString("commandprefix")

  def ghostExisting: Boolean = json.getBoolean("ghostexisting")

  def useNickServ: Boolean = json.getBoolean("nickserv")

  def getPassword: String = json.getString("password")

  def setNickname(nick: String): Unit = {
    json.put("nickname", nick)
    write()
  }

  def setCommandPrefix(prefix: String): Unit = {
    json.put("commandprefix", prefix)
    write()
  }

  def getAdmins: util.HashSet[String] = adminset

  def getIgnores: util.HashSet[String] = ignoreset

  def getChannels: util.HashSet[String] = channelset

  def addAdmin(admin: String): Unit = {
    adminset.add(admin)
    onAdminChange()
    write()
  }

  def addIgnore(ignore: String): Unit = {
    ignoreset.add(ignore)
    onIgnoreChange()
    write()
  }

  def addChannel(channel: String): Unit = {
    channelset.add(channel)
    onChannelsChange()
    write()
  }

  def removeAdmin(admin: String): Unit = {
    adminset.remove(admin)
    onAdminChange()
    write()
  }

  def removeIgnore(ignore: String): Unit = {
    ignoreset.remove(ignore)
    onIgnoreChange()
    write()
  }

  def removeChannel(channel: String): Unit = {
    channelset.remove(channel)
    onChannelsChange()
    write()
  }


  private def onAdminChange() {
    val admins = new JSONArray()
    for(admin: String <- adminset){
      admins.put(admin)
    }
    json.put("admins", admins)
  }

  private def onIgnoreChange() {
    val ignores = new JSONArray()
    for(ignore: String <- ignoreset){
      ignores.put(ignore)
    }
    json.put("ignores", ignores)
  }

  private def onChannelsChange() {
    val channels = new JSONArray()
    for(channel: String <- channelset){
      channels.put(channel)
    }
    json.put("channels", channels)
  }

  private def write() {
    val writer = new FileWriter(jsonFile,false)
    writer.write(json.toString(4))
    writer.close()
    Configs.set(jsonFile.getName.split("\\.")(0), this)
  }
}
