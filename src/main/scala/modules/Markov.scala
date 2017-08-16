package modules

import com.wiiaam.MarkovGenerator
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule, Constants}

class Markov extends BotModule{

  val dbLocation = s"${Constants.MODULE_FILES_FOLDER}responses.db".replace("\\","/")

  val generator = new MarkovGenerator(dbLocation)

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.PRIVMSG){
      if(m.trailing.startsWith(m.config.getNickname + ": ") || m.trailing.startsWith(m.config.getNickname + ", ")) {
        val msg = m.trailing.substring((m.config.getNickname + ": ").length)
        generator.parseSentence(msg)
      }

      if(m.trailing.contains(" " + m.config.getNickname + " ") ||
        m.trailing.startsWith(m.config.getNickname + " ") ||
        m.trailing.startsWith(m.config.getNickname + ",") ||
        m.trailing.startsWith(m.config.getNickname + ":") ||
        m.trailing.endsWith(" " + m.config.getNickname)){
        val sentence = generator.createSentence()
        if(sentence.length > 1) r.reply(sentence)
      }
    }
  }
}
