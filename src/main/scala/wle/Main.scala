package wle

import cats.effect.std.Queue
import cats.effect.{IO, IOApp, Resource}
import fs2.io.file.Path
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client4.httpclient.cats.HttpClientCatsBackend
import wle.consumer.{LinkExtractor, Sink}
import wle.domain.RawMarkup
import wle.producer.{Source, UrlFetcher}

object Main extends IOApp.Simple {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val sourcePath = Path("src/main/resources/in")
  private val sinkPath = Path("src/main/resources/out")
  private val queueCapacity: Int = 10
  private val urlFetchConcurrency: Int = 10

  override def run: IO[Unit] = {
    (for {
      backend <- HttpClientCatsBackend.resource[IO]()
      queue <- Resource.eval(Queue.bounded[IO, Option[RawMarkup]](queueCapacity))
    } yield (backend, queue)).use({ case (backend, queue) =>
      val urlFetcher = UrlFetcher.impl[IO](backend)
      val consumerSink = Sink.toPath[IO](sinkPath)
      val producer: fs2.Stream[IO, Unit] =
        Source
          .fromPath[IO](sourcePath)
          .urlStream
          .parEvalMapUnordered(urlFetchConcurrency)(urlFetcher.fetch)
          .unNone
          .evalMap(rm => queue.offer(Some(rm)))
          .onFinalize(queue.offer(None))

      val consumer: fs2.Stream[IO, Unit] =
        fs2.Stream
          .fromQueueNoneTerminated(queue)
          .through(LinkExtractor.extract)
          .through(consumerSink.write)

      producer.merge(consumer).compile.drain
    })
  }
}
