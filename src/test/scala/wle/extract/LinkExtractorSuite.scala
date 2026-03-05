package wle.extract

import munit.CatsEffectSuite
import wle.domain.{Hyperlink, Markup}

class LinkExtractorSuite extends CatsEffectSuite {

  test("extracts href from anchor tags") {
    val html = Markup(
      """<html><body>
        |<a href="https://example.com">Example</a>
        |<a href="/relative/path">Relative</a>
        |</body></html>""".stripMargin
    )
    val result = LinkExtractor.extract(html)
    assertEquals(
      result.map(_.href),
      List("https://example.com", "/relative/path")
    )
  }

  test("returns empty list for markup with no links") {
    val html = Markup("<html><body><p>No links here</p></body></html>")
    assertEquals(LinkExtractor.extract(html), List.empty[Hyperlink])
  }

  test("ignores anchor tags without href") {
    val html = Markup("""<html><body><a name="top">Anchor</a></body></html>""")
    assertEquals(LinkExtractor.extract(html), List.empty[Hyperlink])
  }

  test("extracts links from nested structures") {
    val html = Markup(
      """<html><body>
        |<div><ul>
        |  <li><a href="https://one.com">One</a></li>
        |  <li><a href="https://two.com">Two</a></li>
        |</ul></div>
        |</body></html>""".stripMargin
    )
    val result = LinkExtractor.extract(html)
    assertEquals(result.map(_.href), List("https://one.com", "https://two.com"))
  }

  test("handles empty href") {
    val html = Markup("""<html><body><a href="">Empty</a></body></html>""")
    val result = LinkExtractor.extract(html)
    assertEquals(result.map(_.href), List(""))
  }

  test("extracts href from area tags") {
    val html = Markup(
      """<html><body>
        |<img src="map.png" usemap="#sitemap">
        |<map name="sitemap">
        |  <area shape="rect" coords="0,0,100,50" href="https://example.com/about">
        |  <area shape="circle" coords="200,100,30" href="/contact">
        |</map>
        |</body></html>""".stripMargin
    )
    val result = LinkExtractor.extract(html)
    assertEquals(
      result.map(_.href),
      List("https://example.com/about", "/contact")
    )
  }

  test("extracts from both anchor and area tags") {
    val html = Markup(
      """<html><body>
        |<a href="https://example.com">Example</a>
        |<map name="nav">
        |  <area shape="rect" coords="0,0,50,50" href="/map-link">
        |</map>
        |</body></html>""".stripMargin
    )
    val result = LinkExtractor.extract(html)
    assertEquals(result.map(_.href), List("https://example.com", "/map-link"))
  }

  test("handles invalid html gracefully") {
    val html = Markup("""Not an html""")
    val result = LinkExtractor.extract(html)
    assertEquals(result.map(_.href), List.empty)
  }

  test("handles malformed html gracefully") {
    val html = Markup("""<a href="https://ok.com">OK</a><p>not a link</p>""")
    val result = LinkExtractor.extract(html)
    assertEquals(result.map(_.href), List("https://ok.com"))
  }
}
