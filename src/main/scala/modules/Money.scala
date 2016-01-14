package modules

import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.net.URISyntaxException
import java.util
import java.util.Properties
import irc.config.Configs
import irc.server.ServerResponder
import ircbot.{BotModule, BotCommand}
import irc.message.Message
//remove if not needed
import scala.collection.JavaConversions._

class Money extends BotModule {

  private val bank: Properties = new Properties()

  private val lastpaid: util.HashMap[String, Long] = new util.HashMap[String, Long]()

  private val jail: util.HashMap[String, Long] = new util.HashMap[String, Long]()

  private val pros: util.HashSet[String] = new util.HashSet[String]()

  override val commands: Map[String, Array[String]] = Map("bene" -> Array("Ask the bruddah winz for some cash"),
  "mug" -> Array("Steal money from another user"),
  "pokies" -> Array("Give some money to the lions foundation")
  )


  try {
    bank.load(new FileInputStream(new File(this.getClass.getResource("files/money.properties").toURI)))
  } catch {
    case e @ (_: IOException | _: URISyntaxException) => e.printStackTrace()
  }


  override def parse(m: Message, b: BotCommand, r: ServerResponder) {
    var target = m.params.first
    if (!m.params.first.startsWith("#")) target = m.sender.nickname
    checkJail()
    if (b.command == "jailstatus") {
      if (!jail.containsKey(m.sender.nickname)) {
        r.say(target, "ur not in jail u helmet")
        return
      } else {
        val timeleft = Math.floor((jail.get(m.sender.nickname) - (System.currentTimeMillis() - (60 * 5 * 1000))) /
          1000).toInt
        r.say(target, "ur in jail for another " + timeleft + " seconds. dont drop the soap!")
      }
    }
    if (jail.containsKey(m.sender.nickname)){
      val timeleft = Math.floor((jail.get(m.sender.nickname) - (System.currentTimeMillis() - (60 * 5 * 1000))) /
        1000).toInt
      r.say(target, "ur in jail for another " + timeleft + " seconds. dont drop the soap!")
      return
    }
    if (b.command == "bene") {
      var lastPaid: Long = 0l
      lastPaid = if (!lastpaid.containsKey(m.sender.nickname)) 0 else lastpaid.get(m.sender.nickname)
      if (lastPaid > System.currentTimeMillis() - (3600 * 1000)) {
        var minutesleft = 0
        var secondsleft = Math.floor((lastPaid - (System.currentTimeMillis() - (3600 * 1000))) /
          1000).toInt
        if (secondsleft > 60) {
          minutesleft = Math.floor(secondsleft / 60).toInt
          secondsleft = minutesleft % 60
        }
        if (minutesleft == 0) {
          r.say(target, "bro ur next payment is in " + secondsleft + " seconds")
        } else {
          r.say(target, "bro ur next payment is in " + minutesleft + " minutes")
        }
        return
      }
      var userbalance: Double = 0.0
      userbalance = if (!bank.containsKey(m.sender.nickname)) 0 else get(m.sender.nickname)
      userbalance += 500
      r.say(target, s"winz just gave u 3$$500. u now have3 $$${"%.0f".format(userbalance)}")
      lastpaid.put(m.sender.nickname, System.currentTimeMillis())
      write(m.sender.nickname, userbalance)
    }
    if (b.command == "money" || b.command == "wallet" ||
      b.command == "bank" ||
      b.command == "balance") {
      if (!m.sender.isRegistered) {
        r.say(target, "pls login m9")
        return
      }
      if (!bank.containsKey(m.sender.nickname)) {
        r.say(target, "You don't have an account yet. Use " + b.commandPrefix +
          "bene to get some cash")
      } else r.say(target, s"You currently have3 $$${"%.0f".format(java.lang.Double.parseDouble(bank.getProperty(m.sender.nickname)))} in the bnz")
    }
    if (b.command == "pokies" || b.command == "bet") {
      if (!m.sender.isRegistered) {
        r.say(target, "pls login m9")
        return
      }
      if (b.hasParams) {
        if (!bank.containsKey(m.sender.nickname)) {
          r.say(target, "winz hasnt given u any money yet. Use " + b.commandPrefix +
            "bene to get some")
          return
        }
        var bet: Long = 0l
        try {
          bet = java.lang.Long.parseLong(b.paramsArray(0))
        } catch {
          case e: NumberFormatException => {
            r.say(target, "u gotta put coins in the machine mate")
            return
          }
        }
        if (bet < 1) {
          r.say(target, "stop being a poor cunt and put money in the machine")
          return
        }
        val usercash = java.lang.Double.parseDouble(bank.getProperty(m.sender.nickname))
        if (usercash < bet) {
          r.say(target, "u dont have enough money for that mate")
          return
        }
        if (Math.random() > 0.7) {
          r.say(target, "bro you won! wow 3$" + bet + ", thats heaps g! drinks on u ay")
          write(m.sender.nickname, usercash + bet)
        } else {
          r.say(target, "shit man, u lost 3$" + bet + ". better not let the middy know")
          write(m.sender.nickname, usercash - bet)
        }
      }
    }
    if (b.command == "mug") {
      if (!m.sender.isRegistered) {
        r.say(target, "pls login m9")
        return
      }
      if (b.hasParams) {
        val tomug = b.paramsArray(0)
        if (!bank.containsKey(m.sender.nickname)) {
          r.say(target, "u dont even have an account to put that in")
          return
        }
        if (!bank.containsKey(tomug) || get(tomug) < 1) {
          r.say(target, "they dont have any money to steal")
          return
        }
        if (pros.contains(m.sender.nickname) && m.sender.isRegistered) {
          val targetmoney = get(tomug)
          val tosteal = Math.floor(Math.random() * (targetmoney / 2))
          r.say(target, s"oh shit, its the notorious ${m.sender.nickname}! $tomug ran off at the sight of them, but accidentally dropped 3$$${"%.0f".format(tosteal)}")
          write(tomug, targetmoney - tosteal)
          write(m.sender.nickname, get(m.sender.nickname) + tosteal)
          return
        }
        if (Math.random() > 0.1 || pros.contains(tomug)) {
          jail.put(m.sender.nickname, System.currentTimeMillis())
          r.say(target, "\u00034█\u00032█\u00030,1POLICE\u000F\u00034█\u00032█\u000F Its the police! looks like u got caught. thats five minutes the big house for you!")
        } else {
          val targetmoney = get(tomug)
          val tosteal = Math.floor(Math.random() * (targetmoney / 3))
          r.say(target, s"u manage to steal 3$$${"%.0f".format(tosteal)} off $tomug")
          write(tomug, targetmoney - tosteal)
          write(m.sender.nickname, get(m.sender.nickname) + tosteal)
        }
      }
    }
    if (b.command == "durry") {
      if (m.sender.isRegistered) {
        r.say(target, "pls login m9")
        return
      }
      if (!bank.containsKey(m.sender.nickname)) {
        r.say(target, "winz hasnt given u any money yet")
        return
      }
      val usercash = get(m.sender.nickname)
      if (usercash < 10) {
        r.say(target, "u dont have enough money for that mate")
        return
      }
      r.notice(m.sender.nickname, "uve bought a durry for 3$10")
      r.say(target, "               )")
      r.say(target, "              (")
      r.say(target, " _ ___________ )")
      r.say(target, "[_[___________4#")
    }
    if (b.command == "give") {
      if (!m.sender.isRegistered) {
        r.say(target, "pls login m9")
        return
      }
      if (b.hasParams) {
        val togiveto = b.paramsArray(0)
        var togive: Double = 0.0
        try {
          togive = java.lang.Integer.parseInt(b.paramsArray(1))
        } catch {
          case e: NumberFormatException =>
            r.say(target, "cmon man help a brother out")
            return
        }
        if (togive < 1) {
          r.say(target, "dont be a cheap cunt")
          return
        }
        if (!bank.containsKey(m.sender.nickname)) {
          r.say(target, "u dont even have an account")
          return
        }
        if (get(m.sender.nickname) < togive) {
          r.say(target, "u dont have enuf money bro")
          return
        }
        write(m.sender.nickname, get(m.sender.nickname) - togive)
        write(togiveto, get(togiveto) + togive)
        r.say(target, s"you gave $togiveto 3$$${"%.0f".format(togive)}")
      }
    }
  }

  private def write(nick: String, amount: Double) {
    bank.setProperty(nick, String.valueOf(amount))
    try {
      bank.store(new FileWriter(new File(this.getClass.getResource("files/money.properties")
        .toURI)), "")
    } catch {
      case e @ (_: IOException | _: URISyntaxException) => e.printStackTrace()
    }
  }

  private def get(nickname: String): Double = {
    java.lang.Double.parseDouble(bank.getProperty(nickname))
  }

  private def checkJail() {
    for (s <- jail.keySet){
      if(System.currentTimeMillis() >= (jail.get(s) + 300000)) jail.remove(s)
    }
  }
}