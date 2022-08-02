import sbt._

object Dependencies {

  object v {
    val scala = "2.13.8"
    val tapir = "1.0.3"
    val http4s = "0.23.12"
    val circe = "0.14.1"
    val cats = "2.7.0"
    val catsEffect = "3.2.1"
  }

  val rootDeps = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core" % v.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % v.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % v.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-newtype" % v.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-refined" % v.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % v.tapir,
    "org.http4s" %% "http4s-blaze-server" % v.http4s,
    "org.http4s" %% "http4s-blaze-client" % v.http4s,
    "org.http4s" %% "http4s-ember-client" % v.http4s,
    "org.http4s" %% "http4s-circe" % v.http4s,
    "org.http4s" %% "http4s-dsl" % v.http4s,
    "io.circe" %% "circe-core" % v.circe,
    "io.circe" %% "circe-refined" % v.circe,
    "io.circe" %% "circe-generic" % v.circe,
    "io.circe" %% "circe-generic-extras" % v.circe,
    "io.circe" %% "circe-parser" % v.circe,
    "io.circe" %% "circe-optics" % v.circe,
    "io.circe" %% "circe-literal" % v.circe,
    "org.typelevel" %% "cats-core" % v.cats,
    "org.typelevel" %% "cats-effect" % v.catsEffect
  )
}
