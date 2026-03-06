package wle.producer

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
      override def urlStream: fs2.Stream[F, Uri] =
        Files[F]
          .list(path)
          .evalTap(file => Logger[F].debug(s"Processing file: [$file]"))
          .flatMap(file =>
            Files[F]
              .readUtf8Lines(file)
              .map(_.trim)
              .filter(_.nonEmpty)
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
              .handleErrorWith(err =>
                fs2.Stream.exec(
                  Logger[F].error(err)(s"Failed to read file [$file], skipping")
                )
              )
          )
    }
}
