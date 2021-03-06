
// Generated by Akka gRPC. DO NOT EDIT.
package example.akka.actortest.grpc;

import akka.annotation.*;
import akka.grpc.internal.*;
import akka.grpc.GrpcClientSettings;
import akka.grpc.javadsl.AkkaGrpcClient;
import akka.grpc.javadsl.SingleResponseRequestBuilder;
import akka.grpc.javadsl.StreamResponseRequestBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;

import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;

import static example.akka.actortest.grpc.SmsGrpcService.Serializers.*;

import scala.concurrent.ExecutionContext;
import scala.compat.java8.FutureConverters;

public abstract class SmsGrpcServiceClient extends SmsGrpcServiceClientPowerApi implements SmsGrpcService, AkkaGrpcClient {
  public static final SmsGrpcServiceClient create(GrpcClientSettings settings, Materializer mat, ExecutionContext ec) {
    return new DefaultSmsGrpcServiceClient(settings, mat, ec);
  }

  protected final static class DefaultSmsGrpcServiceClient extends SmsGrpcServiceClient {

      private final ClientState clientState;
      private final GrpcClientSettings settings;
      private final io.grpc.CallOptions options;
      private final Materializer mat;
      private final ExecutionContext ec;

      private DefaultSmsGrpcServiceClient(GrpcClientSettings settings, Materializer mat, ExecutionContext ec) {
        this.settings = settings;
        this.mat = mat;
        this.ec = ec;
        this.clientState = new ClientState(settings, mat, ec);
        this.options = NettyClientUtils.callOptions(settings);

        if (mat instanceof ActorMaterializer) {
          ((ActorMaterializer) mat).system().getWhenTerminated().whenComplete((v, e) -> close());
        }
      }

  
    
      private final SingleResponseRequestBuilder<example.akka.actortest.grpc.SmsRequest, example.akka.actortest.grpc.SmsReply> sendSmsRequestBuilder(scala.concurrent.Future<ManagedChannel> channel){
        return new JavaUnaryRequestBuilder<>(sendSmsDescriptor, channel, options, settings, ec);
      }
    
  

      

        /**
         * For access to method metadata use the parameterless version of sendSms
         */
        public java.util.concurrent.CompletionStage<example.akka.actortest.grpc.SmsReply> sendSms(example.akka.actortest.grpc.SmsRequest request) {
          return sendSms().invoke(request);
        }

        /**
         * Lower level "lifted" version of the method, giving access to request metadata etc.
         * prefer sendSms(example.akka.actortest.grpc.SmsRequest) if possible.
         */
        
          public SingleResponseRequestBuilder<example.akka.actortest.grpc.SmsRequest, example.akka.actortest.grpc.SmsReply> sendSms()
        
        {
          return clientState.withChannel( this::sendSmsRequestBuilder);
        }
      

      
        private static MethodDescriptor<example.akka.actortest.grpc.SmsRequest, example.akka.actortest.grpc.SmsReply> sendSmsDescriptor =
          MethodDescriptor.<example.akka.actortest.grpc.SmsRequest, example.akka.actortest.grpc.SmsReply>newBuilder()
            .setType(
   MethodDescriptor.MethodType.UNARY 
  
  
  
)
            .setFullMethodName(MethodDescriptor.generateFullMethodName("actortest.SmsGrpcService", "sendSms"))
            .setRequestMarshaller(new ProtoMarshaller<example.akka.actortest.grpc.SmsRequest>(SmsRequestSerializer))
            .setResponseMarshaller(new ProtoMarshaller<example.akka.actortest.grpc.SmsReply>(SmsReplySerializer))
            .setSampledToLocalTracing(true)
            .build();
        

      /**
       * Initiates a shutdown in which preexisting and new calls are cancelled.
       */
      public java.util.concurrent.CompletionStage<akka.Done> close() {
        return clientState.closeCS() ;
      }

     /**
      * Returns a CompletionState that completes successfully when shutdown via close()
      * or exceptionally if a connection can not be established after maxConnectionAttempts.
      */
      public java.util.concurrent.CompletionStage<akka.Done> closed() {
        return clientState.closedCS();
      }
  }

}



