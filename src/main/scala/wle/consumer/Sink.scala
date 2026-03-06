package wle.consumer

import cats.effect.Sync
import cats.effect.std.Console
import fs2.Pipe
import fs2.io.file.Path
import wle.domain.ExtractionResult

trait Sink[F[_]] {
  def write: fs2.Pipe[F, ExtractionResult, Unit]
}

object Sink {

  def toConsole[F[_]: Sync: Console]: Sink[F] = new Sink[F] {
    override def write: Pipe[F, ExtractionResult, Unit] =
      _.evalMap(result => Console[F].println(result.render + '\n'))
  }

  def toPath[F[_]: Sync](path: Path)(implicit files: fs2.io.file.Files[F]): Sink[F] = new Sink[F] {
    override def write: Pipe[F, ExtractionResult, Unit] =
      _.map(_.render)
        .intersperse("\n\n")
        .through(fs2.text.utf8.encode)
        .through(files.writeAll(path))
  }
}
