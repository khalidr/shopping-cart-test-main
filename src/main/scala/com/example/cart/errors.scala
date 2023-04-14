package com.example.cart

import com.example.cart.domain.ItemTitle

import java.net.URL

object errors {
  case class ItemNotFound(title: ItemTitle) extends RuntimeException(title.value)

  case class EmptyCart() extends RuntimeException

  case class EndpointError(url: URL, reason: String) extends RuntimeException(s"Endpoint ${url.toString} failed: $reason")

  case class ParseError(url: URL, reason: String) extends RuntimeException(s"Error parsing response from ${url.toString}: $reason")
}
