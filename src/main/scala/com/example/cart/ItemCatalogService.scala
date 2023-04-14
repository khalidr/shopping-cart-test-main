package com.example.cart

import cats.effect.Sync
import cats.implicits._
import cats.syntax.either._
import com.example.cart.domain._
import com.example.cart.errors.{EndpointError, ParseError}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import okhttp3.{OkHttpClient, Request, Response}

import java.net.URL

trait ItemCatalogService[F[_]] {
  def find(title: ItemTitle): F[Option[Item]]
}

object ItemCatalogService extends Encoders {
  private val itemsBaseUrl = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main"

  def apply[F[_] : Sync](client: OkHttpClient): ItemCatalogService[F] = (title: ItemTitle) => {
    val url = s"$itemsBaseUrl/${title.value}.json"
    val request = new Request
    .Builder()
      .url(url)
      .get()
      .build()

    def makeCall: F[Response] = Sync[F].blocking(client.newCall(request).execute()).adaptErr {
      e: Throwable => EndpointError(new URL(url), e.getMessage)
    }

    for {
      response <- makeCall
      maybeBodyString = response.code() match {
        case 200 => Option(response.body()).map(_.string)
        case _ => None
      }
      decodedOrError = maybeBodyString.map(decode[Item](_)).sequence
      maybeItem <- Sync[F].fromEither(decodedOrError.leftMap(e => ParseError(new URL(url), e.getMessage)))
    } yield maybeItem
  }
}
