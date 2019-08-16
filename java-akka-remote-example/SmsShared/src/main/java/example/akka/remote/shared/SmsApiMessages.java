package example.akka.remote.shared;

import java.io.Serializable;

public class SmsApiMessages {

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

        public Message(String fromNumber, String toNumber, String smsMessage)
        {
            this.fromNumber = fromNumber;
            this.toNumber = toNumber;
            this.smsMessage = smsMessage;
        }

    }

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
}
