package com.example.cart

import cats.data.NonEmptyList
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

  case class Cart(items: NonEmptyList[CartItem], totals: Totals) {
    def show: String = {
      val lines = items.map { case CartItem(item, quantity) =>
        s"Add ${quantity.value} * ${item.title} @ ${item.price.toFormattedString}"
      }.toList

      val totalsString = {
        s"""
           |Subtotal = ${totals.subtotal.toFormattedString}
           |Tax = ${totals.taxesPayable.toFormattedString}
           |Total = ${totals.total.toFormattedString}
           |""".stripMargin
      }

      lines.mkString("\n") + totalsString
    }
  }

  case class CartItem(item: Item, quantity: Quantity)

}
