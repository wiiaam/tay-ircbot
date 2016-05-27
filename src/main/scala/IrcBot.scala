import java.io.File
import java.nio.file.{Files, Paths}

import irc.config.Configs
import irc.server.ConnectionManager
import irc.utilities.{FileUtil, URLParser}
import ircbot._
import out.Out


object IrcBot {

  def main (args: Array[String]): Unit = {
    Out.println("Starting Irc Bot")
    if(!Files.exists(Paths.get(Constants.LOCAL_FILES_FOLDER))) {
      Out.println("Module files do not exist yet, creating new files")
      new File(Constants.LOCAL_FILES_FOLDER).mkdirs()
    }

    if(!Files.exists(Paths.get(Constants.CONFIG_FOLDER))){
      new File(Constants.CONFIG_FOLDER).mkdirs()
      FileUtil.copyFileUsingChannel(new File(this.getClass.getResource("configs/example.json").toURI), new File(Constants.CONFIG_FOLDER + "example.json"))
      FileUtil.copyFileUsingChannel(new File(this.getClass.getResource("configs/user.json").toURI), new File(Constants.CONFIG_FOLDER + "user.json"))
      Out.println(s"Generated new config files in ${Constants.CONFIG_FOLDER}")
      Out.println("Create a copy of the example json file to create a new server config")
    }

    if(!Files.exists(Paths.get(Constants.MODULE_FILES_FOLDER))){
      new File(Constants.MODULE_FILES_FOLDER).mkdirs()
      for(file <- new File(this.getClass.getResource("modules/files/").toURI).listFiles()){
        FileUtil.copyFileUsingChannel(file, new File(Constants.MODULE_FILES_FOLDER + file.getName))
      }
    }
    ConnectionManager.start()
  }

}
