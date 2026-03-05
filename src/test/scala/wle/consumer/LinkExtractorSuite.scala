package wle.consumer

import munit.CatsEffectSuite
import sttp.client4.UriContext
import sttp.model.Uri
import wle.domain.{Hyperlink, RawMarkup}

class LinkExtractorSuite extends CatsEffectSuite {

  private val testUri = uri"https://test.com"

  private def extractLinks(
      html: String
  ): fs2.Stream[cats.effect.IO, (Uri, List[Hyperlink])] =
    fs2.Stream.emit(RawMarkup(testUri, html)).through(LinkExtractor.extract)

  test("extracts href from anchor tags") {
    extractLinks(
      """<html><body>
        |<a href="https://example.com">Example</a>
        |<a href="/relative/path">Relative</a>
        |</body></html>""".stripMargin
    ).compile.lastOrError.map { case (_, links) =>
      assertEquals(
        links.map(_.href),
        List("https://example.com", "/relative/path")
      )
    }
  }

  test("returns empty list for markup with no links") {
    extractLinks(
      "<html><body><p>No links here</p></body></html>"
    ).compile.lastOrError.map { case (_, links) =>
      assertEquals(links, List.empty[Hyperlink])
    }
  }

  test("ignores anchor tags without href") {
    extractLinks(
      """<html><body><a name="top">Anchor</a></body></html>"""
    ).compile.lastOrError.map { case (_, links) =>
      assertEquals(links, List.empty[Hyperlink])
    }
  }

  test("extracts links from nested structures") {
    extractLinks(
      """<html><body>
        |<div><ul>
        |  <li><a href="https://one.com">One</a></li>
        |  <li><a href="https://two.com">Two</a></li>
        |</ul></div>
        |</body></html>""".stripMargin
    ).compile.lastOrError.map { case (_, links) =>
      assertEquals(
        links.map(_.href),
        List("https://one.com", "https://two.com")
      )
    }
  }

  test("handles empty href") {
    extractLinks(
      """<html><body><a href="">Empty</a></body></html>"""
    ).compile.lastOrError.map { case (_, links) =>
      assertEquals(links.map(_.href), List(""))
    }
  }

  test("extracts href from area tags") {
    extractLinks(
      """<html><body>
        |<img src="map.png" usemap="#sitemap">
        |<map name="sitemap">
        |  <area shape="rect" coords="0,0,100,50" href="https://example.com/about">
        |  <area shape="circle" coords="200,100,30" href="/contact">
        |</map>
        |</body></html>""".stripMargin
    ).compile.lastOrError.map { case (_, links) =>
      assertEquals(
        links.map(_.href),
        List("https://example.com/about", "/contact")
      )
    }
  }

  test("extracts from both anchor and area tags") {
    extractLinks(
      """<html><body>
        |<a href="https://example.com">Example</a>
        |<map name="nav">
        |  <area shape="rect" coords="0,0,50,50" href="/map-link">
        |</map>
        |</body></html>""".stripMargin
    ).compile.lastOrError.map { case (_, links) =>
      assertEquals(links.map(_.href), List("https://example.com", "/map-link"))
    }
  }

  test("handles invalid html gracefully") {
    extractLinks("""Not an html""").compile.lastOrError.map { case (_, links) =>
      assertEquals(links.map(_.href), List.empty)
    }
  }

  test("handles malformed html gracefully") {
    extractLinks(
      """<a href="https://ok.com">OK</a><p>not a link</p>"""
    ).compile.lastOrError.map { case (_, links) =>
      assertEquals(links.map(_.href), List("https://ok.com"))
    }
  }

  test("preserves source uri in output") {
    val uri = Uri.unsafeParse("http://source.com/page")
    fs2.Stream
      .emit(RawMarkup(uri, """<a href="/link">Link</a>"""))
      .covary[cats.effect.IO]
      .through(LinkExtractor.extract)
      .compile
      .lastOrError
      .map { case (outputUri, _) =>
        assertEquals(outputUri, uri)
      }
  }
}
