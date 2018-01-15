package com.twilio

import _root_.io.swagger.models._
import _root_.io.swagger.parser.SwaggerParser
import cats.arrow.FunctionK
import com.twilio.swagger.codegen._
import com.twilio.swagger.codegen.generators.AkkaHttp
import org.scalatest._
import scala.collection.JavaConverters._
import scala.meta._
import com.twilio.swagger.codegen.terms.ScalaTerms
import cats.implicits._

package object swagger {
  def runSwaggerSpec(spec: String)(context: Context, framework: FunctionK[CodegenApplication, Target]): (ProtocolDefinitions, Clients, Servers) = runSwagger(new SwaggerParser().parse(spec))(context, framework)
  def runSwagger(swagger: Swagger)(context: Context, framework: FunctionK[CodegenApplication, Target]
      )(implicit Sc: ScalaTerms[CodegenApplication]
      ): (ProtocolDefinitions, Clients, Servers) = {
    import Sc._
    val prog = for {
      protocol <- ProtocolGenerator.fromSwagger[CodegenApplication](swagger)
      definitions = protocol.elems
      clients <- ClientGenerator.fromSwagger[CodegenApplication](context, swagger)(definitions)
      servers <- ServerGenerator.fromSwagger[CodegenApplication](context, swagger)(definitions)
    } yield (protocol, clients, servers)

    Target.unsafeExtract(prog.foldMap(framework))
  }
}
