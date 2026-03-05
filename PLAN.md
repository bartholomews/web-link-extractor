## Notes for next steps

- Consider replacing circular buffer with bounded queue + backpressure (or Channel).
  One scenario where drop-oldest could make sense is a live/streaming system where freshness matters more than
  completeness
  (e.g., real-time data / live feeds). This is currently a batch job processing a finite set of files,
  so I feel backpressure is more sound given the current assumptions.

- Once the core requirements are done, consider some integration-tests (possibly weaver?) for the overall flow, e.g.
  with wiremock for url resolution

- Store/cache already fetched urls (i.e. avoid re-processing)

