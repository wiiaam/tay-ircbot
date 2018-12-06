package modules

import java.sql.{Connection, DriverManager, SQLException}
import java.util

import irc.info.Info
import irc.message.Message
import irc.server.{ConnectionManager, ServerResponder}
import ircbot.{BotCommand, BotModule, Constants}
import out.Out

import scala.util.Random
//remove if not needed
import scala.collection.JavaConversions._

class Bene extends BotModule {

  private val firstSeenDelay = 30

  private val sqlUrl = s"jdbc:sqlite:${Constants.MODULE_FILES_FOLDER}money.db".replace("\\","/")

  private val lastpaid: util.HashMap[String, Long] = new util.HashMap[String, Long]()
  private val lastgrant: util.HashMap[String, Long] = new util.HashMap[String, Long]()
  private val lastTripleDip: util.HashMap[String, Long] = new util.HashMap[String, Long]()

  private val firstSeen: util.HashMap[String, Long] = new util.HashMap[String, Long]()

  private val tiedHosts: util.HashMap[String, (String, Long)] = new util.HashMap[String, (String, Long)]()

  private var topcooldown = System.currentTimeMillis() - 10000

  private var lowestBene = 100 // lowest .bene payout possible
  private var highestBene = 900 // highest .bene payout possible

  private val betChance: Double = 0.49 // chance to win for .bet

  private val normalMugChance = 0.1
  private var mugChance: Double = normalMugChance // chance to mug for .mug
  private val anarchyMugChance = 0.5

  private val tripleDipChance: Double = 0.01 // chance to win triple dip
  private val tripleDipCooldown = 3600 //seconds
  private val tripleDipCost: Long = 1000
  private val minTripleDipWinnings: Long = 50000 // triple dip winnings min
  private val maxTripleDipWinnings: Long = 150000 // triple dip winnings max

  private var checking = false //check for jail

  private var jail: util.HashMap[String, Long] = new util.HashMap[String, Long]()

  private val pros: util.HashSet[String] = new util.HashSet[String]()

  private var anarchy: Boolean = false
  // all times in seconds
  private var anarchyTimeMin = 120
  private var anarchyTimeMax = 200
  private var anarchyDelayMin = 14400
  private var anarchyDelayMax = 36000

  override val commands: Map[String, Array[String]] = Map("bene" -> Array("Ask the bruddah winz for some cash"),
    "mug" -> Array("Steal money from another user"),
    "pokies" -> Array("Give some money to the lions foundation"),
    "money" -> Array("Check if you have enough money for codys"),
    "tripledip" -> Array(s"Take a chance at getting a lot of money. Costs $$$tripleDipCost"),
    "odds" -> Array("Check the rng odds of everything money related"),
    "rank" -> Array("Shows your bene rank"),
    "top5" -> Array("Shows the top 5 beneficiaries")
  )

  val connection: Connection = try{
    // TODO add sqlite configs
    DriverManager.getConnection(sqlUrl)
  }
  catch {
    case e: SQLException =>
      e.printStackTrace()
      null
  }

  initTable()
  startAnarchyThread()

  private def isReg(m: Message): Boolean = m.sender.isRegistered || m.config.networkName == "FishNet"

  private def checkNickIsValid(hostname: String, nickname: String): Boolean = {
    if(tiedHosts.containsKey(hostname)){
      if(tiedHosts.get(hostname)._1 == nickname) return true
    }
    false
  }


