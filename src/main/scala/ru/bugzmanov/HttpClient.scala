package ru.bugzmanov

import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import fs2._
import scodec.Attempt
import spinoco.fs2._
import spinoco.fs2.http.HttpRequest
import spinoco.protocol.http.Uri.Query
import spinoco.protocol.http.{HostPort, HttpScheme, Uri}

object HttpClient extends App {

  implicit val AG = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(5))
  implicit val strategy: Strategy = Strategy.fromFixedDaemonPool(4)
  implicit val scheduler: Scheduler = fs2.Scheduler.fromFixedDaemonPool(2, "generator-scheduler")


  private val value: Task[Attempt[String]] = http.client[Task]().flatMap { client =>
    val request = HttpRequest.get[Task](
      Uri(HttpScheme.HTTPS, HostPort("www.googleapis.com", Some(443)), Uri.Path.fromUtf8String("/youtube/v3/videos"),
        Query.apply(List(("part", "snippet,contentDetails"), ("id", "ZAZJqEKUl3U"), ("key", ""))))
    )
    client.request(request).flatMap { resp =>
      Stream.eval(resp.bodyAsString)
    }.runLog.map(_.head)
  }
  value.unsafeRun().fold(println, println)

}
