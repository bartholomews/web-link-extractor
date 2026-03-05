ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "web-link-extractor",
    libraryDependencies ++= Seq(
      // https://github.com/jhy/jsoup
      "org.jsoup" % "jsoup" % "1.22.1"
    ) ++ Seq(
      // https://github.com/typelevel/munit-cats-effect
      "org.typelevel" %% "munit-cats-effect" % "2.1.0"
    ).map(_ % Test)
  )
