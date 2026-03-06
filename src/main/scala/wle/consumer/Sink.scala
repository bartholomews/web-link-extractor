package wle.consumer

import cats.effect.Sync
import cats.effect.std.Console
import cats.syntax.all._
import fs2.Pipe
import fs2.io.file.Path
import sttp.model.Uri
import wle.domain.Hyperlink

trait Sink[F[_]] {
  def write: fs2.Pipe[F, (Uri, List[Hyperlink]), Unit]
}

object Sink {

  def toConsole[F[_]: Sync: Console]: Sink[F] = new Sink[F] {
    override def write: Pipe[F, (Uri, List[Hyperlink]), Unit] = {
      _.evalMap({ case (uri, hyperlinks) =>
        Console[F].println(uri.toString()) >> Console[F].println(
          hyperlinks.mkString("\n")
        )
      })
    }
  }

  def toPath[F[_]](path: Path): Sink[F] = new Sink[F] {
    override def write: Pipe[F, (Uri, List[Hyperlink]), Unit] = ???
  }
}
