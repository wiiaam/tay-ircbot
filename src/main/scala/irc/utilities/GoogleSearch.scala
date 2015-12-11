package irc.utilities

import java.net.URL
import java.util.Scanner

import org.json.JSONObject


object GoogleSearch {
  def getAnswer(query: String): String ={
    val url = new URL("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&safe=off&q=" + query.replaceAll("\\s+","%20").replace("?","\\?").replace("\"","\\\""))
    val in = url.openStream()
    val scan = new Scanner(in)
    var jsonstring = ""
    while(scan.hasNext()){
      jsonstring += scan.next() + " "
    }
    scan.close()
    val json = new JSONObject(jsonstring)
    val responseData = json.getJSONObject("responseData")
    val result = responseData.getJSONArray("results").getJSONObject(0)
    result.getString("content").replace("<b>","").replace("</b>","").replace("&quot;","").replace(":", " ").replace("&#39;","")
  }
}
