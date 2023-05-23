package com.example.cart

import cats.Show
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.estatico.newtype.macros.newtype
import squants.Money

object domain {

  @derive(decoder, encoder)
  @newtype case class ItemTitle(value: String)

  @derive(decoder, encoder)
  @newtype case class Quantity(value: Int)

  case class Totals(subtotal: Money, taxesPayable: Money, total: Money)

  case class Item(title: ItemTitle, price: Money)

  case class ShoppingCart(items: List[CartItem], totals: Totals)

  object ShoppingCart {
    implicit val show: Show[ShoppingCart] = (cart: ShoppingCart) => {
      val lines = cart.items.map { case CartItem(item, quantity) =>
        s"Add ${quantity.value} * ${item.title} @ ${item.price.toFormattedString}"
      }.toList

      val totalsString = {
        s"""
           |Subtotal = ${cart.totals.subtotal.toFormattedString}
           |Tax = ${cart.totals.taxesPayable.toFormattedString}
           |Total = ${cart.totals.total.toFormattedString}
           |""".stripMargin
      }

      lines.mkString("\n") + totalsString
    }
  }

  case class CartItem(item: Item, quantity: Quantity)

  trait DeleteStatus

  case object Successful extends DeleteStatus

  case object Failed extends DeleteStatus

}
