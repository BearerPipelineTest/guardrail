package com.twilio.guardrail.generators

import cats.data.NonEmptyList
import com.twilio.guardrail.Target
import com.twilio.guardrail.protocol.terms.{ ApplicationJson, ContentType, TextPlain }
import scala.meta._

object AkkaHttpHelper {
  def generateDecoder(tpe: Type, consumes: NonEmptyList[ContentType]): Target[(Term, Type)] = {
    val baseType = tpe match {
      case t"Option[$x]" => x
      case x             => x
    }

    for {
      unmarshallers <- consumes.traverse[Target, Term]({
        case ApplicationJson => Target.pure(q"structuredJsonEntityUnmarshaller")
        case TextPlain       => Target.pure(q"stringyJsonEntityUnmarshaller")
        case contentType     => Target.raiseError(s"Unable to generate decoder for ${contentType}")
      })
      unmarshaller = unmarshallers match {
        case NonEmptyList(x, Nil) => x
        case xs                   => q"Unmarshaller.firstOf(..${xs.toList})"
      }
    } yield {
      val decoder = q""" {
        ${unmarshaller}.flatMap(_ => _ => json => io.circe.Decoder[${baseType}].decodeJson(json).fold(FastFuture.failed, FastFuture.successful))
      }
      """
      (decoder, baseType)
    }
  }
}
