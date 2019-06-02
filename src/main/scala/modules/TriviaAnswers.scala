package modules

import java.io.{File, FileInputStream, FileOutputStream}
import java.util
import java.util.Properties
import java.util.regex.Pattern

import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule, Constants}

import scala.collection.mutable

class TriviaAnswers extends BotModule{

  private var answerHistory = new mutable.Queue[Boolean]

  private var playing = Array[String]()

  private val propertiesFile = new File(Constants.MODULE_FILES_FOLDER + "trivia")
  if(!propertiesFile.exists()) propertiesFile.createNewFile()

  private var questions = new Properties()
  questions.load(new FileInputStream(propertiesFile))

  private var currentQuestion = Map[String, String]()

  private val questionPattern = Pattern.compile("[0-9]+\\.\\s.*")

  private def store(): Unit ={
    questions.store(new FileOutputStream(propertiesFile), "")
  }

  private def getAnswers(question: String): Array[String] ={
    questions.getProperty(question).split("\\}\\{")
  }

  private def storeAnswer(question: String, answer: String): Unit ={
    if(questions.containsKey(question)){
      if(!questions.getProperty(question).split("\\}\\{").contains(answer))
        questions.setProperty(question, questions.getProperty(question) + "}{" + answer)
    }
    else{
      questions.setProperty(question, answer)
    }
    store()
  }

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(b.command == "playtrivia" && m.sender.isAdmin){
      playing = playing :+ m.params.first
      r.reply("Now answering trivia questions in " + m.params.first)
    }
    if(b.command == "unplaytrivia" && m.sender.isAdmin){
      playing = playing.filter(_ != m.params.first)
      r.reply("No longer answering trivia questions in " + m.params.first)
    }
    if(b.command == "triviastats" && m.sender.isAdmin){
      val history = if(b.hasParams){
        try{
          answerHistory.takeRight(Integer.parseInt(b.paramsArray(0)))
        } catch {
          case e: Exception => answerHistory
        }
      } else answerHistory
      val answered = history.count(_ == true)
      val answeredRatio = answered.toDouble / history.length.toDouble
      val percent = Math.ceil(answeredRatio*100).toInt
      val totalLogged = questions.size()
      val estimate = Math.ceil(totalLogged /  answeredRatio).toInt
      r.reply(s"Currently logged $totalLogged questions. Correctly answered $answered questions out of the last " +
        s"${history.length} questions ($percent%). Estimated trivia questions: $estimate ")
    }
    if(m.sender.nickname == "Trivia"){
      if(questionPattern.matcher(m.trailing).matches()){
        val question = m.trailing.split("\\.\\s", 2)(1)
        //if (!questions.containsKey(question))
        currentQuestion += (m.params.first -> question)
        if(playing.contains(m.params.first) && questions.containsKey(question)) {
          Thread.sleep(1000)
          getAnswers(question).foreach(r.reply)
          answerHistory.enqueue(true)
          if(answerHistory.length > 1000) answerHistory.dequeue()
        }
      }
      if(m.trailing.startsWith("Winner: ")){
        val answer = m.trailing.split(": ", 3)(2).split("; Time: ")(0)
        if(currentQuestion.contains(m.params.first)) {
          storeAnswer(currentQuestion(m.params.first), answer)
          currentQuestion = currentQuestion.filter(_._1 != m.params.first )
        }
        if(!m.trailing.contains(m.config.getNickname)){
          if(playing.contains(m.params.first))answerHistory.enqueue(false)
          if(answerHistory.length > 1000) answerHistory.dequeue()
        }
      }
      if(m.trailing.startsWith("Time's up! The answer was: ")){
        val answer = m.trailing.split("Time's up! The answer was: ")(1)
        if(currentQuestion.contains(m.params.first)) {
          storeAnswer(currentQuestion(m.params.first), answer)
          currentQuestion = currentQuestion.filter(_._1 != m.params.first )
        }
        if(playing.contains(m.params.first))answerHistory.enqueue(false)
        if(answerHistory.length > 1000) answerHistory.dequeue()
      }
      if(m.trailing.startsWith("Skipping question")) currentQuestion = currentQuestion.filter(_._1 != m.params.first )
      if(playing.contains(m.params.first) && m.trailing.startsWith("Round of trivia complete.")){
        r.reply(".trivia")
      }
    }
  }

}
