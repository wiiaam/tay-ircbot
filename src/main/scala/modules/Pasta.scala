package modules

import java.io.{File, PrintWriter}
import java.util.Scanner

import irc.config.{Configs, UserConfig}
import irc.info.{Channel, Info, Rank}
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule, ModuleFiles}
import out.Out


class Pasta extends BotModule{

  val topicfile = ModuleFiles.getFile("pastatopic.txt")

  val sc = new Scanner(topicfile)
  var pastatopic = ""
  var banned: Map[String, (String,String)] = Map()
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
    if(m.config.networkName == "Rizon") {

      if(m.params.first == "#pasta"){
        if(m.command == MessageCommands.MODE && m.sender.nickname == m.config.getNickname){

          if(m.params.array(1) == "+b"){
            if(banned.contains(m.params.array(2))){
              r.pm("#pasta", s"Banning mass highlighter: ${banned(m.params.array(2))._1} (found in ${banned(m.params.array(2))._2})")
              banned.filterKeys(_ == m.params.array(2))
            }
          }
        }

        if (bAsDot.command == "rules" || bAsTilde.command == "rules") {
          val rules = UserConfig.getJson.getJSONArray("pastarules")
          for (i <- 0 until rules.length()) {
            r.notice(m.sender.nickname, s"Rule $i: ${rules.getString(i)}")
          }
        }

        if(m.command == MessageCommands.PRIVMSG && m.trailing.toLowerCase.contains("linux") &&
          !m.trailing.toLowerCase.contains("kernel") && !m.trailing.toLowerCase.contains("steamos")){
          r.say(target, "I'd just like to interject for a moment. What you’re referring to as Linux, " +
            "is in fact, SteamOS/Linux, or as I’ve recently taken to calling it, SteamOS plus Linux. " +
            "Linux is not an operating system unto itself, but rather another free component of a fully functioning " +
            "Steam system made useful by the Valve corelibs, shell utilities and vital system components " +
            "comprising a full OS as defined by POSIX.")
        }

        /*if (m.command == MessageCommands.TOPIC ) {
          if (!m.trailing.startsWith(pastatopic + " ||")) {
            r.topic(m.params.first, pastatopic + " || " + m.trailing)
          }
        }*/

        if (m.command == MessageCommands.MODE && m.sender.nickname != m.config.getNickname) {
          if (m.params.array(1).startsWith("+e")) {
            r.send("MODE " + m.params.all.replace("+e", "-e"))
          }
          if (m.params.array(1).startsWith("-e")) {
            r.send("MODE " + m.params.all.replace("-e", "+e"))
          }
        }

        if (m.command == MessageCommands.KICK) {
          if(m.params.array(1) == m.config.getNickname) {
            Thread.sleep(3000)
            r.join("#pasta")
          }
        }

        // fun stuff
        if(m.command == MessageCommands.PRIVMSG && m.trailing.trim == "^") r.say("#pasta","can confirm")

        if(m.sender.nickname == "Pr0Wolf29" && (m.trailing.contains("meow") || m.trailing.startsWith("!neko"))){
          r.say(target, "cram it Pr0Wolf29")
        }

        if(m.trailing.startsWith("\u0001ACTION")){
          val action = m.trailing.substring("\u0001ACTION".length).replace("\u0001","").trim

          var highlight = false
          for((username, user) <- Info.get(m.server).get.findChannel(m.params.first).get.users){
            if(!highlight){
              if(action.equals(username) || action.startsWith(username + " ") || action.endsWith(" " + username) || action.contains(" " + username + " ")){
                highlight = true
              }
            }
          }
          if(!highlight){
            if(action.trim.equals("whips")){
              r.action(target, "nae naes")
            }
            else if(action.startsWith("is ")){
              r.action(target, "is also" + action.substring(2))
            }
            else if(action.split("\\s+")(0).toLowerCase.endsWith("s")){
              r.action(target, "also " + action)
            }
          }
        }

        if(m.trailing.toLowerCase.contains("kill me")){
          r.action(target, s"kills ${m.sender.nickname}")
        }

        if(m.sender.nickname == "topkek_2000" && m.trailing.contains("Suggested course of action: /kickban")){
          var name = m.trailing.split("\\s+")(0)
          name = name.substring(1,name.length-1).trim
          if(name == m.config.getNickname) return
          r.pm("ChanServ",s"BAN ${m.params.first} $name" )
        }
        else if(m.sender.nickname == "topkek_2000" && m.trailing.contains("Suggested course of action: /kick")){
          var name = m.trailing.split("\\s+")(0)
          name = name.substring(1,name.length-1).trim
          if(name == m.config.getNickname) return
          r.pm("ChanServ",s"KICK ${m.params.first} $name" )
        }

        if(m.trailing.toLowerCase.trim.equals("no u") && m.sender.nickname != "topkek_2000"){
          r.say(target, m.trailing.trim)
        }

        //var manFound = false
        //m.trailing.split("\\s+").foreach(word => {
        //  if(word.toLowerCase.equals("man") && !manFound){
        //    r.say(target, word)
        //    manFound = true
        //  }
        //})
      }



      if(m.command == MessageCommands.PRIVMSG && m.params.first != "#pasta") checkHighlights(m, r)

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

  def removeHighlighter(host: String){
    val thread = new Thread(new Runnable {
      override def run(): Unit = {
        Thread.sleep(5000)
        banned.filterKeys(_ == host)
      }
    })
    thread.setName(s"Remove $host from highlighter set")
    thread.start()
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
                if (highlights > 5) {
                  massHighlight = true
                  banned += "*!*@" + m.sender.host -> (m.sender.nickname,m.params.first)
                  r.ban("#pasta", "*!*@" + m.sender.host)
                  removeHighlighter("*!*@" + m.sender.host)
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
