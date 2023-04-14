package com.example.cart
import cats.data.NonEmptyList
import domain._
import cats.effect._
import com.example.cart.ShoppingCartService.TaxRate
import com.example.cart.errors.{EmptyCart, ItemNotFound}
import squants.market.USD

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class ShoppingCartServiceSpec extends UnitSpec with Arbitraries  {

  "ShoppingCartApi" should "add new items to the shopping cart" in {

    forAll { (item: Item, quantity: Quantity) =>
      val catalog = new ItemCatalogService[IO] {
        override def find(title: ItemTitle): IO[Option[Item]] = IO.pure(Some(item))
      }

      val shoppingCart: ShoppingCartService[IO] = ShoppingCartService[IO](catalog)

      {
        for {
          _ <- shoppingCart.addItem(item.title, quantity)
          maybeCartItem <- shoppingCart.getCartItem(item.title)
        } yield maybeCartItem shouldBe Some(CartItem(item, quantity))
      }.unsafeToFuture().futureValue
    }
  }

  it should "return an error when adding an item that does not exist" in {
    forAll { (item: Item, quantity: Quantity) =>
      val catalog = new ItemCatalogService[IO] {
        override def find(title: ItemTitle): IO[Option[Item]] = IO.pure(None)
      }

      val shoppingCart: ShoppingCartService[IO] = ShoppingCartService[IO](catalog)

      assertThrows[ItemNotFound](Await.result(shoppingCart.addItem(item.title, quantity).unsafeToFuture(), 2.seconds))
    }
  }

  it should "replace items that already exist in the cart" in {
    forAll { (item: Item, existingQuantity: Quantity, newQuantity: Quantity) =>
      val catalog = new ItemCatalogService[IO] {
        override def find(title: ItemTitle): IO[Option[Item]] = IO.pure(Some(item))
      }

      val shoppingCart: ShoppingCartService[IO] = ShoppingCartService[IO](catalog)

      {
        for {
          _ <- shoppingCart.addItem(item.title, existingQuantity)
          _ <- shoppingCart.addItem(item.title, newQuantity)
          storedCartItem <- shoppingCart.getCartItem(item.title)
        } yield storedCartItem shouldBe Some(CartItem(item, newQuantity))
      }.unsafeToFuture().futureValue
    }
  }

  it should "retrieve a shopping cart" in {

    forAll { (cartItems: List[CartItem]) =>
      whenever(cartItems.nonEmpty) {

        val catalog = new ItemCatalogService[IO] {
          override def find(id: ItemTitle): IO[Option[Item]] = IO.pure(cartItems.find(_.item.title == id).map(_.item))
        }

        val shoppingCart: ShoppingCartService[IO] = ShoppingCartService[IO]( catalog)

        for {
          _ <- shoppingCart.addCartItems(cartItems)
        } yield ???

        val result = shoppingCart.getCart.unsafeToFuture().futureValue

        result.items.toList should contain theSameElementsAs cartItems
        result.totals.total should be > USD(0.0)
      }
    }
  }

  it should "return an error when an attempt is made to get an empty cart" in {

      val itemCatalog = new ItemCatalogService[IO] {
        override def find(title: ItemTitle): IO[Option[Item]] = ???
      }
      val shoppingCart = ShoppingCartService[IO](itemCatalog)

      assertThrows[EmptyCart](Await.result(shoppingCart.getCart.unsafeToFuture, 2.seconds))
  }

  it should "calculate the subtotal of the cart" in {
    val list =
      NonEmptyList.fromListUnsafe(
        List(
          CartItem(Item(ItemTitle("a"), USD(5.50)), Quantity(1)),
          CartItem(Item(ItemTitle("b"), USD(15.50)), Quantity(10)),
          CartItem(Item(ItemTitle("c"), USD(33.33)), Quantity(100)))
      )

    val actual = ShoppingCartService.cartTotals(list)

    val subTotal = USD(5.50) + (USD(15.50) * 10) + (USD(33.33) * 100)
    val taxes = subTotal * (TaxRate / 100)
    val total = subTotal + taxes
    val expected = Totals(subTotal, taxes, total)

    actual shouldBe expected
  }
}
