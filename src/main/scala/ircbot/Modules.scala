package ircbot

import java.io.File
import java.net.URISyntaxException
import java.util
import scala.collection.JavaConversions._
import coremodules.{Help, IBIP, CTCP, Ping}
import irc.message.Message
import irc.server.ServerResponder

object Modules {
  var coreModules = new util.ArrayList[Module]()
  var modules: util.ArrayList[Module] = new util.ArrayList[Module]()

  def loadCore(): Unit ={
    modules.add(new Ping)
    modules.add(new CTCP)
    modules.add(new IBIP)
    modules.add(new Help)
  }



  def parseToAllModules(m: Message, b: BotCommand, r: ServerResponder): Unit ={
    new Thread(new Runnable {
      override def run(): Unit = {
        for(i <- 0 until modules.size()){
          new Thread(new Runnable {
            override def run(): Unit = {
              modules.get(i).parse(m,b,r)
            }
          }).start()
        }
      }
    }).start()
    new Thread(new Runnable {
      override def run(): Unit = {
        for(i <- 0 until coreModules.size()){
          new Thread(new Runnable {
            override def run(): Unit = {
              coreModules.get(i).parse(m,b,r)
            }
          }).start()
        }
      }
    }).start()
  }

  def loadAll() {
    loadCore()
    try {
      val directory = new File(Modules.getClass.getResource("../modules/").toURI())
      if (directory.exists()) {
        val files = directory.list()
        for (i <- 0 until files.length){
          if(files(i).endsWith(".class") && !files(i).endsWith("$1.class")) {
            val className: String = files(i).substring(0, files(i).length - 6)
            new Thread(new Runnable() {
              def run() {
                try {
                  load(className)
                } catch {
                  case e@(_: ClassNotFoundException | _: IllegalArgumentException) => e.printStackTrace()
                }
              }
            }).start()
          }
        }
      }
    } catch {
      case e: URISyntaxException => e.printStackTrace()
    }
  }

  def reload(module: String): Boolean = {
    if (!unload(module)) return false
    try {
      load(module)
    } catch {
      case e: ClassNotFoundException => return false
    }
    true
  }

  def load(module: String) {
    for (i <- 0 until modules.size if modules.get(i).getClass.getSimpleName == module) {
      throw new IllegalArgumentException("Module already loaded")
    }
    var cl: Class[_] = null
    cl = Class.forName("modules." + module)
    val interfaces = cl.getInterfaces
    var isModule = false
    for (i <- 0 until interfaces.length if interfaces(i) == classOf[Module]) isModule = true
    if (!isModule) {
      throw new IllegalArgumentException("Class " + cl.getName + " does not implement module")
    }
    try {
      val con = cl.getConstructor()
      val toadd = con.newInstance().asInstanceOf[Module]
      add(toadd)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  private def add(m: Module) {
    val modulesloaded = new util.ArrayList[Module](modules)
    for (module <- modulesloaded if m.getClass.getSimpleName == module.getClass.getSimpleName) return
    modules.add(m)
  }

  def unload(module: String): Boolean = {
    for (i <- 0 until modules.size if modules.get(i).getClass.getSimpleName == module) {
      modules.remove(i)
      return true
    }
    false
  }

  def getModuleStatuses: util.HashMap[String, String] = {
    val map = new util.HashMap[String, String]()
    val directory = new File(Modules.getClass.getResource("../modules/").toURI)
    if (directory.exists()) {
      val files = directory.list()
      for (i <- 0 until files.length if files(i).endsWith(".class") && !files(i).endsWith("$1.class")) {
        val className = files(i).substring(0, files(i).length - 6)
        var cl: Class[_] = null
        var isModule = false
        try {
          cl = Class.forName("modules." + className)
          val interfaces = cl.getInterfaces
          for (j <- 0 until interfaces.length if interfaces(j) == classOf[Module]) isModule = true
        } catch {
          case e: ClassNotFoundException => e.printStackTrace()
        }
        var found = false
        if(isModule) {
          for (j <- 0 until modules.size){
            if(!found && modules.get(j).getClass.getSimpleName == className){
              map.put(className, "loaded")
              found = true
            }
          }
        }
        if (!found && isModule) map.put(className, "unloaded")
      }
    }
    map
  }
}
