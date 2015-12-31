import java.io.File

import irc.config.Configs
import irc.server.ConnectionManager
import irc.utilities.URLParser
import out.Out


object IrcBot {

  def main (args: Array[String]): Unit = {
    Out.println("Starting Irc Bot")
    ConnectionManager.start()
  }

}
