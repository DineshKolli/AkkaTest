package example.akka.remote.shared;

import java.io.Serializable;

public class SmsValidationMessage {

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

    public static class DncResponse implements Serializable {
        private String message;

        public String getFrom() {
            return from;
        }

        private String from;

        public DncResponse(String from, String message) {
            this.message = message;
            this.from = from;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class CreditCheckResponse implements Serializable {
        private String message;
        private String from;

        public CreditCheckResponse(String from, String message) {
            this.message = message;
            this.from = from;
        }

        public String getMessage() {
            return message;
        }

        public String getFrom() {
            return from;
        }
    }


    public static class ValidationResponse implements Serializable {
        private String message;

        public ValidationResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    /*
    public static class Sum implements Serializable {
        private int first;
        private int second;

        public Sum(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int getFirst() {
            return first;
        }

        public int getSecond() {
            return second;
        }
    }

    public static class Result implements Serializable {
        private int result;

        public Result(int result) {
            this.result = result;
        }

        public int getResult() {
            return result;
        }
    }
    */

}
