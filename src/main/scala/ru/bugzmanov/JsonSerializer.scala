package ru.bugzmanov

import com.danielasfregola.twitter4s.entities.Tweet
import org.json4s.NoTypeHints
import org.json4s.native.Serialization.{read, write => swrite}

object JsonSerializer {
  implicit val formats = org.json4s.native.Serialization.formats(NoTypeHints)

  def toJson(tweet: Tweet): String = {
    swrite(tweet)
  }

  def fromJson[A : Manifest](json: String): A = {
    read[A](json)
  }
}
