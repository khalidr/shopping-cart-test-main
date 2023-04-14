package com.example.cart

import cats.effect.testing.scalatest.AsyncIOSpec
import org.mockito.IdiomaticMockito
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait UnitSpec extends AsyncFlatSpecLike with AsyncIOSpec with ScalaFutures with ScalaCheckPropertyChecks
  with IdiomaticMockito with Matchers with EitherValues /*{
  implicit class RichIO[A](f: IO[A]) {
    def futureResult: A = f.unsafeToFuture().futureValue(Timeout(Span(2, Seconds)))
  }
}*/

