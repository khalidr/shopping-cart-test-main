package com.example.cart

import cats.effect.IO
import com.example.cart.errors.EndpointError
import io.circe.syntax._
import okhttp3.{Call, MediaType, OkHttpClient, Protocol, Request, Response, ResponseBody}
import org.mockito.ArgumentMatchers.any
import domain._
import io.circe.generic.auto._
import io.circe.syntax._
import scala.concurrent.duration._

import java.io.IOException
import scala.concurrent.Await


class ItemCatalogSpec extends UnitSpec with Encoders with Arbitraries {

  "ItemCatalog.find" should "find an item" in {

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

      ItemCatalog[IO](client).find(title).unsafeToFuture().futureValue shouldBe Some(item)
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

      ItemCatalog[IO](client).find(title).unsafeToFuture().futureValue shouldBe None
    }
  }

  it should "return generate an error if the endpoint throws an exception" in {
    forAll { (title: ItemTitle) =>

      val client = mock[OkHttpClient]
      client.newCall(any) throws new IOException()


      assertThrows[EndpointError] {
        Await.result(ItemCatalog[IO](client).find(title).unsafeToFuture(), 5.seconds )
      }
    }
  }
}
