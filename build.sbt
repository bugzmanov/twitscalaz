import sbt._

import scalariform.formatter.preferences._

seq(Revolver.settings: _*)

/* scala versions and options */
scalaVersion := "2.12.2"

// These options will be used for *all* versions.
scalacOptions ++= Seq(
  "-deprecation"
  , "-unchecked"
  , "-encoding", "UTF-8"
  , "-Xlint"
  , "-Xverify"
  , "-feature"
  , "-language:postfixOps"
  //,"-optimise"
)

val CirceVersion = "0.7.1"

val scalazVersion = "7.2.17"

libraryDependencies ++= Seq(
  // -- config
  "com.typesafe" % "config" % "1.3.1",
  // -- testing --
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  // -- Logging --
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",

  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "com.typesafe.play" %% "play-iteratees" % "2.6.1",
  "co.fs2" %% "fs2-core" % "0.9.6",
  "co.fs2" %% "fs2-io" % "0.9.6",
  "com.spinoco" %% "fs2-http" % "0.1.8",

  "org.twitter4j" % "twitter4j-core" % "4.0.6",
  "com.google.code.gson" % "gson" % "2.8.2"
)

fork := true

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

initialCommands in console := "import scalaz._, Scalaz._"

resolvers ++= Seq(
  "TM" at "http://maven.twttr.com",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Secured Central Repository" at "https://repo1.maven.org/maven2",
  Resolver.sonatypeRepo("snapshots"),
  "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

)

// scalariform
//scalariformSettings

//ScalariformKeys.preferences := ScalariformKeys.preferences.value
//  .setPreference(AlignSingleLineCaseStatements, true)
//  .setPreference(DoubleIndentClassDeclaration, true)
//  .setPreference(IndentLocalDefs, true)
//  .setPreference(IndentPackageBlocks, true)
//  .setPreference(IndentSpaces, 2)
//  .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)

