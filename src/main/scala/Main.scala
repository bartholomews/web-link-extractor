import cats.effect.{IO, IOApp}
import fs2.io.file.Path
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import wle.Source

object Main extends IOApp.Simple {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    Source
      .fromPath[IO](Path("src/main/resources"))
      .urlStream
      .evalTap(url => logger.info(s"TODO: process url: [$url]"))
      .compile
      .drain
}
