package modules

import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class SummonGary extends Module{
  override val commands: Map[String, Array[String]] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(b.command == "summongary" && m.sender.isAdmin){
      val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

      r.say(target, "        GGGGGGGGGGGGG               AAA               RRRRRRRRRRRRRRRRR   YYYYYYY       YYYYYYY")
      r.say(target, "     GGG::::::::::::G              A:::A              R::::::::::::::::R  Y:::::Y       Y:::::Y")
      r.say(target, "   GG:::::::::::::::G             A:::::A             R::::::RRRRRR:::::R Y:::::Y       Y:::::Y")
      r.say(target, "  G:::::GGGGGGGG::::G            A:::::::A            RR:::::R     R:::::RY::::::Y     Y::::::Y")
      r.say(target, " G:::::G       GGGGGG           A:::::::::A             R::::R     R:::::RYYY:::::Y   Y:::::YYY")
      r.say(target, "G:::::G                        A:::::A:::::A            R::::R     R:::::R   Y:::::Y Y:::::Y   ")
      r.say(target, "G:::::G                       A:::::A A:::::A           R::::RRRRRR:::::R     Y:::::Y:::::Y    ")
      r.say(target, "G:::::G    GGGGGGGGGG        A:::::A   A:::::A          R:::::::::::::RR       Y:::::::::Y     ")
      r.say(target, "G:::::G    G::::::::G       A:::::A     A:::::A         R::::RRRRRR:::::R       Y:::::::Y      ")
      r.say(target, "G:::::G    GGGGG::::G      A:::::AAAAAAAAA:::::A        R::::R     R:::::R       Y:::::Y       ")
      r.say(target, "G:::::G        G::::G     A:::::::::::::::::::::A       R::::R     R:::::R       Y:::::Y       ")
      r.say(target, " G:::::G       G::::G    A:::::AAAAAAAAAAAAA:::::A      R::::R     R:::::R       Y:::::Y       ")
      r.say(target, "  G:::::GGGGGGGG::::G   A:::::A             A:::::A   RR:::::R     R:::::R       Y:::::Y       ")
      r.say(target, "   GG:::::::::::::::G  A:::::A               A:::::A  R::::::R     R:::::R    YYYY:::::YYYY    ")
      r.say(target, "     GGG::::::GGG:::G A:::::A                 A:::::A R::::::R     R:::::R    Y:::::::::::Y    ")
      r.say(target, "        GGGGGG   GGGGAAAAAAA                   AAAAAAARRRRRRRR     RRRRRRR    YYYYYYYYYYYYY   ")
    }
  }
}
