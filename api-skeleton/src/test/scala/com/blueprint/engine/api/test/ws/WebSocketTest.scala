package com.blueprint.engine.api.test.ws

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import com.blueprint.engine.api.ws.WebSocketExample
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

/**
  * Created by raduchilom on 19/07/2017.
  */
class WebSocketTest extends WordSpec with Matchers with ScalatestRouteTest {

  "web socket route" should {
    "respond" in {

      val wsClient = WSProbe()

      // WS creates a WebSocket request for testing
      WS("/greeter", wsClient.flow) ~> WebSocketExample.websocketRoute ~>
        check {
          // check response for WS Upgrade headers
          isWebSocketUpgrade shouldEqual true

          // manually run a WS conversation
          wsClient.sendMessage("Peter")
          wsClient.expectMessage("Hello Peter!")

          wsClient.sendMessage(BinaryMessage(ByteString("abcdef")))
          wsClient.expectNoMessage(100.millis)

          wsClient.sendMessage("John")
          wsClient.expectMessage("Hello John!")

          wsClient.sendCompletion()
          wsClient.expectCompletion()
        }
      true
    }
  }

}
