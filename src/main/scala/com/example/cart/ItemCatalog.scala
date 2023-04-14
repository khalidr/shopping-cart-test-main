package com.example.cart

import cats.effect.Sync
import cats.implicits._
import cats.syntax.either._
import com.example.cart.domain._
import com.example.cart.errors.EndpointError
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import okhttp3.{OkHttpClient, Request, Response}

trait ItemCatalog[F[_]] {
  def find(title: ItemTitle): F[Option[Item]]
}

object ItemCatalog extends Encoders {
  private val itemsBaseUrl = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main"

  def apply[F[_] : Sync](client: OkHttpClient): ItemCatalog[F] = (title: ItemTitle) => {
    val request = new Request
    .Builder()
      .url(s"$itemsBaseUrl/${title.value}.json")
      .get()
      .build()

    def makeCall: F[Response] = Sync[F].blocking(client.newCall(request).execute()).adaptErr {
      e: Throwable => EndpointError(title, e.getMessage)
    }

    for {
      response <- makeCall
      maybeBodyString = response.code() match {
        case 200 => Option(response.body()).map(_.string)
        case _ => None
      }
      decoded <- Sync[F].fromEither(maybeBodyString.map(decode[Item](_)).sequence.leftMap(_.getCause))
    } yield decoded
  }
}
