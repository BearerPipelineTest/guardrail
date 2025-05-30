package tests.generators.http4s.client

import dev.guardrail.generators.scala.ScalaGeneratorMappings.scalaInterpreter
import dev.guardrail.generators.scala.http4s.Http4sVersion
import dev.guardrail.generators.scala.syntax.companionForStaticDefns
import dev.guardrail.Context
import dev.guardrail.generators.{ Client, Clients }
import support.SwaggerSpecRunner
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DefaultParametersTest extends AnyFunSuite with Matchers with SwaggerSpecRunner {
  import scala.meta._

  val swagger: String = s"""
    |swagger: '2.0'
    |host: petstore.swagger.io
    |paths:
    |  "/store/order/{order_id}":
    |    get:
    |      tags:
    |      - store
    |      x-jvm-package: store
    |      operationId: getOrderById
    |      produces:
    |      - application/xml
    |      - application/json
    |      parameters:
    |      - name: order_id
    |        in: path
    |        required: true
    |        type: integer
    |        maximum: 10
    |        minimum: 1
    |        format: int64
    |      - name: HeaderMeThis
    |        in: header
    |        type: string
    |        required: true
    |      - name: defparm_opt
    |        in: query
    |        type: integer
    |        format: int32
    |        default: 1
    |      - name: defparm
    |        in: query
    |        type: integer
    |        format: int32
    |        required: true
    |        default: 2
    |      responses:
    |        '200':
    |          description: successful operation
    |          schema:
    |            "$$ref": "#/definitions/Order"
    |        '400':
    |          description: Invalid ID supplied
    |        '404':
    |          description: Order not found
    |    delete:
    |      tags:
    |      - store
    |      x-jvm-package: store
    |      summary: Delete purchase order by ID
    |      description: For valid response try integer IDs with positive integer value.
    |        Negative or non-integer values will generate API errors
    |      operationId: deleteOrder
    |      produces:
    |      - application/xml
    |      - application/json
    |      parameters:
    |      - name: order_id
    |        in: path
    |        description: ID of the order that needs to be deleted
    |        required: true
    |        type: integer
    |        minimum: 1
    |        format: int64
    |      responses:
    |        '400':
    |          description: Invalid ID supplied
    |        '404':
    |          description: Order not found
    |securityDefinitions:
    |  petstore_auth:
    |    type: oauth2
    |    authorizationUrl: http://petstore.swagger.io/oauth/dialog
    |    flow: implicit
    |    scopes:
    |      write:pets: modify pets in your account
    |      read:pets: read your pets
    |  api_key:
    |    type: apiKey
    |    name: api_key
    |    in: header
    |definitions:
    |  Order:
    |    type: object
    |    properties:
    |      id:
    |        type: integer
    |        format: int64
    |      petId:
    |        type: integer
    |        format: int64
    |      quantity:
    |        type: integer
    |        format: int32
    |      shipDate:
    |        type: string
    |        format: date-time
    |      status:
    |        type: string
    |        description: Order Status
    |        enum:
    |        - placed
    |        - approved
    |        - delivered
    |      complete:
    |        type: boolean
    |        default: false
    |    xml:
    |      name: Order
    |""".stripMargin

  def testVersion(version: Http4sVersion): Unit =
    test(s"$version - Ensure responses are generated") {
      val (
        _,
        Clients(Client(tags, className, _, staticDefns, cls, statements) :: _, Nil),
        _
      ) = runSwaggerSpec(scalaInterpreter)(swagger)(Context.empty, version.value)

      tags should equal(Seq("store"))
      val cmp = companionForStaticDefns(staticDefns)

      val clientCompanion = q"""object StoreClient {
      def apply[F[_]](host: String = "http://petstore.swagger.io")(implicit F: Async[F], httpClient: Http4sClient[F]): StoreClient[F] = new StoreClient[F](host = host)(F = F, httpClient = httpClient)
      def httpClient[F[_]](httpClient: Http4sClient[F], host: String = "http://petstore.swagger.io")(implicit F: Async[F]): StoreClient[F] = new StoreClient[F](host = host)(F = F, httpClient = httpClient)
    }"""

      val clientClass = q"""
    class StoreClient[F[_]](host: String = "http://petstore.swagger.io")(implicit F: Async[F], httpClient: Http4sClient[F]) {
      val basePath: String = ""
      private def parseOptionalHeader(response: Response[F], header: String): F[Option[String]] = F.pure(response.headers.get(CIString(header)).map(_.head.value))
      private def parseRequiredHeader(response: Response[F], header: String): F[String] = response.headers.get(CIString(header)).map(_.head.value).fold[F[String]](F.raiseError(ParseFailure("Missing required header.", s"HTTP header '$$header' is not present.")))(F.pure)
      private[this] val getOrderByIdOkDecoder = jsonOf[F, Order]
      def getOrderById(orderId: Long, defparmOpt: Option[Int] = Option(1), defparm: Int = 2, headerMeThis: String, headers: List[Header.ToRaw] = List.empty): F[GetOrderByIdResponse] = {
        val allHeaders: List[org.http4s.Header.ToRaw] = List[Header.ToRaw](org.http4s.headers.Accept(org.http4s.MediaType.application.json.withQValue(org.http4s.QValue.One))) ++ headers ++ List[Option[Header.ToRaw]](Some(("HeaderMeThis", Formatter.show(headerMeThis)))).flatten
        val req = Request[F](method = Method.GET, uri = Uri.unsafeFromString(host + basePath + "/store/order/" + Formatter.addPath(orderId) + "?" + Formatter.addArg("defparm_opt", defparmOpt) + Formatter.addArg("defparm", defparm)), headers = Headers(allHeaders))
        httpClient.run(req).use({
          case _root_.org.http4s.Status.Ok(resp) =>
            F.map(getOrderByIdOkDecoder.decode(resp, strict = false).value.flatMap(F.fromEither))(GetOrderByIdResponse.Ok.apply): F[GetOrderByIdResponse]
          case _root_.org.http4s.Status.BadRequest(_) =>
            F.pure(GetOrderByIdResponse.BadRequest): F[GetOrderByIdResponse]
          case _root_.org.http4s.Status.NotFound(_) =>
            F.pure(GetOrderByIdResponse.NotFound): F[GetOrderByIdResponse]
          case resp =>
            F.raiseError[GetOrderByIdResponse](UnexpectedStatus(resp.status, Method.GET, req.uri))
        })
      }
      def deleteOrder(orderId: Long, headers: List[Header.ToRaw] = List.empty): F[DeleteOrderResponse] = {
        val allHeaders: List[org.http4s.Header.ToRaw] = List.empty[Header.ToRaw] ++ headers ++ List[Option[Header.ToRaw]]().flatten
        val req = Request[F](method = Method.DELETE, uri = Uri.unsafeFromString(host + basePath + "/store/order/" + Formatter.addPath(orderId)), headers = Headers(allHeaders))
        httpClient.run(req).use({
          case _root_.org.http4s.Status.BadRequest(_) =>
            F.pure(DeleteOrderResponse.BadRequest): F[DeleteOrderResponse]
          case _root_.org.http4s.Status.NotFound(_) =>
            F.pure(DeleteOrderResponse.NotFound): F[DeleteOrderResponse]
          case resp =>
            F.raiseError[DeleteOrderResponse](UnexpectedStatus(resp.status, Method.DELETE, req.uri))
        })
      }
    }
      """

      val expected = List(
        q"""
        sealed abstract class GetOrderByIdResponse {
          def fold[A](handleOk: Order => A, handleBadRequest: => A, handleNotFound: => A): A = this match {
            case x: GetOrderByIdResponse.Ok =>
              handleOk(x.value)
            case GetOrderByIdResponse.BadRequest =>
              handleBadRequest
            case GetOrderByIdResponse.NotFound =>
              handleNotFound
          }
        }
      """,
        q"""object GetOrderByIdResponse {
      case class Ok(value: Order) extends GetOrderByIdResponse
      case object BadRequest extends GetOrderByIdResponse
      case object NotFound extends GetOrderByIdResponse
    }""",
        q"""
        sealed abstract class DeleteOrderResponse {
          def fold[A](handleBadRequest: => A, handleNotFound: => A): A = this match {
            case DeleteOrderResponse.BadRequest => handleBadRequest
            case DeleteOrderResponse.NotFound => handleNotFound
          }
        }
      """,
        q"""object DeleteOrderResponse {
      case object BadRequest extends DeleteOrderResponse
      case object NotFound extends DeleteOrderResponse
    }"""
      )

      cls.head.value.structure should equal(clientClass.structure)
      cmp.structure should equal(clientCompanion.structure)

      statements.zip(expected).foreach { case (a, b) => a.structure should equal(b.structure) }
    }

  testVersion(Http4sVersion.V0_22)
  testVersion(Http4sVersion.V0_23)
}
