/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */

//#full-service-impl
package example.akka.actortest;

import akka.NotUsed;
import akka.grpc.javadsl.Metadata;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import example.akka.actortest.grpc.SmsGrpcServiceClientPowerApi;
import example.akka.actortest.grpc.SmsReply;
import example.akka.actortest.grpc.SmsRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SmsGrpServicePowerApiImpl extends SmsGrpcServiceClientPowerApi {
  private final Materializer mat;

  public SmsGrpServicePowerApiImpl(Materializer mat) {
   this.mat = mat;
  }

  //@Override
  public CompletionStage<SmsReply> sendSms(SmsRequest in, Metadata metadata) {
    String greetee = authTaggedName(in, metadata);
    System.out.println("sayHello to " + greetee);
    SmsReply reply = SmsReply.newBuilder().setFrom("Hello, " + greetee).build();
    return CompletableFuture.completedFuture(reply);
  }

  //@Override
  public CompletionStage<SmsReply> itKeepsTalking(Source<SmsRequest, NotUsed> in, Metadata metadata) {
    System.out.println("sayHello to in stream...");
    return in.runWith(Sink.seq(), mat)
      .thenApply(elements -> {
        String elementsStr = elements.stream().map(elem -> authTaggedName(elem, metadata))
            .collect(Collectors.toList()).toString();
        return SmsReply.newBuilder().setFrom("Hello, " + elementsStr).build();
      });
  }

  //@Override
  public Source<SmsReply, NotUsed> itKeepsReplying(SmsRequest in, Metadata metadata) {
    String greetee = authTaggedName(in, metadata);
    System.out.println("sayHello to " + greetee + " with stream of chars");
    List<Character> characters = ("Hello, " + greetee)
        .chars().mapToObj(c -> (char) c).collect(Collectors.toList());
    return Source.from(characters)
      .map(character -> {
        return SmsReply.newBuilder().setFrom(String.valueOf(character)).build();
      });
  }

  //@Override
  public Source<SmsReply, NotUsed> streamHellos(Source<SmsRequest, NotUsed> in, Metadata metadata) {
    System.out.println("sayHello to stream...");
    return in.map(request -> SmsReply.newBuilder().setFrom("Hello, " + authTaggedName(request, metadata)).build());
  }

  // Bare-bones just for GRPC metadata demonstration purposes
  private boolean isAuthenticated(Metadata metadata) {
    return metadata.getText("authorization").isPresent();
  }

  private String authTaggedName(SmsRequest in, Metadata metadata) {
    boolean authenticated = isAuthenticated(metadata);
    return String.format("%s (%sauthenticated)", in.getFrom(), isAuthenticated(metadata) ? "" : "not ");
  }
}
//#full-service-impl
