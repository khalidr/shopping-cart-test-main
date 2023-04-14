package com.example.cart

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import squants.Money
import squants.market.USD

trait Encoders {
  implicit val moneyEncoder: Encoder[Money] = (m: Money) => m.value.asJson
  implicit val moneyDecoder: Decoder[Money] = Decoder.decodeDouble.map(USD(_))
}
