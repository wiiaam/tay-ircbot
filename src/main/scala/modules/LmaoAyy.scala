package modules

import irc.info.{Info, Rank}
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class LmaoAyy extends BotModule{

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.config.networkName == "Rizon"){
      if(m.command == MessageCommands.JOIN){
        if(m.trailing == "#LmaoAyy"){
          val thread = new Thread(new Runnable {
            override def run(): Unit = {
              Thread.sleep(30000)
              for{
                info <- Info.get(m.server)
                channel <- info.findChannel("#LmaoAyy")
              } yield {
                if(channel.getRank(m.sender.nickname) < Rank.VOICE){
                  r.send(s"MODE #LmaoAyy +v ${m.sender.nickname}")
                }
              }
            }
          })
          thread.setName(s"Giving voice to ${m.sender.nickname} in #LmaoAyy")
          thread.start()
        }
      }
      if(m.params.first == "#LmaoAyy"){

      }
    }
  }
}
