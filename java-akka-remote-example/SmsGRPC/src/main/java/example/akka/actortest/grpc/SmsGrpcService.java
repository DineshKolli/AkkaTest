
// Generated by Akka gRPC. DO NOT EDIT.
package example.akka.actortest.grpc;

import akka.grpc.ProtobufSerializer;
import akka.grpc.javadsl.GoogleProtobufSerializer;


public interface SmsGrpcService {
  

  java.util.concurrent.CompletionStage<example.akka.actortest.grpc.SmsReply> sendSms(example.akka.actortest.grpc.SmsRequest in);
  

  static String name = "actortest.SmsGrpcService";

  public static class Serializers {
    
      public static ProtobufSerializer<example.akka.actortest.grpc.SmsRequest> SmsRequestSerializer = new GoogleProtobufSerializer<>(example.akka.actortest.grpc.SmsRequest.class);
    
      public static ProtobufSerializer<example.akka.actortest.grpc.SmsReply> SmsReplySerializer = new GoogleProtobufSerializer<>(example.akka.actortest.grpc.SmsReply.class);
    
  }
}
