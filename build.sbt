// https://github.com/scala/scala
ThisBuild / scalaVersion := "2.13.18"

lazy val root = (project in file("."))
  .settings(
    name := "web-link-extractor",
    libraryDependencies ++= Seq(
      // https://github.com/jhy/jsoup
      "org.jsoup" % "jsoup" % "1.22.1",
      // https://github.com/typelevel/cats-effect
      "org.typelevel" %% "cats-effect" % "3.6.3",
      // https://github.com/typelevel/fs2
      "co.fs2" %% "fs2-io" % "3.12.2",
      // https://github.com/softwaremill/sttp
      "com.softwaremill.sttp.client4" %% "cats" % "4.0.19",
      // https://github.com/typelevel/log4cats
      "org.typelevel" %% "log4cats-slf4j" % "2.7.1",
      // https://github.com/qos-ch/logback
      "ch.qos.logback" % "logback-classic" % "1.5.32",
      // https://github.com/circe/circe
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15"
    ) ++ Seq(
      // https://github.com/typelevel/munit-cats-effect
      "org.typelevel" %% "munit-cats-effect" % "2.1.0"
    ).map(_ % Test)
  )
