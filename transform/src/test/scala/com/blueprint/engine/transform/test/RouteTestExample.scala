package com.blueprint.engine.transform.test

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.blueprint.engine.transform.http.WebServerExample
import org.scalatest.{Matchers, WordSpec}

import scala.util.Random

/**
  * Created by raduchilom on 18/07/2017.
  */
class RouteTestExample extends WordSpec with Matchers with ScalatestRouteTest {

  "The service" should {

    "return a greeting for GET requests to the /hello path" in {
      Get("/hello") ~> WebServerExample.route ~> check {
        responseAs[String] shouldEqual "<h1>Say hello to akka-http</h1>"
      }
    }

    "return an item for GET requests to the /item/LongNumber path" in {
      val id = Random.nextInt(100)
      Get(s"/item/$id") ~> WebServerExample.route ~> check {
        responseAs[String] shouldEqual ("{\"name\":\"test\",\"id\":" + id + "}")
      }
    }

  }

}
