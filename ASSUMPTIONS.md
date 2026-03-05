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
