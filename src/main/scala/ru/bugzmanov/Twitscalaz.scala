package ru.bugzmanov

import java.nio.file.Paths
import java.util.concurrent.Executors

import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.{RestClients, TwitterRestClient}
import fs2.{Pull, Strategy, Task}
import play.api.libs.iteratee.Execution.Implicits
//import ru.bugzmanov.Twitscalaz.twitter
import twitter4j._

import scala.concurrent.{ExecutionContext, Future}

final class FutureExtensionOps[A](x: => Future[A])( implicit strategy: Strategy) {

  def asTask: Task[A] = {
    Task.async {
      register =>
        x.onComplete {
          case scala.util.Success(v) => register(Right(v))
          case scala.util.Failure(ex) => register(Left(ex))
        }(Implicits.trampoline)
    }
  }
}

object Twitscalaz extends App {

  implicit val context = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val strategy = Strategy.fromFixedDaemonPool(4)
  implicit val R = fs2.Scheduler.fromFixedDaemonPool(2, "generator-scheduler")

  implicit val restClient = TwitterRestClient()

  def getTweets(username: String, maxId: Option[Long] = None)(implicit client: RestClients): Task[(Option[Long], Seq[Tweet])] = new FutureExtensionOps(
    client.favoriteStatusesForUser(username, count = 20, since_id = None, max_id = None).map { rd =>
      println(s"======$maxId")
      val sorted = rd.data.sortBy(_.id)
      (sorted.headOption.map(_.id), sorted.reverse)
    }).asTask


  def getTweets2(username: String, page: Int)(implicit twitter: Twitter): Task[Seq[Status]] = Task.delay {
    twitter.favorites().getFavorites("bugzmanov", new Paging(page, 20)).toArray[Status](Array[Status]()).toSeq
  }

  def tweetsStorm(username: String, page: Int = 1)(implicit twitter: Twitter): Pull[Task, Status, Unit] = for {
    res <- Pull.eval(getTweets2(username, page))
    next <- if(res.isEmpty) Pull.done else Pull.output(fs2.Chunk.seq(res)) >> tweetsStorm(username, page + 1)
    //res.fold[Pull[Task, Status, Unit]](Pull.done)(o => Pull.output(fs2.Chunk.seq(res)) >> tweetsStorm(username, page + 1))
  } yield next


  def getTweetsAll(username: String, maxId: Option[Long]): Task[Seq[Tweet]] = {
    getTweets(username, maxId).flatMap { case (id, tweets) => getTweetsAll(username, id).map(_ ++ tweets) }
  }


  def getTweetsAllStream(username: String, maxId: Option[Long]): Pull[Task, Tweet, Unit] = for {
    res <- Pull.eval(getTweets(username, maxId))
    next <- res._1.fold[Pull[Task, Tweet, Unit]](Pull.done)(o => Pull.output(fs2.Chunk.seq(res._2)) >> getTweetsAllStream(username, Some(o)))
  } yield next


  val tweets = getTweets("bugzmanov").map { case (id, tweets) =>
    tweets.foreach(t => println(t.text))
  }



//  private val log: Task[Vector[Tweet]] =

  import twitter4j.TwitterFactory

  implicit val twitter: Twitter = TwitterFactory.getSingleton

//  import scala.collection.JavaConverters._
//
//  for (i <- 1 to 2) {
//    val statuses = twitter.favorites().getFavorites("bugzmanov", new Paging(i, 20)).toArray[Status](Array[Status]())
//    statuses.toVector.foreach { t =>
//      println(s"${t.getId} = ${t.toString} \n");
//    }
//  }

  import com.google.gson.Gson

  val gson = new Gson
    import fs2._
    tweetsStorm("bugzmanov").close.take(20).map(gson.toJson).intersperse("\n-----\n")
    .through(text.utf8Encode)
    .through(fs2.io.file.writeAll(Paths.get("celsius.txt")))
    .run.unsafeRun()

//  private val sync: Vector[Tweet] = log.unsafeRun()
//  sync.foreach(t => println(s"---\n${t.text}"))
//
//  println("*****")
//  println(sync.length)
//  println("started http://localhost:8080")
}
