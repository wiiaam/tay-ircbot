package irc.config

import java.io.{FileWriter, BufferedReader, FileReader, File}
import java.util
import org.json.{JSONArray, JSONObject}


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

  private var roomsset = new util.HashSet[String]
  private val rooms = json.getJSONArray("rooms")
  for(i <- 0 until rooms.length()){
    roomsset.add(rooms.getString(i))
  }

  def getServer: String = json.getString("server")

  def getPort: Int = json.getInt("port")

  def useSSL: Boolean = json.getBoolean("ssl")

  def getNickname: String = json.getString("server")

  def getUsername: String = json.getString("server")

  def getRealname: String = json.getString("server")

  def getCommandPrefix: String = json.getString("server")

  def useNickServ: Boolean = json.getBoolean("nickserv")

  def getPassword: String = json.getString("password")

  def setNickname(nick: String) = json.put("nickname", nick)

  def setCommandPrefix(prefix: String) = json.put("commandprefix", prefix)

  def getAdmins: util.HashSet[String] = adminset

  def getIgnores: util.HashSet[String] = ignoreset

  def getRooms: util.HashSet[String] = roomsset

  def addAdmin(admin: String): Unit = {
    adminset.add(admin)
    onAdminChange()
    write()
  }

  def addIgnore(admin: String): Unit = {
    ignoreset.add(admin)
    onIgnoreChange()
    write()
  }

  def addRoom(admin: String): Unit = {
    roomsset.add(admin)
    onRoomsChange()
    write()
  }


  private def onAdminChange() {
    val admins = new JSONArray()
    for(admin: String <- adminset.iterator()){
      admins.put(admin)
    }
  }

  private def onIgnoreChange() {
    val ignores = new JSONArray()
    for(ignore: String <- ignoreset.iterator()){
      ignores.put(ignore)
    }
  }

  private def onRoomsChange() {
    val rooms = new JSONArray()
    for(room: String <- roomsset.iterator()){
      rooms.put(room)
    }
  }

  private def write() {
    val writer = new FileWriter(jsonFile,false)
    writer.write(json.toString)
    writer.close()
  }
}
