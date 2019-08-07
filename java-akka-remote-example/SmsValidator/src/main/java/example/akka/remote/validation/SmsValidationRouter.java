package example.akka.remote.validation;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import akka.routing.RoundRobinGroup;
import example.akka.remote.shared.SmsValidationMessage;

import java.util.Arrays;
import java.util.List;

public class SmsValidationRouter extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(),
            "SmsValidationBackEndRouter");


//    List<String> paths = Arrays.asList("/user/CommonValidationActor");
 //   ActorRef router4 = getContext().actorOf(new RoundRobinGroup(paths).props(), "router4");


    @Override
    public void preStart() {
       log.info("SmsValidationRouter Started ============================= ");
    }

    @Override
    public void onReceive(Object message) throws Exception {

        backend.forward(message, getContext());
        /*
        log.info("SmsValidationRouter onReceive ------------------- ");
        if (message instanceof SmsValidationMessage.Message) {
            log.info("Got a Sms to SmsValidationRouter, send it to the Validator");
            String from = ((SmsValidationMessage.Message) message).getFromNumber();
            String to = ((SmsValidationMessage.Message) message).getToNumber();
            String smsMessage = ((SmsValidationMessage.Message) message).getSmsMessage();
            SmsValidationMessage.Message newMessage = new SmsValidationMessage.Message(from, to, smsMessage);
            backend.forward(newMessage, getContext());
        }
        else if (message instanceof SmsValidationMessage.ValidationResponse) {
            String result = ((SmsValidationMessage.ValidationResponse) message).getMessage();
            log.info("Got result back Validator: {} ++++++++++++++++++++++++++++++++++++", result);
        }

         */
    }
}