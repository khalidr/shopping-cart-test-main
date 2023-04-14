package com.example.cart

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.implicits.{toFunctorOps, _}
import com.example.cart.domain._
import com.example.cart.errors._

import scala.collection.mutable
import io.circe.syntax._
import squants.market.USD

trait ShoppingCart[F[_]] {

  def addItem(item: ItemTitle, quantity: Quantity): F[Unit]

  def getCartItem(title: ItemTitle): F[Option[CartItem]]

  def addCartItems(items: List[CartItem]): F[Unit]

  def getCart: F[Cart]
}

object ShoppingCart {
  val TaxRate: Double = 12.5

  def apply[F[_] : MonadThrow](catalog: ItemCatalog[F]): ShoppingCart[F] = new ShoppingCart[F] {
    private val store: mutable.Map[Item, Quantity] = mutable.Map.empty

    override def addItem(itemId: ItemTitle, quantity: Quantity): F[Unit] =
      for {
        maybeItem <- catalog.find(itemId)
        v <- maybeItem.map(addToStore(_, quantity)).getOrElse(MonadThrow[F].raiseError(ItemNotFound(itemId)))
      } yield v

    private def addToStore(item: Item, quantity: Quantity): F[Unit] = MonadThrow[F].pure(store.addOne(item -> quantity)).void

    override def getCart: F[Cart] = {
      val nonEmptyListOrError = Either.fromOption(NonEmptyList.fromList(store.toList), EmptyCart())

      for {
        nonEmptyItemList <- MonadThrow[F].fromEither(nonEmptyListOrError)
        cartItems = nonEmptyItemList.map { case (item, quantity) => CartItem(item, quantity) }
      } yield Cart(cartItems, cartTotals(cartItems))
    }

    override def getCartItem(title: ItemTitle): F[Option[CartItem]] =
      MonadThrow[F]
        .pure(store.find { case (item, _) => item.title == title }
          .map{case(item, quantity) => CartItem(item, quantity)})


    override def addCartItems(items: List[CartItem]): F[Unit] =
      MonadThrow[F].pure(store.addAll(items.map{case CartItem(item, quantity) => item -> quantity})).void
  }


  def cartTotals(cartItems: NonEmptyList[CartItem]): Totals = {

    val subTotal = cartItems.foldLeft(USD(0)) { case (runningTotal, cartItem) =>
      runningTotal + (cartItem.item.price * cartItem.quantity.value)
    }
    val taxesPayable = subTotal * (TaxRate / 100)
    val total = subTotal + taxesPayable

    Totals(subTotal, taxesPayable, total)
  }
}
