/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */

//#full-service-impl
package example.akka.actortest;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.Materializer;
import example.akka.actortest.grpc.SmsGrpcService;
import example.akka.actortest.grpc.SmsReply;
import example.akka.actortest.grpc.SmsRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class SmsGrpcServiceImpl implements SmsGrpcService {
  private final Materializer mat;
  private final ActorSystem system;
  private final ActorRef smsGrpcActor;

  public SmsGrpcServiceImpl(Materializer mat, ActorSystem system) {
   this.system = system;
   this.smsGrpcActor = system.actorOf(Props.create(SmsGrpcActor.class));
   this.mat = mat;
  }

  @Override
  public CompletionStage<SmsReply> sendSms(SmsRequest in) {
    //System.out.println("Send Sms for from " + in.getFrom());
    this.smsGrpcActor.tell(in, ActorRef.noSender());
    SmsReply reply = SmsReply.newBuilder().setFrom(in.getFrom()).setTo(in.getTo()).setSms(in.getSms()).build();
    return CompletableFuture.completedFuture(reply);
  }
}
//#full-service-impl
