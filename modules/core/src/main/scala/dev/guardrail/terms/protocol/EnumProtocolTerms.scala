package dev.guardrail.terms.protocol

import cats.Monad
import dev.guardrail.languages.LA
import dev.guardrail.terms.{ CollectionsLibTerms, RenderedEnum }

abstract class EnumProtocolTerms[L <: LA, F[_]](implicit Cl: CollectionsLibTerms[L, F]) { self =>
  def MonadF: Monad[F]
  def renderMembers(clsName: String, elems: RenderedEnum[L]): F[Option[L#ObjectDefinition]]
  def encodeEnum(clsName: String, tpe: L#Type): F[Option[L#Definition]]
  def decodeEnum(clsName: String, tpe: L#Type): F[Option[L#Definition]]
  def renderClass(clsName: String, tpe: L#Type, elems: RenderedEnum[L]): F[L#ClassDefinition]
  def renderStaticDefns(
      clsName: String,
      tpe: L#Type,
      members: Option[L#ObjectDefinition],
      accessors: List[L#TermName],
      encoder: Option[L#Definition],
      decoder: Option[L#Definition]
  ): F[StaticDefns[L]]
  def buildAccessor(clsName: String, termName: String): F[L#TermSelect]

  def copy(
      MonadF: Monad[F] = self.MonadF,
      renderMembers: (String, RenderedEnum[L]) => F[Option[L#ObjectDefinition]] = self.renderMembers _,
      encodeEnum: (String, L#Type) => F[Option[L#Definition]] = self.encodeEnum _,
      decodeEnum: (String, L#Type) => F[Option[L#Definition]] = self.decodeEnum _,
      renderClass: (String, L#Type, RenderedEnum[L]) => F[L#ClassDefinition] = self.renderClass _,
      renderStaticDefns: (String, L#Type, Option[L#ObjectDefinition], List[L#TermName], Option[L#Definition], Option[L#Definition]) => F[StaticDefns[L]] =
        self.renderStaticDefns _,
      buildAccessor: (String, String) => F[L#TermSelect] = self.buildAccessor _
  ) = {
    val newMonadF            = MonadF
    val newRenderMembers     = renderMembers
    val newEncodeEnum        = encodeEnum
    val newDecodeEnum        = decodeEnum
    val newRenderClass       = renderClass
    val newRenderStaticDefns = renderStaticDefns
    val newBuildAccessor     = buildAccessor

    new EnumProtocolTerms[L, F] {
      def MonadF                                                            = newMonadF
      def renderMembers(clsName: String, elems: RenderedEnum[L])            = newRenderMembers(clsName, elems)
      def encodeEnum(clsName: String, tpe: L#Type): F[Option[L#Definition]] = newEncodeEnum(clsName, tpe)
      def decodeEnum(clsName: String, tpe: L#Type): F[Option[L#Definition]] = newDecodeEnum(clsName, tpe)
      def renderClass(clsName: String, tpe: L#Type, elems: RenderedEnum[L]) = newRenderClass(clsName, tpe, elems)
      def renderStaticDefns(
          clsName: String,
          tpe: L#Type,
          members: Option[L#ObjectDefinition],
          accessors: List[L#TermName],
          encoder: Option[L#Definition],
          decoder: Option[L#Definition]
      ): F[StaticDefns[L]]                                 = newRenderStaticDefns(clsName, tpe, members, accessors, encoder, decoder)
      def buildAccessor(clsName: String, termName: String) = newBuildAccessor(clsName, termName)
    }
  }
}
