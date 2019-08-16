/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */

//#full-client
package example.akka.actortest;

import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import example.akka.actortest.grpc.SmsGrpcService;
import example.akka.actortest.grpc.SmsGrpcServiceClient;
import example.akka.actortest.grpc.SmsReply;
import example.akka.actortest.grpc.SmsRequest;
import io.grpc.StatusRuntimeException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;


class SmsClient {
  public static void main(String[] args) throws Exception {

    String serverHost = "127.0.0.1";
    int serverPort = 8080;

    ActorSystem system = ActorSystem.create("SmsClient");
    Materializer materializer = ActorMaterializer.create(system);

    GrpcClientSettings settings = GrpcClientSettings.fromConfig(SmsGrpcService.name, system);
    SmsGrpcServiceClient client = null;
    try {
      client = SmsGrpcServiceClient.create(settings, materializer, system.dispatcher());
      singleRequestReply(client);
    } catch (StatusRuntimeException e) {
      System.out.println("Status: " + e.getStatus());
    } catch (Exception e)  {
      System.out.println(e);
      e.printStackTrace();
    } finally {
      if (client != null) client.close();
      system.terminate();
    }

  }

  private static void singleRequestReply(SmsGrpcServiceClient client) throws Exception {

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    System.out.println("Start time is " + dtf.format(now));
    for(int i = 300002; i < 300005; i++) {
      SmsRequest request = SmsRequest.newBuilder().setFrom("" + i).setTo("9986459050").setSms("My SMS" + i).build();
      CompletionStage<SmsReply> reply = client.sendSms(request);
      reply.toCompletableFuture().get(5, TimeUnit.SECONDS);
      //System.out.println("got reply: " + reply.toCompletableFuture().get(5, TimeUnit.SECONDS));
    }
    now = LocalDateTime.now();
    System.out.println("End time is " + dtf.format(now));

  }

}
//#full-client
