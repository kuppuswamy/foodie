name := """foodie"""
organization := "com.melontric"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies ++= Seq(
  ws
)
libraryDependencies += guice
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.6"
libraryDependencies += "mysql" % "mysql-connector-java" % "6.0.6"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "org.slf4j" % "slf4j-nop" % "1.7.25"
)
libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.2.1"
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"
)
libraryDependencies += "org.sangria-graphql" %% "sangria" % "1.3.2"
libraryDependencies += "org.sangria-graphql" %% "sangria-play-json" % "1.0.4"
libraryDependencies += "org.sangria-graphql" %% "sangria-relay" % "1.3.2"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.melontric.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.melontric.binders._"
