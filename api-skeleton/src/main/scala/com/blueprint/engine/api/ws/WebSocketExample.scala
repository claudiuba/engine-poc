package com.blueprint.engine.api.ws

import akka.http.scaladsl.model.ws.BinaryMessage
import akka.stream.scaladsl.Sink
import scala.io.StdIn

/**
  * Inspired from http://doc.akka.io/docs/akka-http/current/scala/http/websocket-support.html
  */
object WebSocketExample {

  import akka.actor.ActorSystem
  import akka.stream.ActorMaterializer
  import akka.stream.scaladsl.{ Source, Flow }
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model.ws.{ TextMessage, Message }
  import akka.http.scaladsl.server.Directives

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  import Directives._

  def greeter: Flow[Message, Message, Any] =
    Flow[Message].mapConcat {
      case tm: TextMessage =>
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }
  val websocketRoute =
    path("greeter") {
      handleWebSocketMessages(greeter)
    }

  def main(args: Array[String]) {
    val bindingFuture = Http().bindAndHandle(websocketRoute, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()

    import system.dispatcher // for the future transformations
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}