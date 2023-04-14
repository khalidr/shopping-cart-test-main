package com.example

import cats.effect.IOApp
import cats.effect.IO
import com.example.cart.domain.{ItemTitle, Quantity}
import com.example.cart.{ItemCatalog, ShoppingCart}
import okhttp3.OkHttpClient

object Main extends IOApp.Simple {

  private val itemCatalog = ItemCatalog[IO](new OkHttpClient())
  private val shoppingCart = ShoppingCart[IO](itemCatalog)

  def run: IO[Unit] =
    for {
      _ <- shoppingCart.addItem(ItemTitle("cornflakes"), Quantity(2))
      _ <- shoppingCart.addItem(ItemTitle("weetabix"), Quantity(1))
      cart <- shoppingCart.getCart
      _ <- IO.println(cart.show)
    } yield ()
}
