package example.akka.actortest;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.actortest.grpc.SmsRequest;
import example.akka.remote.shared.SmsApiMessages;
import example.akka.remote.shared.SmsValidationMessage;

public class SmsGrpcActor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorSelection route = getContext().actorSelection("akka.tcp://SmsValidationCluster@127.0.0.1:2559/user/SmsValidationRouter");
    @Override
    public void preStart() {
        log.info("Started Api Service Actor");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SmsRequest) {
            log.info("Got a Sms Api Message, send it to the Validator");
            String from = ((SmsRequest) message).getFrom();
            String to = ((SmsRequest) message).getTo();
            String smsMessage = ((SmsRequest) message).getSms();
            SmsValidationMessage.Message newMessage = new SmsValidationMessage.Message(from, to, smsMessage);
            route.tell(newMessage, getSelf());
        }

        else if (message instanceof SmsValidationMessage.ValidationResponse) {
            String result = ((SmsValidationMessage.ValidationResponse) message).getMessage();
            log.info("Got result back Validator: {}", result);
        }
    }
}