import java.io.File

import irc.server.ConnectionManager
import out.Out


object IrcBot {

  def main (args: Array[String]): Unit = {
    Out.println("Starting Irc Bot")
    ConnectionManager.start()

  }

  val version = "1.0"
}
