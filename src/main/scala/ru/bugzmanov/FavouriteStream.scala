package ru.bugzmanov

import fs2.{Pull, Task}
import twitter4j.{Paging, Status, Twitter}

object FavouriteStream {

  def fromTwitter(username: String, page: Int, maxPerPage: Int = 20)(implicit twitter: Twitter): Task[Seq[Status]] = Task.delay {
    twitter.favorites().getFavorites(username, new Paging(page, maxPerPage)).toArray[Status](Array[Status]()).toSeq
  }

  def fromTwitter1(username: String, page: Int = 1)(implicit twitter: Twitter): Pull[Task, Status, Unit] = {
    for {
      res <- Pull.eval(fromTwitter(username, page))
      next <- if (res.isEmpty) Pull.done else Pull.output(fs2.Chunk.seq(res)) >> fromTwitter1(username, page + 1)
    } yield next
  }
}
