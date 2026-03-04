ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "web-link-extractor",
    libraryDependencies ++= Seq.empty
  )
