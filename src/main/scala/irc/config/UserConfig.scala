package irc.config

import java.io.{BufferedReader, File, FileReader, FileWriter}

import org.json.JSONObject


object UserConfig {

  private var json = new JSONObject()
  private var jsonFile = new File("")

  def load(file: File): Unit = {
    jsonFile = file
    val reader = new BufferedReader( new FileReader (file))
    var jsonString = ""
    while(reader.ready()){
      jsonString += reader.readLine() + "\n"
    }
    json = new JSONObject(jsonString)
  }

  def getJson = json

  def setJson(jsonObject: JSONObject): Unit = {
    json = jsonObject
    write()
  }

  private def write(): Unit ={
    val writer = new FileWriter(jsonFile,false)
    writer.write(json.toString)
    writer.close()
  }
}
