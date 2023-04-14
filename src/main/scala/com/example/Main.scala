package com.example

import cats.effect.{IO, IOApp}
import com.example.cart.domain.{ItemTitle, Quantity}
import com.example.cart.{ItemCatalogService, ShoppingCartService}
import okhttp3.OkHttpClient

object Main extends IOApp.Simple {

  private val itemCatalog = ItemCatalogService[IO](new OkHttpClient())
  private val shoppingCart = ShoppingCartService[IO](itemCatalog)

  def run: IO[Unit] =
    for {
      _ <- shoppingCart.addItem(ItemTitle("cornflakes"), Quantity(2))
      _ <- shoppingCart.addItem(ItemTitle("weetabix"), Quantity(1))
      cart <- shoppingCart.getCart
      _ <- IO.println(cart)
    } yield ()
}
