import cats.effect.{IO, IOApp}
import fs2.io.file.Path
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client4.httpclient.cats.HttpClientCatsBackend
import wle.producer.{Source, UrlFetcher}

object Main extends IOApp.Simple {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = (for {
    backend <- HttpClientCatsBackend.resource[IO]()
  } yield UrlFetcher.impl[IO](backend)).use(urlFetcher =>
    Source
      .fromPath[IO](Path("src/main/resources"))
      .urlStream
      .through(urlFetcher.extractMarkup)
      .evalTap(markup => logger.info(s"TODO: process markup: [$markup]"))
      .compile
      .drain
  )
}
