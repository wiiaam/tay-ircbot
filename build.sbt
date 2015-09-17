name := "IrcBot"

version := "1.0"

scalaVersion := "2.11.7"

unmanagedJars in Compile ++= Seq(file( "lib/org.json-20130603.jar"), file("org.json-20130603.jar"))
