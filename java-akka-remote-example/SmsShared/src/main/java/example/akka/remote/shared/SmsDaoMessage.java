package example.akka.remote.shared;

import java.io.Serializable;

public class SmsDaoMessage {

    public static class Message implements Serializable {

        public String getFromNumber() {
            return fromNumber;
        }

        public String getToNumber() {
            return toNumber;
        }

        public String getSmsMessage() {
            return smsMessage;
        }

        private String fromNumber;
        private String toNumber;
        private String smsMessage;

        public Message(String fromNumber, String toNumber, String smsMessage) {
            this.fromNumber = fromNumber;
            this.toNumber = toNumber;
            this.smsMessage = smsMessage;
        }
    }

    public static class Response implements Serializable {
        private String message;

        public Response(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }



}
