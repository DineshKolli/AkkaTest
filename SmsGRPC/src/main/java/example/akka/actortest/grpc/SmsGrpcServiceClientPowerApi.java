
// Generated by Akka gRPC. DO NOT EDIT.
package example.akka.actortest.grpc;

import akka.grpc.javadsl.SingleResponseRequestBuilder;
import akka.grpc.javadsl.StreamResponseRequestBuilder;

import static example.akka.actortest.grpc.SmsGrpcService.Serializers.*;

public abstract class SmsGrpcServiceClientPowerApi {
  
    /**
     * Lower level "lifted" version of the method, giving access to request metadata etc.
     * prefer sendSms(example.akka.actortest.grpc.SmsRequest) if possible.
     */
    
      public SingleResponseRequestBuilder<example.akka.actortest.grpc.SmsRequest, example.akka.actortest.grpc.SmsReply> sendSms()
    
    {
        throw new UnsupportedOperationException();
    }
  
}
