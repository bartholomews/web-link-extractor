package wle.extract

import wle.domain.{Hyperlink, Markup}
import munit.CatsEffectSuite

class LinkExtractorSuite extends CatsEffectSuite {

  test("extracts href from anchor tags") {
    val html = Markup(
      """<html><body>
        |<a href="https://example.com">Example</a>
        |<a href="/relative/path">Relative</a>
        |</body></html>""".stripMargin
    )
    val result = LinkExtractor.extract(html)
    assertEquals(result.map(_.href), List("https://example.com", "/relative/path"))
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

  test("handles malformed html gracefully") {
    val html = Markup("""<a href="https://ok.com">OK</a><p>not a link</p>""")
    val result = LinkExtractor.extract(html)
    assertEquals(result.map(_.href), List("https://ok.com"))
  }
}
