# web-link-extractor

A Simple Producer/Consumer Web Link Extractor

## The Producer

- Receives a list of URLs (from file, command line, etc.).
- Extracts the markup from each URL and places this output onto some form of queue.

## The Consumer

- Reads the queue until it is empty and the producer is no longer extracting markup.
- Parses the HTML and extracts all hyperlinks into a list. This list is output (file or command line) against each parsed URL.
Requirements

- Producer and consumer must run concurrently.
- Error handling should ensure isolation - one bad fetch or parse should not affect others.
- Include some unit tests.

### Bonus Points

- URLs fetched concurrently.
- Trimming oldest queue entries if queue size balloons.
- Comprehensive test coverage.
- Other considerations/enhancements you think are valuable.
