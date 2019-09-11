package example.akka.remote.shared;

import java.io.Serializable;

public class Call {

    public static class CreateCall implements Serializable {

        public String getFromHeader() {
            return fromHeader;
        }

        public String getToHeader() {
            return toHeader;
        }

        public String getCallID() {
            return callID;
        }

        private String fromHeader;
        private String toHeader;
        private String callID;

        public CreateCall(String fromHeader, String toHeader, String callID)
        {
            this.fromHeader = fromHeader;
            this.toHeader = toHeader;
            this.callID = callID;
        }

    }

    public static class DeleteCall implements Serializable {

        public String getFromHeader() {
            return fromHeader;
        }

        public String getToHeader() {
            return toHeader;
        }

        public String getCallID() {
            return callID;
        }

        private String fromHeader;
        private String toHeader;
        private String callID;

        public DeleteCall(String fromHeader, String toHeader, String callID)
        {
            this.fromHeader = fromHeader;
            this.toHeader = toHeader;
            this.callID = callID;
        }

    }

    public static class CreateCallResponse implements Serializable {

        public String getMessage() {
            return message;
        }
        private String message;

        public String getCallId() {
            return callId;
        }

        private String callId;
        public CreateCallResponse(String message, String callId)
        {
            this.message = message;
            this.callId = callId;
        }

    }

    public static class DeleteCallResponse implements Serializable {

        public String getMessage() {
            return message;
        }
        private String message;

        public String getCallId() {
            return callId;
        }

        private String callId;
        public DeleteCallResponse(String message, String callId)
        {
            this.message = message;
            this.callId = callId;
        }

    }

}
