package modules

import java.io.{File, FileWriter, PrintWriter}

import irc.message.Message
import irc.server.ServerResponder
import irc.utilities.URLParser
import ircbot.{BotCommand, BotModule}

class CoolWebsites extends BotModule{

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(b.command == "new"){
      if(b.hasParams){
        if(b.paramsArray(0) == "website"){
          if(b.paramsArray.length > 1){
            var text = ""
            for(i <- b.paramsArray.indices){
              if(i != 0){
                text = text + b.paramsArray(i) + " "
              }
            }
            text = text.trim
            text = URLParser.makeClean(text)
            val split = text.split("\\s+")
            var filename = ""
            for(i <- split.indices){
              if(i < 3){
                var addition = split(i)
                if(addition.length > 10) addition = addition.substring(0,10)
                filename = filename + addition + "-"
              }
            }
            val title = filename.substring(0,filename.length-1)
            filename = title + ".html"
            filename = filename.replace("/","")
            val file = new File(System.getProperty("user.home") + s"${File.separator}ircsites${File.separator}$filename")
            if(!file.exists())file.createNewFile()
            val writer = new PrintWriter(new FileWriter(file))
            writer.println("<html>\n<head>")
            writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/styles.css\">")
            writer.println(s"<title>$title</title>")
            writer.println("</head>\n<body>")
            writer.println(s"<h1>$text</h1>")
            writer.println("</body>\n</html>")
            writer.close()
            r.reply(s"Website created. url: https://wiiaam.com/ircsites/$filename")
          }
        }
      }
    }

  }

}
