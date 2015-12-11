package modules

import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import irc.config.UserConfig
import irc.server.ServerResponder
import ircbot.{AbstractBotModule, BotModule, BotCommand}
import org.w3c.dom.Element
import org.xml.sax.SAXException
import irc.message.Message

class WolframAlpha extends AbstractBotModule {

  override val commands: Map[String, Array[String]] = Map("wa" -> Array("Perform a Wolfram Alpha query"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder) {
    val target: String = if (!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if (b.command == "wa") {
      if (b.hasParams) {
        val search = b.paramsString.replace(" ", "%20")
        val dbf = DocumentBuilderFactory.newInstance()
        try {
          val db = dbf.newDocumentBuilder()
          val dom = db.parse(s"http://api.wolframalpha.com/v2/query?appid=${UserConfig.getJson.getString("wa_key")}&input=$search")
          val ele = dom.getDocumentElement
          if (ele.getAttribute("success") == "true") {
            val nl = ele.getElementsByTagName("pod")
            val pod1 = nl.item(1).asInstanceOf[Element]
            val subpod = pod1.getElementsByTagName("subpod").item(0).asInstanceOf[Element]
            val plaintext = subpod.getElementsByTagName("plaintext").item(0).asInstanceOf[Element]
            val result = plaintext.getTextContent
            r.say(target, "[WA] Result: " + result)
          }
        } catch {
          case e @ (_: ParserConfigurationException | _: SAXException | _: IOException) => e.printStackTrace()
        }
      }
    }
  }
}

