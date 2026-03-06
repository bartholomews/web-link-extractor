package wle.consumer

import cats.effect.Sync
import cats.effect.std.Console
import fs2.Pipe
import fs2.io.file.{Files, Path}
import wle.domain.ExtractionResult

trait Sink[F[_]] {
  def write: fs2.Pipe[F, ExtractionResult, Unit]
}

object Sink {
  private def render(res: ExtractionResult): String =
    (res.uri.toString :: res.links.map(l => s"\t${l.value}")).mkString("\n")

  def toConsole[F[_]: Sync: Console]: Sink[F] = new Sink[F] {
    override def write: Pipe[F, ExtractionResult, Unit] =
      _.evalMap(result => Console[F].println(render(result) + "\n"))
  }

  def toPath[F[_]: Sync: Files](path: Path): Sink[F] = new Sink[F] {
    override def write: Pipe[F, ExtractionResult, Unit] =
      _.map(render)
        .intersperse("\n\n")
        .through(fs2.text.utf8.encode)
        .through(Files[F].writeAll(path))
  }
}
