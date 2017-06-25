name := "IrcBot"

version := "1.0"

scalaVersion := "2.11.7"

unmanagedJars in Compile ++= Seq(file( "lib/org.json-20130603.jar"), file("lib/chatter-bot-api-1.3.3.jar"), file("lib/sqlite-jdbc-3.19.3.jar"))

javacOptions ++= Seq("-encoding", "UTF-8")

scalacOptions ++= Seq("-encoding", "UTF-8")

javaOptions += "-Dfile.encoding=UTF-8 -Xmx256M"