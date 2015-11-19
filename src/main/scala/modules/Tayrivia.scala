package modules

import java.io.File
import java.util.Scanner

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}

import scala.util.Random

class Tayrivia extends Module{
  override val commands: Map[String, Array[String]] = Map("tayrivia" -> Array("Starts a round of Taylor Swift trivia"), "strivia" -> Array("Stops a game of trivia in the channel"))

  private var questions: Map[String, String] = Map()

  loadQuestions()

  var games: Map[String, modules.TriviaGame] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(b.command == "tayrivia" && m.params.first.startsWith("#")) {
      startTrivia(m.server + "/" + m.params.first, r)
    }
    if(b.command == "strivia") {
      stopTrivia(m.server + "/" + m.params.first)
    }
    if(m.command == MessageCommands.PRIVMSG){
      games.foreach(tuple => {
        new Thread(new Runnable {
          override def run(): Unit = {
            if(tuple._1 == m.server + "/" + m.params.first)tuple._2.checkAnswer(m)
          }
        }).start()
      })
    }
  }

  private def startTrivia(serverchannel: String, r: ServerResponder): Unit ={
    games ++= Map(serverchannel -> new TriviaGame(questions, r, serverchannel.split("/")(1)))
  }

  private def stopTrivia(serverchannel: String): Unit ={
    games(serverchannel).stop()
    games = games.filterKeys(_ != serverchannel)
  }

  private def loadQuestions(): Unit ={
    val file = new File(this.getClass.getResource("files/tayrivia.txt").toURI)
    val sc = new Scanner(file)
    while(sc.hasNextLine){
      val line = sc.nextLine()
      val split = line.split("==")
      questions ++= Map(split(0) -> split(1))
    }
  }



}


class TriviaGame(questions: Map[String, String], r: ServerResponder, channel: String){
  var countdown: Thread = new Thread()
  countdown.setName(s"$channel trivia countdown")
  var currentAnswer = ""

  nextQuestion()

  def checkAnswer(message: Message): Unit ={
    if(stripPunctuation(message.trailing).contains(stripPunctuation(currentAnswer))){
      countdown.interrupt()
      r.say(channel, s"[\u0002Tayrivia\u0002] \u00032${message.sender.nickname}\u0003 got it! The answer was \u000304${currentAnswer}\u0003")
      nextQuestion()
    }
  }

  def nextQuestion(): Unit ={
    Thread.sleep(2000)
    val array = questions.keySet.toArray[String]
    val random = Random.nextInt(array.length)
    r.say(channel, s"[\u0002Tayrivia\u0002] ${array(random)}")
    currentAnswer = questions(array(random))
    autoChange()
  }

  private def stripPunctuation(string: String): String = {
    var toreturn = string.replace("'","")
    toreturn = toreturn.replace("\"","")
    toreturn = toreturn.replace(",","")
    toreturn = toreturn.toLowerCase
    toreturn
  }

  private def autoChange(): Unit ={
    try{
      countdown = new Thread(new Runnable {
        override def run(): Unit = {
          Thread.sleep(15000)
          r.say(channel, s"[\u0002Tayrivia\u0002] Time up! The answer was \u000304${currentAnswer}\u0003")
          nextQuestion()
        }
      })
      countdown.start()
    }
    catch{
      case e: java.lang.InterruptedException =>
    }
  }

  def stop(): Unit ={
    r.say(channel, "[\u0002Tayrivia\u0002] Trivia stopped. To start again, use .tayrivia")
    countdown.interrupt()
  }
}