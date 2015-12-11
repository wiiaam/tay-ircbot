package ircbot

import java.io.File
import java.net.URISyntaxException
import java.util
import out.Out

import scala.collection.JavaConversions._
import coremodules._
import irc.message.Message
import irc.server.ServerResponder

import scala.collection.mutable.ArrayBuffer

object Modules {
  var coreModules = new util.ArrayList[BotModule]()
  var modules: Set[BotModule] = Set()

  def loadCore(): Unit = {
    modules += new Ping
    modules += new CTCP
    modules += new IBIP
    modules += new Help
    modules += new InfoParser
    modules += new Admin
    modules += new ConfigUpdater
    modules += new NickServ
  }


  def parseToAllModules(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        for (module <- modules) {
          new Thread(new Runnable {
            override def run(): Unit = {
              module.parse(m, b, r)
            }
          }).start()
        }
      }
    }).start()
    new Thread(new Runnable {
      override def run(): Unit = {
        for (i <- 0 until coreModules.size()) {
          new Thread(new Runnable {
            override def run(): Unit = {
              coreModules.get(i).parse(m, b, r)
            }
          }).start()
        }
      }
    }).start()
  }

  def loadAll() {
    loadCore()
    try {
      val directory = new File(Modules.getClass.getResource("../modules/").toURI)
      if (directory.exists()) {
        val files = directory.list()
        for (i <- 0 until files.length) {
          if (files(i).endsWith(".class") && !files(i).contains("$")) {
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
    for (m <- modules){
      if(m.getClass.getSimpleName == module){
        throw new IllegalArgumentException("Module already loaded")
      }
    }
    var cl: Class[_] = null
    cl = Class.forName("modules." + module)
    val interfaces = cl.getInterfaces
    var isModule = false

    val superClass = cl.getSuperclass
    if(superClass == classOf[AbstractBotModule]) isModule = true
    
    for (j <- 0 until interfaces.length) {
      if(!isModule) {
        if (interfaces(j) == classOf[BotModule]) isModule = true
      }
    }
    if (!isModule) {
      throw new IllegalArgumentException("Class " + cl.getName + " does not implement module")
    }
    try {
      val con = cl.getConstructor()
      val toadd = con.newInstance().asInstanceOf[BotModule]
      add(toadd)
      Out.println(s"Added module: $module")
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  private def add(m: BotModule) {
    modules += m
  }

  def unload(module: String): Boolean = {
    for (m <- modules) {
      if (m.getClass.getSimpleName == module) {
        modules.remove(m)
        return true
      }
    }
    false
  }

  def getModuleStatuses: util.HashMap[String, String] = {
    val map = new util.HashMap[String, String]()
    val directory = new File(Modules.getClass.getResource("../modules/").toURI)
    if (directory.exists()) {
      val files = directory.list()
      for (i <- 0 until files.length){
        if (files(i).endsWith(".class") && !files(i).endsWith("$1.class")) {
          val className = files(i).substring(0, files(i).length - 6)
          var cl: Class[_] = null
          var isModule = false
          try {
            cl = Class.forName("modules." + className)
            val interfaces = cl.getInterfaces
            val superClass = cl.getSuperclass
            if(superClass == classOf[AbstractBotModule]) isModule = true
            for (j <- 0 until interfaces.length) {
              if (!isModule) {
                if (interfaces(j) == classOf[BotModule]) isModule = true
              }
            }
          } catch {
            case e: ClassNotFoundException => e.printStackTrace()
          }
          var found = false
          if (isModule) {
            for (m <- modules) {
              Out.println(m.getClass.getSimpleName)
              if (!found && m.getClass.getSimpleName == className) {
                map.put(className, "loaded")
                found = true
              }
            }
          }
          if (!found && isModule) map.put(className, "unloaded")
          Out.println(s"$found $isModule")
        }
      }
    }
    map
  }


}
