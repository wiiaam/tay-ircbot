package irc.config


import java.io.File
import java.util


object Configs {

  private var configs = new util.HashMap[String, Config]
  private var userconfigs = new util.HashMap[String, Config]

  def load(): Unit ={
    val dir = new File(this.getClass.getResource("../../configs/").toURI)
    if (dir.exists()){
      val list = dir.list()
      for(i <- 0 until list.length){
        val filename = list(i)
        if(filename.endsWith(".json") && !filename.equals("example.json")){
          if(filename.startsWith("user")){
            val file = new File(this.getClass.getResource("../../configs/" + filename).toURI)
            configs.put(file.getName,new Config(file))
          }
        }
      }
    }
  }

  def get(name: String): Option[Config] = {
    if(configs.containsKey(name)) Some(configs.get(name))
    None
  }
}
