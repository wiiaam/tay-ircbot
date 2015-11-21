package irc.utilities

/**
  * Created by William on 20/11/2015.
  */
object CharStripper {
  def strip(string: String): String = {
    var toreturn = string
    toreturn = toreturn.replace("\u0001","")
    toreturn = toreturn.replace("\u0002","")
    toreturn = toreturn.replace("\u0003","")
    toreturn = toreturn.replace("\u000F","")
    toreturn = toreturn.replace("\u200B","")
    toreturn = toreturn.replace("\u001D","")
    toreturn = toreturn.replace("\u001F","")
    toreturn
  }
}
