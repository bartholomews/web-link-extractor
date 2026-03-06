package wle.consumer

import org.jsoup.Jsoup
import wle.domain.{ExtractionResult, Hyperlink, RawMarkup}

import scala.jdk.CollectionConverters._

object LinkExtractor {
  def extract[F[_]]: fs2.Pipe[F, RawMarkup, ExtractionResult] =
    _.map(markup =>
      ExtractionResult(
        markup.uri,
        Jsoup
          .parse(markup.content)
          .select("a[href], area[href]")
          .asScala
          .map(el => Hyperlink(el.attr("href")))
          .toList
      )
    )
}
