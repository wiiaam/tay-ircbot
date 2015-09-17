package irc.config


import java.io.File
import java.util

import out.Out


object Configs {

  val configs: util.Map[String, Config] = new util.HashMap[String, Config]

  def load(): Unit ={
    val dir = new File(this.getClass.getResource("../../configs/").toURI)
    if (dir.exists()){
      Out.println("Config directory exists")
      val list = dir.list()
      for(i <- 0 until list.length){
        val filename = list(i)
        if(filename.endsWith(".json") && !filename.equals("example.json")){
          val file = new File(this.getClass.getResource("../../configs/" + filename).toURI)
          if(filename.equals("user.json")){
            UserConfig.load(file)
          }
          else{
            Out.println(file.getName.split("\\.")(0))
            configs.put(file.getName.split("\\.")(0), new Config(file))
          }
        }
      }
    }
    else {
      Out.println("Config directory doesn't exist")
    }
  }

  def get(name: String): Option[Config] = {
    if(configs.containsKey(name)) Some(configs.get(name))
    else None
  }

  def set(name: String, config: Config): Unit = {
    configs.put(name, config)
  }

}
