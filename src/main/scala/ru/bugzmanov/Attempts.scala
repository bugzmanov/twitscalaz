package ru.bugzmanov

import fs2.util.Async
import fs2.{Scheduler, Stream, time}

import scala.concurrent.duration.FiniteDuration

// mostly copy-pasted from fs2 0.10M
object Attempts {

  def retry[F[_], A](fa: F[A],
                     delay: FiniteDuration,
                     nextDelay: FiniteDuration => FiniteDuration,
                     maxRetries: Int,
                     retriable: Throwable => Boolean = _ => true)(
                      implicit F: Async[F], S: Scheduler): Stream[F, A] = {
    val delays = Stream.unfold(delay)(d => Some(d -> nextDelay(d))).covary[F]

    attempts(Stream.eval(fa), delays)
      .take(maxRetries)
      .takeThrough(_.fold(err => retriable(err), _ => false))
      .last
      .map(_.getOrElse(sys.error("[fs2] impossible: empty stream in retry")))
      .flatMap {
        case Left(e) => Stream.fail(e)
        case Right(e) => Stream.emit(e)
      }
  }

  def attempts[F[_], A](s: Stream[F, A], delays: Stream[F, FiniteDuration])(
    implicit F: Async[F], S: Scheduler): Stream[F, Either[Throwable, A]] =
    s.attempt ++ delays.flatMap(delay => time.sleep_(delay) ++ s.attempt)
}