  override def parse(m: Message, b: BotCommand, r: ServerResponder) {


    var target = m.params.first
    if (!m.params.first.startsWith("#")) target = m.sender.nickname

    if(!firstSeen.containsKey(m.sender.nickname)){
      firstSeen.put(m.sender.nickname, System.currentTimeMillis())
    }

    if(tiedHosts.containsKey(m.sender.host)){
      val pair = tiedHosts.get(m.sender.host)
      if(m.sender.nickname == pair._1){
        tiedHosts.put(m.sender.host, (m.sender.nickname, System.currentTimeMillis))
      } else if(pair._2 + 300000 < System.currentTimeMillis){
        tiedHosts.put(m.sender.host, (m.sender.nickname, System.currentTimeMillis))
      }
    } else {
      tiedHosts.put(m.sender.host, (m.sender.nickname, System.currentTimeMillis))
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

      if(nick == "") nick = m.sender.nickname

      r.reply(s"gibbed $gib to $nick!")

      if(parsed){
        var userbalance = if (!hasUser(nick)) 0 else getBalance(nick)
        userbalance += value
        setBalance(nick, userbalance)
      }

    }


    if(b.command == "nuke" && m.sender.isAdmin){
      if(b.hasParams){
        var nick = b.paramsArray(0).toLowerCase
        var userbalance = if (!hasUser(nick)) 0 else getBalance(nick)
        r.announce("\u0002\u000304!! WARNING NUKE INCOMING !!\u0003 \u0002One users benes will soon be completely wiped to zero!")
        Thread.sleep(10000)
        setBalance(nick, 0)
        r.announce(s"$nick has been nuked! They lost \u000303$$${userbalance}\u0003! ")
      } else {
        val userList = getUserList.filter(_._2 > 0)
        if(userList.length > 0) {
          val randomUser = userList(Random.nextInt(userList.length))
          r.announce("\u0002\u000304!! WARNING NUKE INCOMING !!\u0003 \u0002One users benes will soon be completely wiped to zero!")
          Thread.sleep(10000)
          setBalance(randomUser._1, 0)
          r.announce(s"${randomUser._1} has been nuked! They lost \u000303$$${randomUser._2}\u0003! ")
        }
      }
    }



    // bene commands -------------------------

    if(!m.target.startsWith("#")) return

    if (b.command == "jailstatus") {
      checkJail()
      if (!jail.containsKey(m.sender.nickname.toLowerCase())) {
        r.say(target, m.sender.nickname + s", ur not in jail u helmet")
        return
      } else {
        val timeleft = Math.floor((jail.get(m.sender.nickname) - (System.currentTimeMillis() - (60 * 5 * 1000))) /
          1000).toLong
        r.say(target, m.sender.nickname + s", ur in jail for another $timeleft seconds. dont drop the soap!")
      }
    }



    if (b.command == "bene") {
      if(!checkNickIsValid(m.sender.host, m.sender.nickname)) return
      if (!isReg(m)) {
        r.say(target, m.sender.nickname + ", You need to be identified with nickserv to use this command")
        return
      }
      val check = checkFirstSeen(m.sender.nickname)
      if(!check.allowed){
        if(check.timeLeft == firstSeenDelay){
          r.notice(m.sender.nickname, s"This is my first time seeing your nick. Please wait ${check.timeLeft} seconds " +
            s"before using this command")
        }
        else {
          r.notice(m.sender.nickname, s"Please wait another ${check.timeLeft} seconds before using this command")
        }
        return
      }
      var lastPaid: Long = 0l
      lastPaid = if (!lastpaid.containsKey(m.sender.nickname.toLowerCase())) 0 else lastpaid.get(m.sender.nickname.toLowerCase())
      if (lastPaid > System.currentTimeMillis() - (3600 * 1000)) {
        var minutesleft = 0
        var secondsleft = Math.floor((lastPaid - (System.currentTimeMillis() - (3600 * 1000))) /
          1000).toLong
        if (secondsleft > 60) {
          minutesleft = Math.floor(secondsleft / 60).toInt
          secondsleft = minutesleft % 60
        }
        if (minutesleft == 0) {
          r.say(target, m.sender.nickname + ", bro ur next payment is in " + secondsleft + " seconds")
        } else {
          r.say(target, m.sender.nickname + ", bro ur next payment is in " + minutesleft + " minutes")
        }
        return
      }
      var userbalance: Long = 0
      userbalance = if (!hasUser(m.sender.nickname)) 0 else getBalance(m.sender.nickname)
      val addition = Math.floor(lowestBene + Math.random()*(highestBene - lowestBene)).toLong
      userbalance += addition
      r.say(target, m.sender.nickname + s", winz just gave u \u00033$$${addition}\u0003. u now have\u00033 $$$userbalance")
      lastpaid.put(m.sender.nickname.toLowerCase(), System.currentTimeMillis())
      setBalance(m.sender.nickname, userbalance)
    }





    if(b.command == "odds"){
      r.reply("Current odds:")
      r.reply(m.sender.nickname + s", bene: random between \u00033$$${lowestBene}\u0003 and \u00033$$${highestBene}\u0003. " +
        f"Chance to mug: $mugChance%.2f. Bet chance: $betChance%.2f. Tripledip chance: $tripleDipChance%.2f")
    }





    if(b.command == "tripledip"){
      if(!checkNickIsValid(m.sender.host, m.sender.nickname)) return
      if (!isReg(m)) {
        r.say(target, m.sender.nickname + ", You need to be identified with nickserv to use this command")
        return
      }
      val check = checkFirstSeen(m.sender.nickname)
      if(!check.allowed){
        if(check.timeLeft == firstSeenDelay){
          r.notice(m.sender.nickname, s"This is my first time seeing your nick. Please wait ${check.timeLeft} seconds " +
            s"before using this command")
        }
        else {
          r.notice(m.sender.nickname, s"Please wait another ${check.timeLeft} seconds before using this command")
        }
        return
      }
      val user = m.sender.nickname
      if(getBalance(user) > tripleDipCost) {
        val lastDip = lastTripleDip.getOrDefault(user.toLowerCase(), 0)
        val timeLeftMillis = (lastDip + tripleDipCooldown*1000) - System.currentTimeMillis()
        if(timeLeftMillis < 0){
          if(Math.random() < tripleDipChance){
            val tripleDipWinnings = minTripleDipWinnings + Random.nextInt((maxTripleDipWinnings - minTripleDipWinnings).toInt)
            setBalance(user, getBalance(user) - tripleDipCost + tripleDipWinnings)
            r.reply(s"\u00038,4!WINNER!\u0003 \u00038,4!WINNER!\u0003 \u00038,4!WINNER!\u0003 $user just won the jackpot! " +
              s"\u00033$$${tripleDipWinnings}\u0003 has been awarded to them. \u00038,4!WINNER!\u0003 \u00038,4!WINNER!\u0003 \u00038,4!WINNER!\u0003")
          }
          else {
            setBalance(user, getBalance(user) - tripleDipCost)
            r.reply(m.sender.nickname + s", You bought a ticket for \u00033$$${tripleDipCost}\u0003. Unfortunately you had no luck winning this time.")
          }
          lastTripleDip.put(user.toLowerCase(), System.currentTimeMillis())
        }
        else{
          var minutesleft = 0
          var secondsleft = Math.floor(timeLeftMillis / 1000).toInt
          if (secondsleft > 60) {
            minutesleft = Math.floor(secondsleft / 60).toInt
            secondsleft = minutesleft % 60
          }
          if (minutesleft == 0) {
            r.say(target, m.sender.nickname + s", Please wait another $secondsleft seconds before trying again")
          } else {
            r.say(target, m.sender.nickname + s", Please wait another $minutesleft minutes before trying again")
          }
        }
      }
      else{
        r.reply(m.sender.nickname + s", Tickets are \u00033$$${tripleDipCost}\u0003. You don't have enough benebux for one")
      }
    }





    if(b.command == "top5"){
      val array = getTopList
      if(array.length == 0){
        r.reply("There are currently no beneficiaries")
        return
      }
      if(topcooldown > System.currentTimeMillis()){
        val wait = Math.floor((topcooldown - System.currentTimeMillis())/1000).toInt
        r.notice(m.sender.nickname, s"This command is currently on cooldown. Please wait another $wait " +
          s"seconds before using it")
        return

      }
      topcooldown = System.currentTimeMillis() + 10000

      r.notice(m.sender.nickname, "Top 5 beneficiaries:")
      for(i <- array.indices){
        val nick = array(i)._1
        val balance = array(i)._2
        r.notice(m.sender.nickname, s"${i+1}. $nick with \u00033$$$balance")
      }
    }

    if(b.command == "rank"){
      val toFind = if(b.hasParams){
        b.paramsArray(0).toLowerCase
      } else m.sender.nickname.toLowerCase
      val array = getUserList.filter(_._2 > 0)
      var found = false
      for(i <- array.indices){
        if(array(i)._1 == toFind){
          found = true
          val rank = i + 1 match{
            case 1 => "1st"
            case 2 => "2nd"
            case 3 => "3rd"
            case _ => s"${i+1}th"
          }
          val percent = Math.ceil((i.toDouble + 1) / array.length.toDouble * 100).toInt
          if(b.hasParams) r.reply(s"${m.sender.nickname}, ${b.paramsArray(0)}'s rank: $rank out of ${array.length} users. Top $percent%")
          else r.reply(s"${m.sender.nickname}, Your rank: $rank out of ${array.length} users. Top $percent%")
        }
      }
      if(!found){
        if(b.hasParams) r.reply(s"${m.sender.nickname}, Their rank could not be found")
        else r.reply(s"${m.sender.nickname}, Your rank could not be found")
      }
    }




    if (b.command == "money" || b.command == "wallet" ||
      b.command == "bank" || b.command == "balance" || b.command == "bal") {
      if(!checkNickIsValid(m.sender.host, m.sender.nickname)) return
      if (!isReg(m)) {
        r.say(target, m.sender.nickname + ", You need to be identified with nickserv to use this command")
        return
      }
      if(b.hasParams){
        val user = b.paramsArray(0)
        if(hasUser(user)){
          r.reply(m.sender.nickname + s", $user currently has \u00033$$${getBalance(user)}\u0003 in their bnz")
        }
        else r.reply(m.sender.nickname + s", sorry bro theyre with kiwibank")
        return
      }
      if (!hasUser(m.sender.nickname)) {
        r.say(target, m.sender.nickname + s", You don't have an account yet. Use " + b.commandPrefix +
          "bene to get some cash")
      } else r.say(target, m.sender.nickname + s", You currently have3 $$${getBalance(m.sender.nickname)} in the bnz")
    }





    if (b.command == "pokies" || b.command == "bet") {
      if(!checkNickIsValid(m.sender.host, m.sender.nickname)) return
      if (!isReg(m)) {
        r.say(target, m.sender.nickname + ", You need to be identified with nickserv to use this command")
        return
      }
      val check = checkFirstSeen(m.sender.nickname)
      if(!check.allowed){
        if(check.timeLeft == firstSeenDelay){
          r.notice(m.sender.nickname, s"This is my first time seeing your nick. Please wait ${check.timeLeft} seconds " +
            s"before using this command")
        }
        else {
          r.notice(m.sender.nickname, s"Please wait another ${check.timeLeft} seconds before using this command")
        }
        return
      }
      if (b.hasParams) {
        if (!hasUser(m.sender.nickname)) {
          r.say(target, m.sender.nickname + s", winz hasnt given u any money yet. Use " + b.commandPrefix +
            "bene to get some")
          return
        }
        var bet: Long = 0
        try {
          bet = java.lang.Long.parseLong(b.paramsArray(0))
        } catch {
          case e: NumberFormatException =>
            r.say(target, m.sender.nickname + s", u gotta put coins in the machine mate")
            return
        }
        if (bet < 1) {
          r.say(target, m.sender.nickname + s", stop being a poor cunt and put money in the machine")
          return
        }
        val usercash = getBalance(m.sender.nickname)
        if (usercash < bet) {
          r.say(target, m.sender.nickname + s", u dont have enough money for that mate")
          return
        }
        if (Math.random() < betChance) {
          r.say(target, m.sender.nickname + ", bro you won! wow 3$" + bet + ", thats heaps g! drinks on u ay")
          setBalance(m.sender.nickname, usercash + bet)
        } else {
          r.say(target, m.sender.nickname + ", shit man, u lost 3$" + bet + ". better not let the middy know")
          setBalance(m.sender.nickname, usercash - bet)
        }
      }
    }




    if (b.command == "mug") {
      if(!checkNickIsValid(m.sender.host, m.sender.nickname)) return
      checkJail()
      if (!isReg(m)) {
        r.say(target, m.sender.nickname + ", You need to be identified with nickserv to use this command")
        return
      }
      val check = checkFirstSeen(m.sender.nickname)
      if(!check.allowed){
        if(check.timeLeft == firstSeenDelay){
          r.notice(m.sender.nickname, s"This is my first time seeing your nick. Please wait ${check.timeLeft} seconds " +
            s"before using this command")
        }
        else {
          r.notice(m.sender.nickname, s"Please wait another ${check.timeLeft} seconds before using this command")
        }
        return
      }
      if (jail.containsKey(m.sender.nickname.toLowerCase())){
        val timeleft = Math.floor((jail.get(m.sender.nickname.toLowerCase()) - (System.currentTimeMillis() - (60 * 5 * 1000))) /
          1000).toLong
        r.say(target, m.sender.nickname + ", ur in jail for another " + timeleft + " seconds. dont drop the soap!")
        return
      }
      if (b.hasParams) {
        val tomug = b.paramsArray(0)
        if (!hasUser(m.sender.nickname)) {
          r.say(target, m.sender.nickname + s", u dont even have an account to put that in")
          return
        }
        if (!hasUser(tomug) || getBalance(tomug) < 1) {
          r.say(target, m.sender.nickname + s", they dont have any money to steal")
          return
        }
        if (pros.contains(m.sender.nickname.toLowerCase()) && isReg(m)) {
          val targetmoney = getBalance(tomug)
          val toSteal = Math.floor(Math.random() * (targetmoney / 2)).toLong
          r.say(target, s"oh shit, its the notorious ${m.sender.nickname}! $tomug ran off at the sight of them, but accidentally dropped \u00033$$${toSteal}\u0003")
          setBalance(tomug, targetmoney - toSteal)
          setBalance(m.sender.nickname, getBalance(m.sender.nickname) + toSteal)
          return
        }
        if (Math.random() > mugChance || pros.contains(tomug)) {
          jail.put(m.sender.nickname.toLowerCase(), System.currentTimeMillis())
          r.say(target, m.sender.nickname + s", \u00034,4 \u00032,2 \u00030,1POLICE\u000F\u00034,4 \u00032,2 \u000F Its the police! looks like u got caught. thats five minutes in the big house for you!")
        } else {
          val targetMoney = getBalance(tomug)

          val stealRatio = {
            var balancedTargetMoney: Double = targetMoney
            if(targetMoney > 50000) balancedTargetMoney = 50000
            else if(targetMoney < 1000) balancedTargetMoney = 1000
            0.3 - (((balancedTargetMoney - 1000) / 49000) * 0.2)
          }

          val toSteal = Math.floor(Math.random() * (targetMoney * stealRatio)).toLong
          r.say(target, m.sender.nickname + s", u manage to steal \u00033$$${toSteal}\u0003 off $tomug")
          setBalance(tomug, targetMoney - toSteal)
          setBalance(m.sender.nickname, getBalance(m.sender.nickname) + toSteal)
        }
      }
    }


    if (b.command == "give") {
      if(!checkNickIsValid(m.sender.host, m.sender.nickname)) return
      if (!isReg(m)) {
        r.say(target, m.sender.nickname + ", You need to be identified with nickserv to use this command")
        return
      }
      val check = checkFirstSeen(m.sender.nickname)
      if(!check.allowed){
        if(check.timeLeft == firstSeenDelay){
          r.notice(m.sender.nickname, s"This is my first time seeing your nick. Please wait ${check.timeLeft} seconds " +
            s"before using this command")
        }
        else {
          r.notice(m.sender.nickname, s"Please wait another ${check.timeLeft} seconds before using this command")
        }
        return
      }
      if (b.hasParams) {
        val togiveto = b.paramsArray(0)
        var togive: Long = 0
        try {
          togive = b.paramsArray(1).toLong
        } catch {
          case e: NumberFormatException =>
            r.say(target, m.sender.nickname + s", cmon man help a brother out")
            return
        }
        if (togive < 1) {
          r.say(target, m.sender.nickname + s", dont be a cheap cunt")
          return
        }
        if (!hasUser(m.sender.nickname)) {
          r.say(target, m.sender.nickname + s", u dont even have an account")
          return
        }
        if (getBalance(m.sender.nickname) < togive) {
          r.say(target, m.sender.nickname + s", u dont have enuf money bro")
          return
        }
        if (!hasUser(togiveto)) {
          r.say(target, m.sender.nickname + s", sorry bro theyre with kiwibank")
          return
        }
        setBalance(m.sender.nickname, getBalance(m.sender.nickname) - togive)
        setBalance(togiveto, getBalance(togiveto) + togive)
        r.say(target, m.sender.nickname + s", you gave $togiveto 3$$$togive")
      }
    }
  }



// =======================================================


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
      pstmt.setString(2, nick.toLowerCase())
      pstmt.executeUpdate()
    }
    catch{
      case e: SQLException => e.printStackTrace()
    }
  }

  private def getBalance(nickname: String): Long = {
    val sql = "SELECT nick, balance FROM money WHERE nick = ?"
    val pstmt = connection.prepareStatement(sql)
    pstmt.setString(1, nickname.toLowerCase())
    val rs = pstmt.executeQuery()
    rs.getLong("balance")
  }

  private def getTopList: Array[(String, Long)] = {
    val sql = "SELECT nick, balance FROM money"
    val rs = connection.createStatement().executeQuery(sql)
    var array = Array[(String, Long)]()
    var i = 0
    while(rs.next()){
      array = array :+ (rs.getString(1), rs.getLong(2))
      i += 1
    }
    array.sortWith(_._2 > _._2).take(Math.min(i, 5))
  }

  private def getUserList: Array[(String, Long)] = { //sorted top to bottom
    val sql = "SELECT nick, balance FROM money"
    val rs = connection.createStatement().executeQuery(sql)
    var array = Array[(String, Long)]()
    var i = 0
    while(rs.next()){
      array = array :+ (rs.getString(1), rs.getLong(2))
      i += 1
    }
    array.sortWith(_._2 > _._2)
  }


  private def printTable() = {
    val sql = "SELECT nick, balance FROM money"
    val rs = connection.createStatement().executeQuery(sql)
    while (rs.next()){
      Out.println(rs.getString(1) + " " + rs.getLong(2))
    }
  }
  private def initTable() = {
    try {
      val sql = "CREATE TABLE IF NOT EXISTS money(" +
        " nick TEXT PRIMARY KEY," +
        " balance LONG NOT NULL DEFAULT 0);"
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
    pstmt.setString(1, nickname.toLowerCase())
    val rs = pstmt.executeQuery()
    rs.next()
    rs.getRow > 0
  }

  private def checkJail() {
    val tempjail = jail.clone().asInstanceOf[util.HashMap[String, Long]]
    val jailItems = tempjail.keySet
    for (s <- jailItems){
      if(System.currentTimeMillis() >= (tempjail.get(s) + 300000)) jail.remove(s)
    }
  }

  private def checkFirstSeen(nick: String): CommandsAllowedCheck = {
    if (firstSeen.get(nick) > System.currentTimeMillis() - firstSeenDelay * 1000) {
      // 10 second delay before users are able to use bene commands if they are only just being seen
      CommandsAllowedCheck(allowed = false, Math.floor((firstSeen.get(nick) + firstSeenDelay * 1000 -
        System.currentTimeMillis()) / 1000).toInt + 1)
    }
    else new CommandsAllowedCheck(true, 0)
  }

  private case class CommandsAllowedCheck(allowed: Boolean, timeLeft: Int)

  private def startAnarchyThread(): Unit ={
    val responses = Array(
    "[NEWS] Pottery World is offering the best pot deal in town! ",
    "[NEWS] Pranksters just covered The Beehive in actual honey! Bees are everywhere, MSD can't get to work and beneficiaries arent getting paid!",
    "[BREAKING] Some ponyfag is stealing everyones benez!",
    "[BREAKING] Jacinda Ardern is taking a 9 month maternity leave, leaving Winston Peters as acting PM. Supergold has gone up and benez have gone down, leaving beneficiaries pissed!",
    "[NEWS] Auckland housing prices just rose! Otara beneficiaries are raiding Epsom and Remuera in search of some extra cash.",
    "[BREAKING] Parties in the streets of Dunedin as students and beneficiaries are burning couches with massive heads of steam. " +
      "Some party-goers have taken to breaking into a few houses in search of some money for more codyz and billy mavs.",
    "[NEWS] The refugee quota just increased! As more Syrian terr.. uhh refugees enter the country, payments are being cut for beneficiaries!")

    val thread = new Thread(new Runnable {
      override def run(): Unit = {
        Thread.sleep(10000)
        while(true){
          var waittime = Math.ceil(anarchyDelayMin + Math.random() * (anarchyDelayMax - anarchyDelayMin)).toInt
          Thread.sleep(waittime * 1000)

          anarchy = true
          mugChance = anarchyMugChance
          waittime = Math.ceil(anarchyTimeMin + Math.random() * (anarchyTimeMax - anarchyTimeMin)).toInt
          for((name, server) <- ConnectionManager.servers){
            val serverResponder = new ServerResponder(server, "")
            serverResponder.announce(responses(Math.floor(Math.random()*responses.length).toInt))
            serverResponder.announce(s"For the next $waittime seconds, the mugging success rate will be increased!")
          }


          Thread.sleep(waittime * 1000)

          for((name, server) <- ConnectionManager.servers){
            val serverResponder = new ServerResponder(server, "")
            serverResponder.announce("The mug rate has returned to normal.")
          }


          anarchy = false
          mugChance = normalMugChance
        }
      }
    })
    thread.setName("Anarchy")
    thread.start()
  }

}
