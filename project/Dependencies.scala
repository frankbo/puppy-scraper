import sbt._

object Dependencies {
  val scalaScraperVersion = "2.2.0"

  lazy val scalaScraper = Seq(
    "net.ruippeixotog" %% "scala-scraper" % "2.2.0"
  )
  lazy val dependencies = scalaScraper
}

