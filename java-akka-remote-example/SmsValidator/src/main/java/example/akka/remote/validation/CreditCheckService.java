package example.akka.remote.validation;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.remote.shared.LoggingActor;
import example.akka.remote.shared.SmsDaoMessage;
import example.akka.remote.shared.SmsValidationMessage;

public class CreditCheckService extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);



    private ActorRef loggingActor = getContext().actorOf(Props.create(LoggingActor.class), "LoggingActor");

    public void checkCredit(SmsValidationMessage.Message message)
    {

        /*
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

         */
        log.info("Credit Check done for {}", message.getFromNumber());
    }


    @Override
    public void onReceive(Object message) throws Exception {
        log.info("onReceive({})", message);

        if (message instanceof SmsValidationMessage.Message) {

            log.info("Got a Sms in Credit Check Service");

            String from = ((SmsValidationMessage.Message) message).getFromNumber();
            String to = ((SmsValidationMessage.Message) message).getToNumber();
            String smsMessage = ((SmsValidationMessage.Message) message).getSmsMessage();

            checkCredit((SmsValidationMessage.Message) message);

            getSender().tell(new SmsValidationMessage.CreditCheckResponse(from, "success"), getSelf());
        }
        else {
            unhandled(message);
        }
    }



}
