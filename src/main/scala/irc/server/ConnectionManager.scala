package irc.server

import irc.config.Configs


object ConnectionManager {
  def start(): Unit ={
    Configs.load()
  }
}
