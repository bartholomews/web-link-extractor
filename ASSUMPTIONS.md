# Assumptions

## Hyperlink extraction scope

"All hyperlinks" is interpreted as `<a>` and `<area>` elements with an `href` attribute.
Other elements with `href` (`<link>`, `<base>`) are excluded, as the goal is to extract hyperlinks to pages rather than resource references.
