package modules

import java.io.{PrintWriter, File}
import java.util.Scanner

import irc.config.{Configs, UserConfig}
import irc.info.{Channel, Info}
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}
import out.Out


class Pasta extends Module{

  val topicfile = new File(this.getClass.getResource("files/pastatopic").toURI)

  val sc = new Scanner(topicfile)
  var pastatopic = ""
  try {
    pastatopic = sc.nextLine()
  }
  catch {
    case e: NoSuchElementException =>
      Out.println("Pasta topic is not set in file.")
  }
  var currentTopic = ""
  sc.close()

  override val commands: Map[String, Array[String]] = Map("rules" -> Array("RIZON ONLY | Only works in #pasta. Displays the current rules"),
  "pastatopic" -> Array("RIZON ONLY | Sets the main topic in #pasta. Requires at least SOP (+a)"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    val bAsDot = new BotCommand(m, ".")
    val bAsTilde = new BotCommand(m,"~")
    if(m.server == "rizon") {
      if (m.params.first == "#pasta" && (bAsDot.command == "rules" || bAsTilde.command == "rules")) {
        val rules = UserConfig.getJson.getJSONArray("pastarules")
        for (i <- 0 until rules.length()) {
          r.notice(m.sender.nickname, s"Rule $i: ${rules.getString(i)}")
        }
      }

      if (m.command == MessageCommands.TOPIC && m.params.first == "#pasta" && false) { // TODO enable later
        if(!m.trailing.startsWith(pastatopic + " ||")){
          r.topic(m.params.first, pastatopic + " || " + m.trailing)
        }
      }

      if(b.command == "pastatopic"){
        val pasta: Option[Channel] = Info.get(m.server).get.findChannel("#pasta")
        if(pasta.isDefined){
          Out.println(s"User rank: ${pasta.get.getRank(m.sender.nickname)}")
          if(pasta.get.getRank(m.sender.nickname) >= 4){
            if(b.hasParams) {
              Out.println("params ." + b.paramsString + ".")
              changeTopic(b.paramsString)
              r.say(target, s"${m.sender.nickname}: Topic prefix successfully set. The new prefix will be applied next time the topic changes")
            }
            else r.say(target, s"Usage: ${m.config.getCommandPrefix}pastatopic <topic prefix>")
          }
          else{
            r.say(target, "You need to be at least SOP (+a) in #pasta to use this command")
          }
        }
        else r.say(target, "I am not currently in #pasta")
      }
    }
  }

  private def changeTopic(newTopic: String): Unit = {
    pastatopic = newTopic
    val writer = new PrintWriter(topicfile)
    writer.println(pastatopic)
    writer.close()
    Out.println("Changed topic prefix to " + pastatopic)
  }
}
