package ru.bugzmanov

import java.nio.file.{Paths, StandardOpenOption}
import java.util.concurrent.Executors

import twitter4j.{TwitterFactory, _}

import scala.concurrent.ExecutionContext

object Twitscalaz extends App {
  import fs2._

  import scala.concurrent.duration._

  private val executorService = Executors.newFixedThreadPool(2)
  implicit val context:ExecutionContext = ExecutionContext.fromExecutor(executorService)
  implicit val strategy: Strategy = Strategy.fromFixedDaemonPool(4)
  implicit val scheduler: Scheduler = fs2.Scheduler.fromFixedDaemonPool(2, "generator-scheduler")
  implicit val twitter: Twitter = TwitterFactory.getSingleton

  Stream.iterate(1)(_ + 1)
    .flatMap(page => Attempts.retry(FavouriteStream.fromTwitter("bugzmanov", page), 5.seconds, _ => 5.seconds, 1000))
    .takeWhile(_.nonEmpty)
    .flatMap(Stream.emits)
    .map(TwitterObjectFactory.getRawJSON).intersperse("\n")
    .through(text.utf8Encode)
    .through(fs2.io.file.writeAll(Paths.get("tweets.txt"), Seq(StandardOpenOption.TRUNCATE_EXISTING)))
    .run.unsafeRun()

  executorService.shutdown()

}
