package ircbot

import java.io.File


object Constants {
  val REPO = "https://github.com/wiiaam/tay-ircbot"
  val LOCAL_FILES_FOLDER = System.getProperty("user.home") + s"${File.separator}.taylorswift${File.separator}"
  val CONFIG_FOLDER =  LOCAL_FILES_FOLDER + s"configs${File.separator}"
  val MODULE_FILES_FOLDER = LOCAL_FILES_FOLDER + s"modulefiles${File.separator}"
}
