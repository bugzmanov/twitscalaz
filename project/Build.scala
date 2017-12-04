import sbt._
import sbt.Keys._

object TwitscalazBuild extends Build {

  lazy val twitscalaz = Project(
    id = "twitscalaz",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "twitscalaz",
      organization := "ru.bugzmanov",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.12.2"
      // add other settings here
    )
  )
}
