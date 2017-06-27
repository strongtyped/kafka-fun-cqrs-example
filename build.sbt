
organization := "io.strongtyped"
name := "kafka-fun-cqrs-example"

scalaVersion := "2.11.11"

val funCQRSVersion = "1.0.0-M2" 

val akkaDeps = {
  val version = "2.4.17"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % version,
    "com.typesafe.akka" %% "akka-stream" % version
  )
}

val kafkaDeps = {

  val version = "0.10.1.0"
  val scalaClientVersion = "0.8.0"

  Seq(
    "org.apache.kafka" %% "kafka" % version exclude("log4j", "log4j") exclude("org.slf4j", "slf4j-log4j12"),
    "org.apache.kafka" % "kafka-clients" % version,
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"
  )
}

val main =
Project(
  id = "akka-kafka-scala", base = file(".")
)
.settings(libraryDependencies ++= akkaDeps)
.settings(libraryDependencies ++= kafkaDeps)
.settings(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9")
.settings(libraryDependencies += "io.strongtyped" %% "fun-cqrs-core" % funCQRSVersion)
.settings(libraryDependencies += "io.strongtyped" %% "fun-cqrs-akka" % funCQRSVersion)
