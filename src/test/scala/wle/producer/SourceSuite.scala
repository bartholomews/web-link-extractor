package wle.producer

import cats.effect.IO
import fs2.io.file.{Files, Path}
import munit.CatsEffectSuite
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger
import sttp.model.Uri

class SourceSuite extends CatsEffectSuite {

  implicit val logger: Logger[IO] = NoOpLogger[IO]

  private def writeFile(path: Path, lines: List[String]): IO[Unit] =
    fs2.Stream
      .emits(lines)
      .intersperse("\n")
      .through(fs2.text.utf8.encode)
      .through(Files[IO].writeAll(path))
      .compile
      .drain

  test("reads valid URLs from files") {
    Files[IO].tempDirectory.use { dir =>
      writeFile(dir / "urls.txt", List("https://example.com", "https://other.com")) >>
        Source.fromPath[IO](dir).urlStream.compile.toList.map { urls =>
          assertEquals(urls.map(_.toString), List("https://example.com", "https://other.com"))
        }
    }
  }

  test("skips blank lines") {
    Files[IO].tempDirectory.use { dir =>
      writeFile(dir / "urls.txt", List("https://example.com", "", "  ", "https://other.com")) >>
        Source.fromPath[IO](dir).urlStream.compile.toList.map { urls =>
          assertEquals(urls.map(_.toString), List("https://example.com", "https://other.com"))
        }
    }
  }

  test("skips invalid URLs without killing the stream") {
    Files[IO].tempDirectory.use { dir =>
      writeFile(dir / "urls.txt", List("https://good.com", "not a valid url %%%", "https://also-good.com")) >>
        Source.fromPath[IO](dir).urlStream.compile.toList.map { urls =>
          assert(urls.nonEmpty)
        }
    }
  }

  test("reads URLs from multiple files") {
    Files[IO].tempDirectory.use { dir =>
      writeFile(dir / "a.txt", List("https://one.com")) >>
        writeFile(dir / "b.txt", List("https://two.com")) >>
        Source.fromPath[IO](dir).urlStream.compile.toList.map { urls =>
          assertEquals(urls.map(_.toString).toSet, Set("https://one.com", "https://two.com"))
        }
    }
  }

  test("returns empty stream for empty directory") {
    Files[IO].tempDirectory.use { dir =>
      Source.fromPath[IO](dir).urlStream.compile.toList.map { urls =>
        assertEquals(urls, List.empty[Uri])
      }
    }
  }

  test("returns empty stream for file with no valid URLs") {
    Files[IO].tempDirectory.use { dir =>
      writeFile(dir / "urls.txt", List("", "  ")) >>
        Source.fromPath[IO](dir).urlStream.compile.toList.map { urls =>
          assertEquals(urls, List.empty[Uri])
        }
    }
  }

  test("unreadable file is skipped without killing the stream") {
    Files[IO].tempDirectory.use { dir =>
      // Name files so bad sorts before good, ensuring stream continues after error
      writeFile(dir / "a_bad.txt", List("https://other.com")) >>
        writeFile(dir / "b_good.txt", List("https://example.com")) >>
        IO.blocking((dir / "a_bad.txt").toNioPath.toFile.setReadable(false)) >>
        Source.fromPath[IO](dir).urlStream.compile.toList.map { urls =>
          assertEquals(urls.map(_.toString), List("https://example.com"))
        }
    }
  }
}
