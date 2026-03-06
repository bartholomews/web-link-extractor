package wle.consumer

import munit.CatsEffectSuite
import sttp.client4.UriContext
import wle.domain.{ExtractionResult, Hyperlink, RawMarkup}

class LinkExtractorSuite extends CatsEffectSuite {

  private val testUri = uri"https://test.com"

  private def assertExtractedLinks(
      html: String,
      expectedHrefs: List[String]
  ): Unit = {
    val actual = fs2.Stream
      .emit(RawMarkup(testUri, html))
      .through(LinkExtractor.extract)
      .toList
    assertEquals(
      actual,
      List(ExtractionResult(testUri, expectedHrefs.map(Hyperlink.apply)))
    )
  }

  test("extracts href from anchor tags") {
    assertExtractedLinks(
      """<html><body>
        |<a href="https://example.com">Example</a>
        |<a href="/relative/path">Relative</a>
        |</body></html>""".stripMargin,
      expectedHrefs = List("https://example.com", "/relative/path")
    )
  }

  test("returns empty list for markup with no links") {
    assertExtractedLinks(
      "<html><body><p>No links here</p></body></html>",
      expectedHrefs = List.empty
    )
  }

  test("ignores anchor tags without href") {
    assertExtractedLinks(
      """<html><body><a name="top">Anchor</a></body></html>""",
      expectedHrefs = List.empty
    )
  }

  test("extracts links from nested structures") {
    assertExtractedLinks(
      """<html><body>
        |<div><ul>
        |  <li><a href="https://one.com">One</a></li>
        |  <li><a href="https://two.com">Two</a></li>
        |</ul></div>
        |</body></html>""".stripMargin,
      expectedHrefs = List("https://one.com", "https://two.com")
    )
  }

  test("handles empty href") {
    assertExtractedLinks(
      """<html><body><a href="">Empty</a></body></html>""",
      expectedHrefs = List("")
    )
  }

  test("extracts href from area tags") {
    assertExtractedLinks(
      """<html><body>
        |<img src="map.png" usemap="#sitemap">
        |<map name="sitemap">
        |  <area shape="rect" coords="0,0,100,50" href="https://example.com/about">
        |  <area shape="circle" coords="200,100,30" href="/contact">
        |</map>
        |</body></html>""".stripMargin,
      expectedHrefs = List("https://example.com/about", "/contact")
    )
  }

  test("extracts from both anchor and area tags") {
    assertExtractedLinks(
      """<html><body>
        |<a href="https://example.com">Example</a>
        |<map name="nav">
        |  <area shape="rect" coords="0,0,50,50" href="/map-link">
        |</map>
        |</body></html>""".stripMargin,
      expectedHrefs = List("https://example.com", "/map-link")
    )
  }

  test("handles invalid html gracefully") {
    assertExtractedLinks(
      """Not an html""",
      expectedHrefs = List.empty
    )
  }

  test("handles malformed html gracefully") {
    assertExtractedLinks(
      """<a href="https://ok.com">OK</a><p>not a link</p>""",
      expectedHrefs = List("https://ok.com")
    )
  }
}
