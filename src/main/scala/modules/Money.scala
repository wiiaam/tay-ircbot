package modules

import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.net.URISyntaxException
import java.sql.{Connection, DriverManager, ResultSet, SQLException}
import java.util
import java.util.Properties

import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule, Constants, ModuleFiles}
import irc.message.Message

import scala.util.Sorting
//remove if not needed
import scala.collection.JavaConversions._

class Money extends BotModule {

  private val sqlUrl = s"jdbc:sqlite:${Constants.MODULE_FILES_FOLDER}money.db".replace("\\","/")

  private val lastpaid: util.HashMap[String, Long] = new util.HashMap[String, Long]()
  private val lastgrant: util.HashMap[String, Long] = new util.HashMap[String, Long]()

  private val jail: util.HashMap[String, Long] = new util.HashMap[String, Long]()

  private val pros: util.HashSet[String] = new util.HashSet[String]()

  override val commands: Map[String, Array[String]] = Map("bene" -> Array("Ask the bruddah winz for some cash"),
    "mug" -> Array("Steal money from another user"),
    "pokies" -> Array("Give some money to the lions foundation"),
    "money" -> Array("Check if you have enough money for codys")
  )

  val connection = try{
    DriverManager.getConnection(sqlUrl)

  }
  catch {
    case e: SQLException =>
      e.printStackTrace()
      null
  }

