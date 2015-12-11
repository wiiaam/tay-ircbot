package modules

import java.io.{FileInputStream, FileOutputStream, File}
import java.util.Properties

import irc.message.Message
import irc.server.ServerResponder
import irc.utilities.GoogleSearch
import ircbot.{BotCommand, Module}


class Trivia extends Module{
  override val commands: Map[String, Array[String]] = Map()

  var scootaloo: Properties = new Properties()
  var scootalooQuestion: Map[String, String] = Map()
  val scootalooFile = new File(this.getClass.getResource("files/trivia/scootaloo.properties").toURI)

  var delay = 10

  load()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(b.command == "triviadelay" && m.sender.isAdmin){
      if(b.hasParams)delay = b.paramsArray(0).toInt
    }

    if(m.sender.nickname == "Scootaloo" && m.trailing.startsWith("\u0002\u000304")) {
      val question = m.trailing.split("\u0002\u0003\u00034 \u0002")(1)
      scootalooQuestion += m.server + "/" + m.params.first -> question
      Thread.sleep(delay * 1000)
      if (scootalooQuestion(m.server + "/" + m.params.first) == question) {
        if (scootaloo.containsKey(question)) {
          r.pm(m.target, scootaloo.getProperty(question))
        }
      }
    }
    if(m.sender.nickname == "Scootaloo" && m.trailing.startsWith("\u0002\u00033Winner:\u0002\u0003\u00034")){
      val answer = m.trailing.split("Answer:\u0002\u0003\u00034 \u0002")(1).split("\u0002\u0003\u00033")(0)
      if(scootalooQuestion.contains(m.server + "/" + m.params.first)){
        scootaloo.put(scootalooQuestion(m.server + "/" + m.params.first), answer)
        write()
      }
    }
    if(m.sender.nickname == "Scootaloo" && m.trailing.startsWith("\u0002\u00033Time's up! The answer was:")){
      val answer = m.trailing.split("\u0002\u00033Time's up! The answer was:\u0002\u0003\u00034 \u0002")(1)
      if(scootalooQuestion.contains(m.server + "/" + m.params.first)){
        scootaloo.put(scootalooQuestion(m.server + "/" + m.params.first), answer)
        write()
      }
    }
  }

  def write(): Unit ={
    val scootalooOut = new FileOutputStream(scootalooFile)
    scootaloo.store(scootalooOut,"scootaloo trivia")
  }

  def load(): Unit ={
    val scootalooIn = new FileInputStream(scootalooFile)
    scootaloo.load(scootalooIn)
  }

}
