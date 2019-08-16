/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */

//#full-server
package example.akka.actortest;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.akka.actortest.grpc.SmsGrpcService;

import java.util.concurrent.CompletionStage;

class SmsServer {
  public static void main(String[] args) throws Exception {
    // important to enable HTTP/2 in ActorSystem's config
    Config conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
            .withFallback(ConfigFactory.defaultApplication());

    // Akka ActorSystem Boot
    ActorSystem sys = ActorSystem.create("SmsServer", conf);

    run(sys).thenAccept(binding -> {
      System.out.println("gRPC server bound to: " + binding.localAddress());
    });

    // ActorSystem threads will keep the app alive until `system.terminate()` is called
  }

  public static CompletionStage<ServerBinding> run(ActorSystem sys) throws Exception {
    Materializer mat = ActorMaterializer.create(sys);

    // Instantiate implementation
    SmsGrpcService impl = new SmsGrpcServiceImpl(mat,sys);

    return Http.get(sys).bindAndHandleAsync(
      SmsServiceHandlerFactory.create(impl, mat, sys),
      ConnectHttp.toHost("127.0.0.1", 8080),
      mat);
  }
}
//#full-server
