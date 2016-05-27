package irc.utilities

import java.io.{File, FileInputStream, FileOutputStream, IOException}

object FileUtil {

  @throws(classOf[IOException])
  def copyFileUsingChannel(source: File, dest: File): Unit = {
    dest.createNewFile()
    val sourceChannel = new FileInputStream(source).getChannel
    val destChannel = new FileOutputStream(dest).getChannel
    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
    sourceChannel.close()
    destChannel.close()
  }

}
