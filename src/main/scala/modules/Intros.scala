package modules

import java.io._
import java.net.URISyntaxException
import java.util.Scanner
import irc.config.Configs
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}
import org.json.{JSONArray, JSONObject}
import irc.message.{MessageCommands, Message}
import scala.collection.JavaConversions._

class Intros extends BotModule {


  private var jsonfile: File = _
  private var json: JSONObject = _
  private var intros: JSONObject = _

  override val commands: Map[String, Array[String]] = Map("intros" -> Array("Set an intro for a channel.",
  "%pintros add <intro> will add a new intro", "%pintros del <intro number> will delete an intro",
    "%pintros list will list all intros set"))

  try {
    jsonfile = new File(this.getClass.getResource("files/intros.json").toURI)
    var scan = new Scanner(new FileInputStream(jsonfile))
    var jsonstring = ""
    while (scan.hasNext) {
      jsonstring += scan.next() + " "
    }
    scan.close()
    json = new JSONObject(jsonstring)
    intros = json.getJSONObject("intros")
  } catch {
    case e: IOException => e.printStackTrace()
    case e: URISyntaxException => e.printStackTrace()
  }

  override def parse(m: Message, b: BotCommand, r: ServerResponder) {
    if(m.config.networkName != "Rizon") return
    val target: String = if (!m.params.first.startsWith("#")) m.sender.nickname else m.params.first
    if (m.params.first.startsWith("#") && m.command == MessageCommands.PRIVMSG) {
      if (b.command == "intros" || b.command == "intro") {
        if (m.sender.isRegistered) {
          if (b.hasParams) {
            if (b.paramsArray(0) == "list") { // List all intros
              if (intros.has(m.params.first)) {
                if (intros.getJSONObject(m.params.first).has(m.sender.nickname)) {
                  val introarray = intros.getJSONObject(m.params.first).getJSONArray(m.sender.nickname)
                  r.notice(m.sender.nickname, "You have " + introarray.length() + " intros set for " +
                    m.params.first)
                  for (i <- 0 until introarray.length()) {
                    r.notice(m.sender.nickname, "Intro " + (i + 1) + ": " + introarray.getString(i))
                  }
                } else {
                  r.notice(m.sender.nickname, "You do not have any intros set for " + m.params.first)
                }
              } else {
                r.notice(m.sender.nickname, "You do not have any intros set for " + m.params.first)
              }
            } else if (b.paramsArray(0) == "add") { // Add an intro
              if (b.paramsArray.length > 1) {
                var channelintros: JSONObject = null
                if (!intros.has(m.params.first)) {
                  channelintros = new JSONObject()
                  intros.put(m.params.first, channelintros)
                } else {
                  channelintros = intros.getJSONObject(m.params.first)
                }
                var userintros: JSONArray = null
                if (!channelintros.has(m.sender.nickname)) {
                  userintros = new JSONArray()
                  channelintros.put(m.sender.nickname, userintros)
                } else {
                  userintros = channelintros.getJSONArray(m.sender.nickname)
                }
                var intro = ""
                for (i <- 1 until b.paramsArray.length) {
                  intro += b.paramsArray(i) + " "
                }
                intro = intro.trim().replaceAll("\n", "").replaceAll("\r", "")
                if (userintros.length() == 10) {
                  r.say(target, s"${m.sender.nickname}: Sorry, you have already set the max number of intros. Use " +
                    s"${b.commandPrefix}intros del <intro> to remove some.")
                } else {
                  userintros.put(intro)
                  r.say(target, m.sender.nickname + ": Intro added. You now have " + userintros.length() +
                    " intros set.")
                  save()
                }
              } else {
                r.say(target, m.sender.nickname + ": Usage: " + b.commandPrefix + "intros add <intro>")
              }
            } else if (b.paramsArray(0) == "del") { // Delete an intro
              if (b.paramsArray.length > 1) {
                var num: Int = 0
                try {
                  num = java.lang.Integer.parseInt(b.paramsArray(1))
                } catch {
                  case e: NumberFormatException => {
                    r.say(target, m.sender.nickname + ": Please provide a valid intro number.")
                    return
                  }
                }
                if (intros.has(m.params.first)) {
                  if (intros.getJSONObject(m.params.first).has(m.sender.nickname)) {
                    val userintros = intros.getJSONObject(m.params.first).getJSONArray(m.sender.nickname)
                    if (userintros.length() < num) {
                      r.say(target, m.sender.nickname + ": Intro " + num + " does not exist")
                    } else {
                      userintros.remove(num - 1)
                      if (userintros.length() == 0) {
                        intros.getJSONObject(m.params.first).remove(m.sender.nickname)
                      }
                      save()
                      r.say(target, m.sender.nickname + ": Intro removed. You now have " + userintros.length() +
                        " intros set")
                    }
                  } else {
                    r.say(target, m.sender.nickname + ": Intro " + num + " does not exist")
                  }
                } else {
                  r.say(target, m.sender.nickname + ": Intro " + num + " does not exist")
                }
              } else {
                r.say(target, m.sender.nickname + ": Usage: " + b.commandPrefix + "intros del <num>")
              }
            }
          } else {
            r.say(target,  m.sender.nickname + ": Usage: " + b.commandPrefix + "intros <add | list | del>")
          }
        } else {
          r.say(target, m.sender.nickname +
            ": You must be a registered user to use this command")
        }
      }
    }
    if (m.command == MessageCommands.JOIN) {
      println(m.trailing + " " + intros.has(m.params.first))
      if (intros.has(m.trailing)) {
        if (intros.getJSONObject(m.trailing).has(m.sender.nickname)) {
          val userintros = intros.getJSONObject(m.trailing).getJSONArray(m.sender.nickname)
          val rand = Math.floor(Math.random() * userintros.length()).toInt
          r.say(m.trailing, "​" + userintros.getString(rand))
        }
      }
    }
  }

  private def save() {
    try {
      val writer = new PrintWriter(jsonfile)
      writer.println(json.toString)
      writer.close()
    } catch {
      case e: FileNotFoundException => e.printStackTrace()
    }
  }
}
