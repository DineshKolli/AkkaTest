// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sms.proto

package example.akka.actortest.grpc;

public interface SmsReplyOrBuilder extends
    // @@protoc_insertion_point(interface_extends:actortest.SmsReply)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string from = 1;</code>
   */
  String getFrom();
  /**
   * <code>string from = 1;</code>
   */
  com.google.protobuf.ByteString
      getFromBytes();

  /**
   * <code>string to = 2;</code>
   */
  String getTo();
  /**
   * <code>string to = 2;</code>
   */
  com.google.protobuf.ByteString
      getToBytes();

  /**
   * <code>string sms = 3;</code>
   */
  String getSms();
  /**
   * <code>string sms = 3;</code>
   */
  com.google.protobuf.ByteString
      getSmsBytes();
}
