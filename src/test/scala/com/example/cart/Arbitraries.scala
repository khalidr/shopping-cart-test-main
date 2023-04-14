package com.example.cart

import org.scalacheck.{Arbitrary, Gen}
import squants.market.{Money, USD}
import domain._

trait Arbitraries {

  implicit val arbString: Arbitrary[String] = Arbitrary(Gen.alphaStr.suchThat(_.nonEmpty))

  implicit val arbMoney: Arbitrary[Money] = Arbitrary(Gen.posNum[Double].map(USD(_).rounded(2)))

  implicit val arbItemId: Arbitrary[ItemTitle] = Arbitrary(Gen.resultOf(ItemTitle.apply _).suchThat(_.value.nonEmpty))

  implicit val arbItem: Arbitrary[Item] = Arbitrary(Gen.resultOf(Item))

  implicit val arbQuantity: Arbitrary[Quantity] = Arbitrary(Gen.choose[Int](1, 100).map(Quantity.apply))

  implicit val arbCartItem: Arbitrary[CartItem] = Arbitrary(Gen.resultOf(CartItem))
}