  initTable()

  
  private def isReg(m: Message): Boolean = m.sender.isRegistered || m.config.networkName == "FishNet"


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
          1000).toLong
        r.say(target, "ur in jail for another " + timeleft + " seconds. dont drop the soap!")
      }
    }


    if (b.command == "bene") {
      if (!isReg(m)) {
        r.say(target, "pls login m9")
        return
      }
      var lastPaid: Long = 0l
      lastPaid = if (!lastpaid.containsKey(m.sender.nickname)) 0 else lastpaid.get(m.sender.nickname)
      if (lastPaid > System.currentTimeMillis() - (3600 * 1000)) {
        var minutesleft = 0
        var secondsleft = Math.floor((lastPaid - (System.currentTimeMillis() - (3600 * 1000))) /
          1000).toLong
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
      var userbalance: Long = 0
      userbalance = if (!hasUser(m.sender.nickname)) 0 else getBalance(m.sender.nickname)
      val addition = Math.floor(100 + Math.random()*900).toLong
      userbalance += addition
      r.say(target, s"winz just gave u \u00033$$${addition}\u0003. u now have\u00033 $$$userbalance")
      lastpaid.put(m.sender.nickname, System.currentTimeMillis())
      setBalance(m.sender.nickname, userbalance)
    }

    if(b.command == "foodgrant"){

    }

    if(b.command == "top5"){
      val array = getTopList
      if(array.length == 0){
        r.reply("There are currently no beneficiaries")
        return
      }
      r.reply("Top 5 beneficiaries:")
      for(i <- array.indices){
        val nick = array(i)._1
        val balance = array(i)._2
        val isPrivate = array(i)._3
        if(isPrivate){
          r.reply(s"${i+1}. private")
        }
        else{
          r.reply(s"${i+1}. $nick with \u00033$$$balance")
        }
      }
      r.reply("You can exclude yourself from this list by enabling privacy with .privacy")
    }

    if(b.command == "privacy"){
      if (!isReg(m)) {
        r.say(target, "pls login m9")
        return
      }
      if(hasUser(m.sender.nickname)) {
        if (b.hasParams) {
          if (b.paramsArray(0) == "enable") {
            setPrivate(m.sender.nickname, privacy = true)
            r.reply(s"${m.sender.nickname}: Privacy has been enabled")
            return
          }
          if (b.paramsArray(0) == "disable") {
            setPrivate(m.sender.nickname, privacy = false)
            r.reply(s"${m.sender.nickname}: Privacy has been disabled")
            return
          }
        }
        r.reply(s"${m.sender.nickname}: Usage: .privacy [enable|disable]")
      }
      else {
        r.reply(s"${m.sender.nickname}: you dont even have any benes to hide")
      }
    }

    if(b.command == "gib" && m.sender.isAdmin){
      var parsed = false
      var value: Long = 0
      var nick = ""
      val gib = b.paramsString.substring(b.paramsArray(0).length + 1, b.paramsString.length)
      nick = b.paramsArray(0)
      try{
        value = java.lang.Long.parseLong(gib)
        parsed = true
      }
      catch{
        case e:NumberFormatException =>
      }

      r.reply(s"gibbed $gib to $nick!")

      if(parsed){
        var userbalance = if (!hasUser(m.sender.nickname)) 0 else getBalance(m.sender.nickname)
        userbalance += value
        setBalance(if(nick != "") nick else m.sender.nickname, userbalance)
      }

    }


    if (b.command == "money" || b.command == "wallet" ||
      b.command == "bank" || b.command == "balance") {
      if (!isReg(m)) {
        r.say(target, "pls login m9")
        return
      }
      if(b.hasParams){
        val user = b.paramsArray(0)
        if(hasUser(user)){
          if(isPrivate(user)){
            r.reply("theyre currently hiding all their benez (try looking under their bed)")
          }
          else{
            r.reply(s"$user currently has \u00033$$${getBalance(user)}\u0003 in their bnz")
          }
        }
        else r.reply("sorry bro theyre with kiwibank")
        return
      }
      if (!hasUser(m.sender.nickname)) {
        r.say(target, "You don't have an account yet. Use " + b.commandPrefix +
          "bene to get some cash")
      } else r.say(target, s"You currently have3 $$${getBalance(m.sender.nickname)} in the bnz")
    }


    if (b.command == "pokies" || b.command == "bet") {
      if (!isReg(m)) {
        r.say(target, "pls register with nickserv m9")
        return
      }
      if (b.hasParams) {
        if (!hasUser(m.sender.nickname)) {
          r.say(target, "winz hasnt given u any money yet. Use " + b.commandPrefix +
            "bene to get some")
          return
        }
        var bet: Long = 0
        try {
          bet = java.lang.Long.parseLong(b.paramsArray(0))
        } catch {
          case e: NumberFormatException =>
            r.say(target, "u gotta put coins in the machine mate")
            return
        }
        if (bet < 1) {
          r.say(target, "stop being a poor cunt and put money in the machine")
          return
        }
        val usercash = getBalance(m.sender.nickname)
        if (usercash < bet) {
          r.say(target, "u dont have enough money for that mate")
          return
        }
        if (Math.random() > 0.7) {
          r.say(target, "bro you won! wow 3$" + bet + ", thats heaps g! drinks on u ay")
          setBalance(m.sender.nickname, usercash + bet)
        } else {
          r.say(target, "shit man, u lost 3$" + bet + ". better not let the middy know")
          setBalance(m.sender.nickname, usercash - bet)
        }
      }
    }


    if (b.command == "mug") {
      if (!isReg(m)) {
        r.say(target, "pls login m9")
        return
      }
      if (jail.containsKey(m.sender.nickname)){
        val timeleft = Math.floor((jail.get(m.sender.nickname) - (System.currentTimeMillis() - (60 * 5 * 1000))) /
          1000).toLong
        r.say(target, "ur in jail for another " + timeleft + " seconds. dont drop the soap!")
        return
      }
      if (b.hasParams) {
        val tomug = b.paramsArray(0)
        if (!hasUser(m.sender.nickname)) {
          r.say(target, "u dont even have an account to put that in")
          return
        }
        if (!hasUser(tomug) || getBalance(tomug) < 1) {
          r.say(target, "they dont have any money to steal")
          return
        }
        if (pros.contains(m.sender.nickname) && isReg(m)) {
          val targetmoney = getBalance(tomug)
          val tosteal = Math.floor(Math.random() * (targetmoney / 2)).toLong
          r.say(target, s"oh shit, its the notorious ${m.sender.nickname}! $tomug ran off at the sight of them, but accidentally dropped \u00033$$${tosteal}\u0003")
          setBalance(tomug, targetmoney - tosteal)
          setBalance(m.sender.nickname, getBalance(m.sender.nickname) + tosteal)
          return
        }
        if (Math.random() > 0.1 || pros.contains(tomug)) {
          jail.put(m.sender.nickname, System.currentTimeMillis())
          r.say(target, "\u00034,4 \u00032,2 \u00030,1POLICE\u000F\u00034,4 \u00032,2 \u000F Its the police! looks like u got caught. thats five minutes the big house for you!")
        } else {
          val targetmoney = getBalance(tomug)
          val tosteal = Math.floor(Math.random() * (targetmoney / 3)).toLong
          r.say(target, s"u manage to steal \u00033$$${tosteal}\u0003 off $tomug")
          setBalance(tomug, targetmoney - tosteal)
          setBalance(m.sender.nickname, getBalance(m.sender.nickname) + tosteal)
        }
      }
    }


    if (b.command == "durry") {
      if (!isReg(m)) {
        r.say(target, "pls login m9")
        return
      }
      if (!hasUser(m.sender.nickname)) {
        r.say(target, "winz hasnt given u any money yet")
        return
      }
      val usercash = getBalance(m.sender.nickname)
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
      if (!isReg(m)) {
        r.say(target, "pls login m9")
        return
      }
      if (b.hasParams) {
        val togiveto = b.paramsArray(0)
        var togive: Long = 0
        try {
          togive = b.paramsArray(1).toLong
        } catch {
          case e: NumberFormatException =>
            r.say(target, "cmon man help a brother out")
            return
        }
        if (togive < 1) {
          r.say(target, "dont be a cheap cunt")
          return
        }
        if (!hasUser(m.sender.nickname)) {
          r.say(target, "u dont even have an account")
          return
        }
        if (getBalance(m.sender.nickname) < togive) {
          r.say(target, "u dont have enuf money bro")
          return
        }
        if (!hasUser(togiveto)) {
          r.say(target, "sorry bro theyre with kiwibank")
          return
        }
        setBalance(m.sender.nickname, getBalance(m.sender.nickname) - togive)
        setBalance(togiveto, getBalance(togiveto) + togive)
        setBalance(togiveto, getBalance(togiveto) + togive)
        r.say(target, s"you gave $togiveto 3$$$togive")
      }
    }
  }

  
  private def setBalance(nick: String, amount: Long) {
    val sql = if(hasUser(nick)){
      "UPDATE money SET balance = ? WHERE nick = ? "
    }
    else{
      "INSERT INTO money(balance, nick) VALUES(?,?)"
    }
    try{
      val pstmt = connection.prepareStatement(sql)
      pstmt.setLong(1, amount)
      pstmt.setString(2, nick)
      pstmt.executeUpdate()
    }
    catch{
      case e: SQLException => e.printStackTrace()
    }
    printTable()
  }

  private def setPrivate(nick: String, privacy: Boolean) {
    val sql = if(hasUser(nick)){
      "UPDATE money SET private = ? WHERE nick = ? "
    }
    else{
      "INSERT INTO money(private, nick) VALUES(?,?)"
    }
    try{
      val pstmt = connection.prepareStatement(sql)
      pstmt.setBoolean(1, privacy)
      pstmt.setString(2, nick)
      pstmt.executeUpdate()
    }
    catch{
      case e: SQLException => e.printStackTrace()
    }
    printTable()
  }

  private def getBalance(nickname: String): Long = {
    val sql = "SELECT nick, balance FROM money WHERE nick = ?"
    val pstmt = connection.prepareStatement(sql)
    pstmt.setString(1, nickname)
    val rs = pstmt.executeQuery()
    rs.getLong("balance")
  }

  private def getTopList: Array[(String, Long, Boolean)] = {
    val sql = "SELECT nick, balance, private FROM money"
    val rs = connection.createStatement().executeQuery(sql)
    var array = Array[(String, Long, Boolean)]()
    var i = 0
    while(rs.next() && i < 5){
      array = array :+ (rs.getString(1), rs.getLong(2), rs.getBoolean(3))
      i += 1
    }
    array.sortWith(_._2 > _._2)
  }

  private def isPrivate(nickname: String): Boolean = {
    val sql = "SELECT nick, private FROM money WHERE nick = ?"
    val pstmt = connection.prepareStatement(sql)
    pstmt.setString(1, nickname)
    val rs = pstmt.executeQuery()
    rs.getBoolean("private")
  }

  private def printTable() = {
    val sql = "SELECT nick, balance, private FROM money"
    val rs = connection.createStatement().executeQuery(sql)
    while (rs.next()){
      println(rs.getString(1) + " " + rs.getLong(2) + " " + rs.getBoolean(3) )
    }
  }
  private def initTable() = {
    try {
      val sql = "CREATE TABLE IF NOT EXISTS money(" +
        " nick TEXT PRIMARY KEY," +
        " balance LONG NOT NULL DEFAULT 0," +
        " private BOOLEAN NOT NULL DEFAULT FALSE);"
      val conn = connection
      val stmt = conn.createStatement()
      stmt.execute(sql)
    }
    catch{
      case e: SQLException => e.printStackTrace()
    }
  }



  private def hasUser(nickname: String): Boolean = {
    val sql = "SELECT nick FROM money WHERE nick = ?"
    val pstmt = connection.prepareStatement(sql)
    pstmt.setString(1, nickname)
    val rs = pstmt.executeQuery()
    rs.next()
    rs.getRow > 0
  }

  private def checkJail() {
    for (s <- jail.keySet){
      if(System.currentTimeMillis() >= (jail.get(s) + 300000)) jail.remove(s)
    }
  }
}
