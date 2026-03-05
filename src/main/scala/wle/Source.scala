package wle

import cats.effect.Async
import cats.syntax.all._
import fs2.io.file.{Files, Path}
import org.typelevel.log4cats.Logger
import sttp.model.Uri

trait Source[F[_]] {
  def urlStream: fs2.Stream[F, Uri]
}

object Source {
  def fromPath[F[_]: Async: Files: Logger](path: Path): Source[F] =
    new Source[F] {
      // Stream for the data in files in resources at startup
      // TODO[FB] Could also have a Watcher for new files being pushed
      //  while the producer is running
      private val files = Files[F].list(path)

      override def urlStream: fs2.Stream[F, Uri] =
        files
          .evalTap(file => Logger[F].debug(s"Processing file: [$file]"))
          .flatMap(file =>
            Files[F]
              .readUtf8Lines(file)
              .filter(_.nonEmpty)
              .evalTap(str => Logger[F].debug(str))
              .map(Uri.parse)
              .evalMap {
                case Left(err) =>
                  Logger[F]
                    .error(new Exception(err))(
                      s"Failed to decode url from file [${file.fileName}]"
                    )
                    .as(Option.empty[Uri])
                case Right(uri) => uri.some.pure[F]
              }
              .unNone
          )
      // TODO[FB] Currently handling files content errors, but for an unreadable file, the whole stream will fail
    }
}
