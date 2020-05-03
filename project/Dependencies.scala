import sbt._

object Dependencies {
  val scalaScraperVersion = "2.2.0"
  val akkaHttpVersion = "10.1.11"
  val akkaStreamVersion = "2.5.31"
  val pureConfigVersion = "0.12.3"
  val catsEffectVersion = "2.1.3"
  val catsCoreVersion = "2.1.1"

  lazy val scalaScraper = Seq(
    "net.ruippeixotog" %% "scala-scraper" % "2.2.0"
  )

  lazy val akka = Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
  )

  lazy val pureConfig = Seq(
    "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
  )

  lazy val cats = Seq(
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "org.typelevel" %% "cats-core" % catsCoreVersion
  )

  lazy val dependencies
    : Seq[ModuleID] = scalaScraper ++ akka ++ pureConfig ++ cats
}
