name := """reactive-buzz"""

version := "1.0"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test",
   "org.json4s" %% "json4s-native" % "3.2.11",
   "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test")
  