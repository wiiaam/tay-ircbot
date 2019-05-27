package modules

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.Properties
import java.util.regex.Pattern

import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule, Constants}

class TriviaAnswers extends BotModule{

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

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(b.command == "playtrivia" && m.sender.isAdmin){
      playing = playing :+ m.params.first
      r.reply("Now answering trivia questions in " + m.params.first)
    }
    if(b.command == "unplaytrivia" && m.sender.isAdmin){
      playing = playing.filter(_ != m.params.first)
      r.reply("No longer answering trivia questions in " + m.params.first)
    }
    if(m.sender.nickname == "Trivia"){
      if(questionPattern.matcher(m.trailing).matches()){
        val question = m.trailing.split("\\.\\s", 2)(1)
        if (!questions.containsKey(question)) currentQuestion += (m.params.first -> question)
        if(playing.contains(m.params.first) && questions.containsKey(question)) {
          Thread.sleep(1000)
          r.reply(questions.getProperty(question))
        }
      }
      if(m.trailing.startsWith("Winner: ")){
        val answer = m.trailing.split(": ", 3)(2).split("; Time: ")(0)
        if(currentQuestion.contains(m.params.first)) {
          questions.setProperty(currentQuestion(m.params.first), answer)
          store()
          currentQuestion = currentQuestion.filter(_._1 != m.params.first )
        }
      }
      if(m.trailing.startsWith("Time's up! The answer was: ")){
        val answer = m.trailing.split("Time's up! The answer was: ")(1)
        if(currentQuestion.contains(m.params.first)) {
          questions.setProperty(currentQuestion(m.params.first), answer)
          store()
          currentQuestion = currentQuestion.filter(_._1 != m.params.first )
        }
      }
      if(m.trailing.startsWith("Skipping question")) currentQuestion = currentQuestion.filter(_._1 != m.params.first )
    }
  }

}
