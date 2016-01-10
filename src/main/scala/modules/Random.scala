package modules

import irc.info.Info
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class Random extends BotModule{
  override val commands: Map[String, Array[String]] = Map("slap" -> Array("Slap some sense into a user"))

  override val adminCommands: Map[String, Array[String]] = Map("triggergen2" -> Array("Trigger installgen2"),
    "banall" -> Array("Ban all users in the channel"),
    "summongary" -> Array("Prints gary in huge ASCII letters"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(b.command == "slap"){
      if(b.hasParams) r.action(target,s"slaps ${b.paramsString}")
    }


    if(m.command == MessageCommands.MODE && m.params.first == "#xmas"){
      if(m.params.array(1) == "+v") r.notice(m.params.array(2),"Don't forget to /cycle to get your new vhost!")
    }


    if(b.command == "triggergen2" && m.sender.isAdmin){
      for {
        info <- Info.get(m.server)
        channel <- info.findChannel(m.params.first)
      } yield {
        val users = channel.users
        users.foreach(user => {
          val username = user._1
          if (user._2.modes.contains("~")) {
            r.send(s"MODE ${m.params.first} +aohv $username $username $username $username")
          }
          else if (user._2.modes.contains("&")) {
            r.send(s"MODE ${m.params.first} +ohv $username $username $username")
          }
          else if (user._2.modes.contains("@")) {
            r.send(s"MODE ${m.params.first} +hv $username $username")
          }
          else if (user._2.modes.contains("%")) {
            r.send(s"MODE ${m.params.first} +v $username")
          }
        })
      }
    }

    if(b.command == "banall" && m.sender.isAdmin){
      for {
        info <- Info.get(m.server)
        channel <- info.findChannel(m.params.first)
      } yield {
        val users = channel.users
        users.foreach(user => {
          r.send(s"MODE ${m.params.first} +bb ${user._2.nickname}!*@* @${user._2.host}")
        })
      }
    }

    if(m.params.first.startsWith("#")){
      if(m.trailing.toLowerCase.trim == s"ayy ${m.config.getNickname.toLowerCase}"){
        r.pm(m.target, m.trailing.toLowerCase.replace(m.config.getNickname.toLowerCase, m.sender.nickname))
      }
    }

    if(b.command == "summongary" && m.sender.isAdmin){
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

    if(b.command == "summongen2" && m.sender.isAdmin){
      r.say(target, "        GGGGGGGGGGGGGEEEEEEEEEEEEEEEEEEEEEENNNNNNNN        NNNNNNNN 222222222222222    ")
      r.say(target, "     GGG::::::::::::GE::::::::::::::::::::EN:::::::N       N::::::N2:::::::::::::::22  ")
      r.say(target, "   GG:::::::::::::::GE::::::::::::::::::::EN::::::::N      N::::::N2::::::222222:::::2 ")
      r.say(target, "  G:::::GGGGGGGG::::GEE::::::EEEEEEEEE::::EN:::::::::N     N::::::N2222222     2:::::2 ")
      r.say(target, " G:::::G       GGGGGG  E:::::E       EEEEEEN::::::::::N    N::::::N            2:::::2 ")
      r.say(target, "G:::::G                E:::::E             N:::::::::::N   N::::::N            2:::::2 ")
      r.say(target, "G:::::G                E::::::EEEEEEEEEE   N:::::::N::::N  N::::::N         2222::::2  ")
      r.say(target, "G:::::G    GGGGGGGGGG  E:::::::::::::::E   N::::::N N::::N N::::::N    22222::::::22   ")
      r.say(target, "G:::::G    G::::::::G  E:::::::::::::::E   N::::::N  N::::N:::::::N  22::::::::222     ")
      r.say(target, "G:::::G    GGGGG::::G  E::::::EEEEEEEEEE   N::::::N   N:::::::::::N 2:::::22222        ")
      r.say(target, "G:::::G        G::::G  E:::::E             N::::::N    N::::::::::N2:::::2             ")
      r.say(target, " G:::::G       G::::G  E:::::E       EEEEEEN::::::N     N:::::::::N2:::::2             ")
      r.say(target, "  G:::::GGGGGGGG::::GEE::::::EEEEEEEE:::::EN::::::N      N::::::::N2:::::2       222222")
      r.say(target, "   GG:::::::::::::::GE::::::::::::::::::::EN::::::N       N:::::::N2::::::2222222:::::2")
      r.say(target, "     GGG::::::GGG:::GE::::::::::::::::::::EN::::::N        N::::::N2::::::::::::::::::2")
      r.say(target, "        GGGGGG   GGGGEEEEEEEEEEEEEEEEEEEEEENNNNNNNN         NNNNNNN22222222222222222222")
    }




    if(b.command == "sata" && m.sender.isAdmin){
r.say(target, "((((((((/,")
r.say(target, "((((((((((((/,")
r.say(target, "(((((((((((((((//")
r.say(target, "(((((((((((((((((((/.")
r.say(target, "(((((((((((((((((((((((/.")
r.say(target, "(((((((((((((((((((((((((((*")
r.say(target, "(((((((((((((((((((((((((((((//")
r.say(target, "(((((((((((((((((((((((((((((((((/,")
r.say(target, "(((((((((((((((((((((((((((((((((((((/.                               #%%%#*")
r.say(target, "((((((((((((((((((((((((((((((((((((((((/,         .(###(*          #%%%%%%%#%(.")
r.say(target, "(((((((((((((((((((((((((((((((((((((((((((/*   .%%&&%%%%%#(/     #%%%%%&%%%%%%&%#*")
r.say(target, "((((((((((((((((((((((((((((((((((((((((((((((%%&&&&&&&&&%%%%#((#%%%%%%%&%%&&&@&&&&%%(")
r.say(target, "((((((((((((((((((((((((((((((((((((((((((((%&&%&%&&&&&&&&&%%%&&%%%%%%%%%&&&&&%&&&@@&&&%%*")
r.say(target, "((((((((((((((((((((((((((((((((((((((((((%%&%&&&&&%%&&&&&%&&&&&&&&&%%%&@@@&%&&&&&%&&&&&&&%%(")
r.say(target, "((((((((((((((((((((((((((((((((((((((((%%%&&&&&%&&%&&&&%&&&&&&&&&&&&&&@@@&%#%%&%&&&&&&&&&&&&&%#,")
r.say(target, "(((((((((((((((((((((((((/((((((((((((%%%%%&&%%%%&%&%#&&&&&&&&&%&&&%%&&@@&&(&&&&&&&&&&&&&&&&%%%#")
r.say(target, "((((((((((((((((((((((((((((((((((((&%&&%%%%%&&&&&&&%&&&&&&&&&&%&%&&&&&%%&&&@&&&&&&&%(&&&&%%%&%&%%%#%*")
r.say(target, "(((((((((((//((((((((/((((((((((((%%&%&&&%%&&%&&&&%%&&&&&&&&&%%%&&&&&&%%%%&&&&&&&&&&&%&@@&&&&&%&&%%&&&&%%%(.")
r.say(target, "//((((((((((((((((((((((((((((((%&&%&&%&%&&&&%&%%#&&&&&&&&&%#%&&&&&&#%&&&&&&&&&&&%&@@&&&&&%&%#%%&@&&&&&&&&%%(##*")
r.say(target, "  ,///((((((((((((((((((((((((%&&%&&&%&&%%%&&&&%&&&&&&&&&%&&&&&&%#%&&&&&&&&&&&%#%&&&&%&@@&&%%%&&@@&&&&&&&&&&%&&&%(")
r.say(target, "     *//((((((((((((((((((((%&&%&&&%%%&&&&&&%#&&&&&&&&&%&&&&&&&%#%&&&&&&&&&&&%#&&&&%&&&&&&&&&%%##%%%%%&&&&&&&%(#%%%#.")
r.say(target, "        *//(((((((((((((((#&&&&&%%&&&&&&&&&(&&&&&&&&&&(%%&&&&&%&&&&&&&&&&&&&&&&&%&&&((#%%&%#(//*./#&&&%&%%%%&&%%&&%%%(")
r.say(target, "           *//(((((((((((%&&&%%&%%&&%%&&&&&&&&&&&&(&&&&&&&&((#&&&&&&&&&&&%#&&&&%&&&&(((##%%##///,.   (%%&%#%%&%%%&%%%%%%%#%")
r.say(target, "              *//(((((((&&&&&&%&%&%&&&&&&&&&&&&&&&(&&&&&&&&(&&&&&&&&&&&&&&&&&&%&&%(((##%%#((/**    ,*/((#%%&%&&%%%%%%%%(#%%#/")
r.say(target, "                 *///((&&&&&&&&&&&&&%/&&&&&&&&&&%&&&&&%%&&&&&&&&&&&&&&&&%&#((##%%##(//*    ,*/((#((%%%%%&%%%%%%#/%%%%%%.")
r.say(target, "                    */#&&&&&&&&&&&&%#&&&&&&&&&(&&&&&&&&(&%&&&&&&&&&&&&(%&&&&%%&((##%%##(//*.   .*/(((((#((/%%%&%%%%#(%%&&&&%%%%%/")
r.say(target, "                      %%%%&&&&&&&&&&&&&&&&*%%&&&&&&(%%&%&&&&&&&&&&&&&&%&&((##%%##(//*.   ,*/((#(((#((//*/&%&%#(%&&&&&&%%%%%%%%%%,")
r.say(target, "                     (####%%%&&&&&&&&&&&&&&&&&&&&&&*%%&&&&&&&&&&&&%&&&&&%%&(((##%##(//*.   .,*/(((#((((/**.  *(#&%&&@&&&&%%%%%%%&%%&%%%(")
r.say(target, "                     *((##%%%%%&&&&&&&&&&*&&&&&&%/%%&&&&&&&&&&&&%&&&&&%%&&%(((##%##((/*.    ,*/(((((((//**.  .*/(((((&&&&&&%%&&&&&%%%%%&&%%%%,")
r.say(target, "                      /((####&&&&&&&&&&%&@@@@@&&&&&&&&&&&&&&&%&&&&*&&&%(((##%%#((/*,    ,*/((####((//*,   */((((((((((%&&&&&&&&&&&%%%%&%%%%%%%(")
r.say(target, "                       *//((&&&&&&&&&%@@@@@@@@@@@&&&&&&&&&&&%%&&&%&&%(((##%%##(//,    .*/((###(((//*,   */((((((((((((((((%&&&&&&&%%%%&%%&%%&%%%%%#.")
r.say(target, "                         ./%&&&&&&&%@@@@%&%%%&@@@@&&&&&&&&%(&&&&%%&&%(((##%%##(//,.   .*/(((((#((//*,   ,//((((((((((((((((/(((#&&&&&&&%&&%%%&&%%%%%%%%#")
r.say(target, "                           /%%&&&&&&@@@&&&&&%%%&&&&&&&&&%#&&&&%%%%%(((##%%##(//*.   .,/((##(#(((/*,   ,*//(((/(((((((((//*,.*/((((#%&&&&&&&&&&&&%%%%%%#((")
r.say(target, "                           ./(##&&&&&&%%%&@@&%&&&&&&&&%#&&&&&&&((((#%%%#(//*.    ,*/((#(#(((/**.  .*/(((/(((((((((/(// .  .*//(((/*%%&&&&%%%%%%%%%%((((")
r.say(target, "                            *////(#%&&&&&&&&&&&&&&&&&%&&&&&&&&&%%&%%%%%#((/*.    ,*/(((#(((//*,.  .*//((/(((((((((((//((*,   .,*//,%%%%&%&&&&&&&&%%(##(((/")
r.say(target, "                             *//////(#%&&&&&&&&&&&&&&&&&&&&&%%&&@@@@@@@&%.   .*/(((##(((/**.   *///(((((((((((((((((/////,.***.#%&%%%%%%%%%%%%#(#&@##(((")
r.say(target, "                              ,////////(#%&&&&&&&&&&&&&&&&&&&&&&&@@@@@@@@@@&%/*/(###((((//,.   ,/(//(((((((/(((((((((///////////(%%%%%%%%%%%%%%((&@@@%#(((/")
r.say(target, "                               ,/(////////(#%&&&&&&&&&&&%&&&&&&@@@@&%#&@@@@@@@&%((#(((/**.   ,*/(((///((((((((((((/(((//////////**,*(%%#%%%%%#(&@@@@&&%#(")
r.say(target, "                                ,(/(#////////((%&&&&&&&&&@&%@@@@@%&&&&@&&&&&%&&(#(//**   .*//((/((//((((((/(((/(((////////////*,.  *%%%%%#(%&&&&&&&%#/")
r.say(target, "                                 .//((((////////((%&&&&&&&&@&@@@&&&%%%&@&%&%%(%&@@%/*,    */(((((((//((//((((((((((((/////.    ,*  ,**/&&%(#&&&%%#&%%(")
r.say(target, "                                  ,(((((((/////////((%&&%&&&&@@@@@&&&&&%#/#%%&&%&&/   ,///((((/(((((/(((((((((((((////,#%&&&&&/ ***%&&%(&@&&%%((%(.")
r.say(target, "                                     *(((/((///////////(#%&&%&&&%&@&&%(%%*%&&&&&&%&%%%%//((((/((//((/(((((/(((((((/////(&%(#%&&&&&(%&%%(%@&%%%(%%(,")
r.say(target, "                                        /((/(/////////////(%%&&&&&&&%&%(%&&&&%&%&%%&%##%@&((((((((/((//*..*/(((((/(////((#%%%&&&&&&&%(%&&&%%#((((")
r.say(target, "                                           (((//////////////((%&&&%%&%(&&&&&&&%&%%%#(#%&&@@@(((((//,.    ,*/((((////**(%&&&&&&&&(&@###&((")
r.say(target, "                                              ,(/(////(/////////(#%&&&&&&&%&%%%%%#(#%%&&&&%%&&&%#(////*    .,***/////**.  ,%&&&&&%(&@&%##((#(")
r.say(target, "                                                 /((/////(////////((#%&&%&&%%%%%(##%&&@&&&&&&&&%#%////* .,*/ (%%*,**   ,**%&&%(%@@%##((((.")
r.say(target, "                                                    /(//////(////////((#&&&%%%#(#%&&&&&&&&&&&&&&&&&&@*//*/./%&&&@ .*/*#&&#&@&%%##&(/")
r.say(target, "                                                       ((//////(////////((#%%(#%&&&&&&&%%&&&&&&&&&&%&&&&,.#%%%%%%&&&%*(%&&%#&@&%##((((")
r.say(target, "                                                          /((/////#////////((%&&&&&&&%%%&&&&&&&&&&&&&&&%%&&&&%%%%%%%#%%&&&%(&@&%#%(%%(")
r.say(target, "                                                             (((/////#////(/((#%&&&@&&%&&&&&&&&&&&&&&&&&&&%%%%%%%%##%%%%#(%@@&%%#(#(")
r.say(target, "                                                                #(/////(%////((#&&&&&@@&%&&&&&%&&&&&&&&&&&&&&%%%%#%%%%%#(&&%%(((.")
r.say(target, "                                                                   #(/////##////%&%%%&&&&@&&%%&&&&&&&&&&&&&&&&%%%%%#(&@&%%%(%%,")
r.say(target, "                                                                      #((((//(%(/%(#%%&&&&&&@@&%&&&&&&&&&&&&&&&%%%##(&@&%%%#((/")
r.say(target, "                                                                         #(((///(%%///%&&&&&&&&@@&&&&&&&&&&&&&&%%#(%&&&%%##&(")
r.say(target, "                                                                           .#(((((##/////%&&&&&&&&&@&&&&&&&&&&&&(#&&&%%#((#")
r.say(target, "                                                                               ((((#&%(/(((/#%&&&&&&&&&&&&&&&%#((%&%%%(#(")
r.say(target, "                                                                                 .((#%   ((#(//#&&&&&&&&&&&%##((((%%##&")
r.say(target, "                                                                                            (/////#&&&&&&&&%%%#(((#((.")
r.say(target, "                                                                                              .(/////#%&&%%%%#(((((/")
r.say(target, "                                                                                                 .(/////#####((((/")
r.say(target, "                                                                                                    ,(///(##((((")
r.say(target, "                                                                                                       *(/((((")
    }
  }
}
