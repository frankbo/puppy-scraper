import Dependencies._

scalaVersion := "2.13.1"
name := "puppy-scraper"
version := "1.0"
libraryDependencies := dependencies

enablePlugins(DockerPlugin) // or should I enablePlugins(DockerPlugin) as mentioned on native-packager page
enablePlugins(JavaAppPackaging)
version in Docker := "0.0.1"