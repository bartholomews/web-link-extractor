# How to run

Place one or more files containing URLs (one per line) in `src/main/resources/in/`, then:

```
sbt run
```

Output is written to `src/main/resources/out`. Tests can be run with `sbt test`.

# Assumptions

## Program lifecycle

The program processes a _finite_ set of URLs and exits once all have been fetched and parsed
(that's how I interpret "_The Consumer reads the queue until it is empty_").

## Hyperlink extraction scope

"All hyperlinks" is interpreted as `<a>` and `<area>` elements with an `href` attribute.
Other elements with `href` (`<link>`, `<base>`) are excluded, as the goal is to extract hyperlinks to pages rather than
resource references.

## MarkupExtractor

Only the HTTP status code (2xx) is validated, otherwise we parse the response body as plain string.
This is to cleanly separate the boundaries between producer (network I/O bounded) and consumer (HTML parsing, CPU
bound). It could check `Content-Type` (e.g. `text/html`) but we could assume header might be missing, charsets params,
etc. would overcomplicate.

## Queue strategy ("Trimming oldest queue entries if queue size balloons")

This relates to "Trimming oldest queue entries if queue size balloons": the current approach is to use a bounded queue
with backpressure (the capacity is set to an arbitrary default — in practice it would depend on expected throughput,
memory constraints, etc.).

The assumption is that URL extraction doesn't have an intrinsic priority: every requested URL should be processed,
so data loss is not acceptable.

In a different use case (e.g. real-time data / live feeds - where freshness matters more than completeness),
trimming via a circular buffer could be an alternative. However, I think even then the domain model
would need to carry identifiers / timestamps to discard data safely, rather than relying on
positional eviction of the oldest queue entries.

## Product considerations

Hyperlinks are extracted as raw `href` values without transformation, preserving the original data.
The following are product-level decisions that would depend on the use case:

- **Relative URLs** (e.g. `/about`, `../page`): not resolved against the source page URL.
  Could be resolved using the source URI in `ExtractionResult` if absolute URLs are needed.
- **Fragment-only links** (e.g. `#section`): included as-is. These refer to anchors within the same page,
  not separate pages — filtering them out may or may not be desired.
- **Non-HTTP schemes** (e.g. `mailto:`, `tel:` etc.): included as-is (actually noticed what it seems
  Cloudflare obfuscated value for my website email)
- These are valid `href` values but not web page links — filtering would require a product decision on what counts as
  a "hyperlink" for the specific use case.
- **Duplicate links**: a page may contain the same link multiple times. Currently all occurrences are included;
  deduplication per page could be added if only unique links are needed.
- **Absolute URL normalisation**: URLs like `https://example.com/` and `https://example.com` are treated as
  distinct values. Normalisation could be added if semantic equality matters.

Output data is currently written into a single `out` file which gets overridden after each run.
If historical results were needed, the output path could incorporate a timestamp or run ID, or results could be appended
rather than overwritten.

# Possible follow-up work

- **Caching**: skip URLs already fetched to avoid redundant network requests across input files.
- **Integration tests with wiremock**: test the full pipeline (Source → UrlFetcher → Queue → LinkExtractor → Sink)
  with stubbed HTTP responses for realistic end-to-end coverage.
- **UrlFetcher unit tests**: use sttp's `BackendStub` to test HTTP error handling (non-2xx, timeouts, connection errors)
  without hitting the network.
- **Performance testing**: generate a large input set with a slow/throttled mock server
  (e.g. wiremock with fixed delays) to observe backpressure behaviour under load — verify the bounded queue blocks the
  producer rather than growing memory unboundedly.
