package com.github.tapirrepro

import cats.effect._
import cats.implicits._
import io.circe._
import io.circe.literal._
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.blaze.server._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.POST
import org.http4s.implicits._
import org.http4s.server.Router
import sttp.model.StatusCode
import sttp.model.headers.{Cookie, CookieValueWithMeta}
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.ServerEndpoint
import sttp.capabilities.fs2.Fs2Streams
import scala.concurrent.duration._

object Repro extends IOApp {

  case class TestReq(name: String)
  object TestReq {
    implicit val encoder: Encoder[TestReq] = deriveEncoder[TestReq]
    implicit val decoder: Decoder[TestReq] = deriveDecoder[TestReq]
  }

  abstract class TestErr(val error: String, val description: Option[String])
  object TestErr {

    implicit def encoder[A <: TestErr]: Encoder[A] =
      new Encoder[A] {
        def apply(a: A): Json =
          json"""{
            "error": ${a.error},
            "error_description": ${a.description}
          }"""
      }

    implicit def decoder[A <: TestErr]: Decoder[A] =
      new Decoder[A] {
        def apply(c: HCursor): Decoder.Result[A] =
          c.downField("error").as[String].map {
            case "internal_server_error" => InternalServerError.asInstanceOf[A]
            case "invalid_name"          => InvalidNameError.asInstanceOf[A]
            case _                       => ???
          }
      }

    object InvalidNameError
        extends TestErr(
          "invalid_name",
          "Valid name required.".some
        )

    object InternalServerError
        extends TestErr(
          "internal_server_error",
          "Contact the administrator.".some
        )
  }

  case class TestRes(greeting: String)
  object TestRes {
    implicit val encoder: Encoder[TestRes] = deriveEncoder[TestRes]
    implicit val decoder: Decoder[TestRes] = deriveDecoder[TestRes]
  }

  val invalidNameError =
    statusCode(StatusCode.Forbidden).and(
      jsonBody[TestErr.InvalidNameError.type]
        .description("Valid name required.")
        .example(TestErr.InvalidNameError)
    )

  val internalServerError =
    statusCode(StatusCode.InternalServerError).and(
      jsonBody[TestErr.InternalServerError.type]
        .description("Internal server error.")
        .example(TestErr.InternalServerError)
    )

  val testEndpoint: Endpoint[
    Unit,
    TestReq,
    TestErr,
    TestRes,
    Any
  ] =
    endpoint.post
      .in("test")
      .in(jsonBody[TestReq])
      .errorOut(
        oneOf[TestErr](
          oneOfVariantValueMatcher(invalidNameError) {
            case Left(TestErr.InvalidNameError) => true
          },
          oneOfDefaultVariant(internalServerError)
        )
      )
      .out(
        statusCode(StatusCode.Ok)
          .and(
            jsonBody[TestRes]
              .description("Greeting.")
              .example(TestRes("Hey, John!"))
          )
      )
      .description("An endpoint responsible for greeting users.")

  def routes[F[+_]: Async]: HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      List[ServerEndpoint[Fs2Streams[F], F]](
        testEndpoint.serverLogic { _ =>
          (TestErr.InvalidNameError: TestErr).asLeft[TestRes].pure[F]
        }
      )
    )

  val httpRoutes = Router(
    "/" -> routes[IO]
  ).orNotFound

  def testReq: Request[IO] =
    POST(
      json"""{ "name": "John" }""",
      uri"http://localhost:8080/" / "test"
    )

  val expectedJson =
    json"""{
      "error": "invalid_name",
      "error_description": "Valid name required."
    }"""

  override def run(args: List[String]): IO[ExitCode] =
    for {
      res <- httpRoutes.run(testReq)
      body <- res.asJson
      _ <-
        if (body == expectedJson) IO.println("Body as expected")
        else IO.println(s"Unexpected body: $body")
      _ <-
        if (res.status == Status.Forbidden) IO.println("Status as expected")
        else IO.println(s"Unexpected status: ${res.status}")
    } yield ExitCode.Success
}
