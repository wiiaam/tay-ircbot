package irc.utilities.urlparsers

import java.io.IOException
import irc.config.UserConfig
import irc.utilities.URLParser
import org.json.JSONObject

object SteamParser {

  def find(steamID: String): String = {
    var id = steamID
    if (id.contains("/profiles/")) {
      id = id.split(".*/profiles/")(1).split("/")(0)
    }
    if (id.toLowerCase.contains("/id/")) {
      var vanityurl: String = null
      try {
        vanityurl = id.split(".*/id/")(1).split("/")(0)
        val url = "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" +
          UserConfig.getJson.getString("steamapikey") +
          "&vanityurl=" +
          vanityurl
        val jsonstring = URLParser.readUrl(url)

        val json = new JSONObject(jsonstring)
        val response = json.getJSONObject("response")
        if (response.getInt("success") == 1) {
          id = response.getString("steamid")
        } else {
          throw new ParserException
        }
      } catch {
        case e @ (_: ArrayIndexOutOfBoundsException | _: IOException) => return "Invalid steam vanity ID"
      }
    }
    var title = ""
    var friends = ""
    var onlinestatus = ""
    try {
      var url = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" +
        UserConfig.getJson.getString("steamapikey") +
        "&steamids=" +
        id
      var jsonstring = URLParser.readUrl(url)
      var json = new JSONObject(jsonstring)
      val response = json.getJSONObject("response")
      val players = response.getJSONArray("players")
      if (players.length < 1) {
        throw new ParserException
      }
      val player = players.getJSONObject(0)
      if (player.getInt("profilestate") != 1) {
        title == "This user has not set up their community profile yet"
      } else if (player.getInt("communityvisibilitystate") == 1) {
        val personaname = player.getString("personaname")
        val profileurl = player.getString("profileurl")
        title = String.format("%s | %s", personaname, profileurl)
        onlinestatus = "4Private"
      } else {
        val personaname = player.getString("personaname")
        var info = ""
        if (player.has("realname")) info += player.getString("realname") + " "
        if (player.has("loccountrycode")) info += "| Location: " + player.getString("loccountrycode") +
          " "
        val personastate = player.getInt("personastate")
        personastate match {
          case 0 => onlinestatus = "14offline"
          case 1 => onlinestatus = "12Online"
          case 2 => onlinestatus = "12Busy"
          case 3 => onlinestatus = "12Away"
          case 4 => onlinestatus = "12Snooze"
          case 5 => onlinestatus = "12Looking to Trade"
          case 6 => onlinestatus = "12Looking to Play"
        }
        if (player.has("gameextrainfo")) {
          onlinestatus = "3In game: " + player.getString("gameextrainfo") +
            ""
        }
        title = if (info == "") String.format("Steam: %s | %s ", personaname, onlinestatus) else String.format("Steam: %s | %s| %s ",
          personaname, info, onlinestatus)
      }
      url = "http://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=" +
        UserConfig.getJson.getString("steamapikey") +
        "&steamid=" +
        id +
        "&relationship=friend"
      jsonstring = URLParser.readUrl(url)
      json = new JSONObject(jsonstring)
      val friendslist = json.getJSONObject("friendslist").getJSONArray("friends")
      friends = s"${friendslist.length()} friends"
      title += "| " + friends
    } catch {
      case e: IOException =>
        throw new ParserException
    }
    title
  }
}


