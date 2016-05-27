package ircbot

import java.io._
import java.nio.channels.FileChannel
import java.nio.file.{Files, Path, Paths}


object ModuleFiles {

  @throws(classOf[FileNotFoundException])
  def getFile(filename: String): File = {
    val file = new File(Constants.MODULE_FILES_FOLDER + filename)
    if(file.exists()){
      file
    }
    else throw new FileNotFoundException()
  }

}
