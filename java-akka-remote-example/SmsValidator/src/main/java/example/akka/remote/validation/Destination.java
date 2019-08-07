package example.akka.remote.validation;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.remote.shared.SmsDaoMessage;

public class Destination extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public Destination() {
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // register to the path
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("onReceive({} and my node is {})", message, getSelf().toString());
        if (message instanceof SmsDaoMessage.Message) {

            log.info("Got a Sms Dao Message");

            String from = ((SmsDaoMessage.Message) message).getFromNumber();
            String to = ((SmsDaoMessage.Message) message).getToNumber();
            String smsMessage = ((SmsDaoMessage.Message) message).getSmsMessage();

            SmsDaoMessage.Message daoMessage = new SmsDaoMessage.Message(from, to, smsMessage);

            //selection.tell(daoMessage, getSelf());
        } else {

            log.info("Unknown message -------------------> " + message);
            unhandled(message);
        }
    }
}
