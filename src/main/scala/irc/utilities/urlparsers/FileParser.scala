package irc.utilities.urlparsers

import java.io.IOException
import java.net.{URL, URLConnection}
import javax.imageio.ImageIO

object FileParser {

  def find(urlc: URLConnection): String = {
    val bytes = urlc.getContentLengthLong.toDouble
    val kilobytes = bytes / 1024
    val megabytes = kilobytes / 1024
    val gigabytes = megabytes / 1024
    val terabytes = gigabytes / 1024
    val petabytes = terabytes / 1024
    val exabytes = petabytes / 1024
    val zettabytes = exabytes / 1024
    val yottabytes = zettabytes / 1024
    var filesize = ""
    if (bytes < 1000) {
      filesize = Math.round(bytes).toInt + "B"
    } else if (kilobytes < 1000) {
      filesize = Math.round(kilobytes).toInt + "KB"
    } else if (megabytes < 1000) {
      filesize = Math.round(megabytes).toInt + "MB"
    } else if (gigabytes < 1000) {
      filesize = Math.round(gigabytes).toInt + "GB"
    } else if (terabytes < 1000) {
      filesize = Math.round(terabytes).toInt + "TB"
    } else if (petabytes < 1000) {
      filesize = Math.round(petabytes).toInt + "PB"
    } else if (exabytes < 1000) {
      filesize = Math.round(exabytes).toInt + "EB"
    } else if (zettabytes < 1000) {
      filesize = Math.round(zettabytes).toInt + "ZB"
    } else if (yottabytes < 1000) {
      filesize = Math.round(yottabytes).toInt + "YB"
    }
    var `type` = "" + urlc.getContentType + ""
    println(urlc.getContentType)
    if (megabytes < 5) {
      if (urlc.getContentType.startsWith("image")) {
        try {
          val image = ImageIO.read(urlc.getURL)
          `type` += " (" + image.getWidth + " x " + image.getHeight + ")"
        } catch {
          case e: IOException =>
        }
      }
    }
    val title = String.format("%s size: %s", `type`, filesize)
    title
  }

  def find(url: String): String = {
    var URLurl: URL = null
    try {
      URLurl = new URL(url)
      val urlc = URLurl.openConnection()
      urlc.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
      urlc.addRequestProperty("User-Agent", "Mozilla")
      urlc.connect()
      find(urlc)
    } catch {
      case e: IOException => {
        e.printStackTrace()
        null
      }
    }
  }
}
