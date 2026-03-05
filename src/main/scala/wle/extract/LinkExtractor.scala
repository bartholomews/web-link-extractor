package wle.extract

import org.jsoup.Jsoup
import wle.domain.{Hyperlink, Markup}

import scala.jdk.CollectionConverters._

object LinkExtractor {
  def extract(markup: Markup): List[Hyperlink] =
    Jsoup
      .parse(markup.content)
      .select("a[href], area[href]")
      .asScala
      .map(el => Hyperlink(el.attr("href")))
      .toList
}
