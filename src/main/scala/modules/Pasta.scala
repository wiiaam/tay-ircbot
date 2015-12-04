package modules

import java.io.{PrintWriter, File}
import java.util.Scanner

import irc.config.{Configs, UserConfig}
import irc.info.{Rank, Channel, Info}
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}
import out.Out


class Pasta extends Module{

  val topicfile = new File(this.getClass.getResource("files/pastatopic.txt").toURI)

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


  override val commands: Map[String, Array[String]] = Map("rules" -> Array("(RIZON ONLY) Only works in #pasta. Displays the current rules"),
  "pastatopic" -> Array("RIZON ONLY | Sets the main topic in #pasta. Requires at least SOP (+a)"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    val bAsDot = new BotCommand(m, ".")
    val bAsTilde = new BotCommand(m,"~")
    if(m.server == "rizon") {

      if(m.command == MessageCommands.PRIVMSG && m.params.first != "#pasta") checkHighlights(m, r)


      if (m.params.first == "#pasta" && (bAsDot.command == "rules" || bAsTilde.command == "rules")) {
        val rules = UserConfig.getJson.getJSONArray("pastarules")
        for (i <- 0 until rules.length()) {
          r.notice(m.sender.nickname, s"Rule $i: ${rules.getString(i)}")
        }
      }

      if (m.command == MessageCommands.TOPIC && m.params.first == "#pasta") {
        if (!m.trailing.startsWith(pastatopic + " ||")) {
          r.topic(m.params.first, pastatopic + " || " + m.trailing)
        }
      }

      if (b.command == "pastatopic") {
        val pasta: Option[Channel] = Info.get(m.server).get.findChannel("#pasta")
        if (pasta.isDefined) {
          Out.println(s"User rank: ${pasta.get.getRank(m.sender.nickname)}")
          if (pasta.get.getRank(m.sender.nickname) >= Rank.SOP) {
            if (b.hasParams) {
              Out.println("params ." + b.paramsString + ".")
              changeTopic(b.paramsString)
              r.say(target, s"${m.sender.nickname}: Topic prefix successfully set. The new prefix will be applied next time the topic changes")
            }
            else r.say(target, s"Usage: ${b.commandPrefix}pastatopic.txt <topic prefix>")
          }
          else {
            r.say(target, "You need to be at least SOP (+a) in #pasta to use this command")
          }
        }
        else r.say(target, "I am not currently in #pasta")
      }

      if (m.command == MessageCommands.MODE && m.params.first == "#pasta" && m.sender.nickname != m.config.getNickname) {
        if (m.params.array(1).startsWith("+e")) {
          r.send("MODE " + m.params.all.replace("+e", "-e"))
        }
        if (m.params.array(1).startsWith("-e")) {
          r.send("MODE " + m.params.all.replace("-e", "+e"))
        }
      }
      if (m.command == MessageCommands.KICK && m.params.first == "#pasta") {
        if(m.params.array(1) == m.config.getNickname) {
          Thread.sleep(3000)
          r.join("#pasta")
        }
      }
      if (m.trailing == "Cannot join channel (+b)") {
        if (m.params.array(1) == "#pasta") {
          r.pm("ChanServ", "UNBAN #pasta")
        }
      }
      if(m.sender.nickname.toLowerCase == "chanserv" && m.trailing.equals(s"${m.config.getNickname} has been unbanned from \u0002#pasta\u0002.")){
        r.join("#pasta")
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

  private def checkHighlights(m: Message, r: ServerResponder): Unit ={
    val checkThread = new Thread(new Runnable {
      override def run(): Unit = {
        var highlights = 0
        var massHighlight = false
        for{
          info <- Info.get(m.server)
          channel <- info.findChannel(m.params.first)
        } yield {
          for((username,user) <- channel.users) {
            if (!massHighlight) {
              if (m.trailing.contains(username)) {
                highlights += 1
                if (highlights > 10) {
                  massHighlight = true
                  r.pm("#pasta", s"Banning mass highlighter: ${m.sender.nickname} ")
                  r.ban("#pasta", "@" + m.sender.host)
                }
              }
            }
          }
        }
      }
    })
    checkThread.setName(s"Checking highlights in ${m.trailing}")
    checkThread.start()
  }
}
