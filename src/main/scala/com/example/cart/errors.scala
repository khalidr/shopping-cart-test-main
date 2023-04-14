package com.example.cart

import com.example.cart.domain.ItemTitle

object errors {
  case class ItemNotFound(title: ItemTitle) extends RuntimeException(title.value)

  case class EmptyCart() extends RuntimeException

  case class EndpointError(title: ItemTitle, reason: String) extends RuntimeException(reason)
}
