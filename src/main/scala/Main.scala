import cats.effect.std.Queue
import cats.effect.{IO, IOApp, Resource}
import fs2.io.file.Path
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client4.httpclient.cats.HttpClientCatsBackend
import wle.domain.{Hyperlink, RawMarkup}
import wle.extract.LinkExtractor
import wle.producer.{Source, UrlFetcher}

object Main extends IOApp.Simple {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  private val queueCapacity: Int = 10

  override def run: IO[Unit] = {
    (for {
      backend <- HttpClientCatsBackend.resource[IO]()
      queue <- Resource.eval(
        // TODO[FB] Not convinced on dropping urls silently here,
        //  possibly a better idea is to have a bounded queue with backpressure
        Queue.circularBuffer[IO, Option[RawMarkup]](queueCapacity)
      )
    } yield (backend, queue)).use({ case (backend, queue) =>
      val urlFetcher = UrlFetcher.impl[IO](backend)
      val producer: fs2.Stream[IO, Unit] =
        Source
          .fromPath[IO](Path("src/main/resources"))
          .urlStream
          .through(urlFetcher.fetch)
          .evalMap(rm => queue.offer(Some(rm)))
          .onFinalize(queue.offer(None))

      val consumer: fs2.Stream[IO, Hyperlink] =
        fs2.Stream
          .fromQueueNoneTerminated(queue)
          .map(LinkExtractor.extract)
          .flatMap(fs2.Stream.emits)
          .evalTap(href => logger.info(s"TODO: Process href: [$href]"))

      consumer.concurrently(producer).compile.drain
    })
  }
}
