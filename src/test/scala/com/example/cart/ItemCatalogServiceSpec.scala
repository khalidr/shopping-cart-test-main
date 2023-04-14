package com.example.cart

import cats.effect.IO
import com.example.cart.domain._
import com.example.cart.errors.{EndpointError, ParseError}
import io.circe.generic.auto._
import io.circe.syntax._
import okhttp3.{Call, MediaType, OkHttpClient, Protocol, Request, Response, ResponseBody}
import org.mockito.ArgumentMatchers.any

import java.io.IOException
import scala.concurrent.Await
import scala.concurrent.duration._


class ItemCatalogServiceSpec extends UnitSpec with Encoders with Arbitraries {

  "ItemCatalog" should "find an item" in {

    forAll { (title: ItemTitle, item: Item) =>

      val client = mock[OkHttpClient]
      val mockCall = mock[Call]
      client.newCall(any) returns mockCall
      val responseBody = ResponseBody.create(item.asJson.noSpaces, MediaType.get("application/json; charset=utf-8"))
      val response =
        new Response
        .Builder()
          .request(new Request.Builder().url("http://foo.com").build())
          .protocol(Protocol.HTTP_2)
          .message("")
          .code(200)
          .body(responseBody)
          .build()

      mockCall.execute() returns response

      ItemCatalogService[IO](client).find(title).unsafeToFuture().futureValue shouldBe Some(item)
    }
  }

  it should "return none when the item is not found" in {
    forAll { (title: ItemTitle) =>

      val client = mock[OkHttpClient]
      val mockCall = mock[Call]
      client.newCall(any) returns mockCall
      val response =
        new Response
        .Builder()
          .request(new Request.Builder().url("http://foo.com").build())
          .protocol(Protocol.HTTP_2)
          .message("")
          .code(404)
          .build()

      mockCall.execute() returns response

      ItemCatalogService[IO](client).find(title).unsafeToFuture().futureValue shouldBe None
    }
  }

  it should "return an error if the endpoint throws an exception" in {
    forAll { (title: ItemTitle) =>

      val client = mock[OkHttpClient]
      client.newCall(any) throws new IOException()


      assertThrows[EndpointError] {
        Await.result(ItemCatalogService[IO](client).find(title).unsafeToFuture(), 5.seconds)
      }
    }
  }

  it should "return an error when there is a parse error" in {
    forAll { (title: ItemTitle) =>

      val client = mock[OkHttpClient]
      val mockCall = mock[Call]
      client.newCall(any) returns mockCall
      val responseBody = ResponseBody.create("a bad response that is not json", MediaType.get("application/json; charset=utf-8"))
      val response =
        new Response
        .Builder()
          .request(new Request.Builder().url("http://foo.com").build())
          .protocol(Protocol.HTTP_2)
          .message("")
          .code(200)
          .body(responseBody)
          .build()

      mockCall.execute() returns response

      assertThrows[ParseError](Await.result(ItemCatalogService[IO](client).find(title).unsafeToFuture(), 2.seconds))
    }
  }
}
