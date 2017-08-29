package irc.config

import java.io.{BufferedReader, File, FileReader, FileWriter}
import java.lang.reflect.Type

import org.json.{JSONArray, JSONObject}

import scala.reflect.ClassTag


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

  def getString(key: String): Option[String] ={
    try{
      Some(json.getString(key))
    }
    catch{
      case _: Throwable => None
    }

  }

  def getBoolean(key: String): Option[Boolean] ={
    try{
      Some(json.getBoolean(key))
    }
    catch{
      case _: Throwable => None
    }
  }

  def getDouble(key: String): Option[Double] ={
    try{
      Some(json.getDouble(key))
    }
    catch{
      case _: Throwable => None
    }
  }

  def getInt(key: String): Option[Int] ={
    try{
      Some(json.getInt(key))
    }
    catch{
      case _: Throwable => None
    }
  }

  def getLong(key: String): Option[Long] ={
    try{
      Some(json.getLong(key))
    }
    catch{
      case _: Throwable => None
    }
  }

  def getArray(key: String): Option[Array[Object]] ={
    try{
      val jsonArray = json.getJSONArray(key)
      Some(Array(""))
    }
    catch{
      case _: Throwable => None
    }
  }

  def getArrayAsType[T: ClassTag](key: String): Option[Array[T]] ={
    println(json.toString)
    try{
      val jsonArray = json.getJSONArray(key)
      var array = Array[T]()
      for(i <- 0 until jsonArray.length()){
        val next = jsonArray.get(i)
        array = array :+ next.asInstanceOf[T]
      }
      Some(array)

    }
    catch{
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  def setArray(key: String, array: Array[_ <: AnyRef]): Unit ={
    val jsonArray = new JSONArray()
    for(i: Int <- array.indices){
      jsonArray.put(array(i))
    }
    json.put(key, jsonArray)
    write()
  }

  private def write(): Unit ={
    val writer = new FileWriter(jsonFile,false)
    writer.write(json.toString(4))
    println("writing: " + json.toString)
    writer.close()
  }
}
