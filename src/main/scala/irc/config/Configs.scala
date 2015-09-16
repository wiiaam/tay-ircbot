package irc.config

import scala.collection.immutable.HashMap
import java.io.File


object Configs {

  private var configs = new HashMap[String, Config]
  private var userconfigs = new HashMap[String, Config]

  def load(): Unit ={
    val dir = new File(this.getClass.getResource("../../configs").toURI)
    if (dir.exists()){
      val list = dir.list()
      for(i <- 0 until list.length){
        val filename = list(i)
        if(filename.endsWith())
      }
    }
  }
}
