package example.akka.remote.validation;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.remote.shared.LoggingActor;
import example.akka.remote.shared.SmsDaoMessage;
import example.akka.remote.shared.SmsValidationMessage;

public class SmsValidationInterfaceDispatcher extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ActorSelection selection = getContext().actorSelection("akka.tcp://SmsDaoCluster@127.0.0.1:2565/user/SmsDaoRouter");

    public SmsValidationInterfaceDispatcher() {
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // register to the path
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
    }


    @Override
    public void onReceive(Object message) throws Exception {
        log.info("onReceive({})", message);

        if (message instanceof SmsDaoMessage.Message) {



            String from = ((SmsDaoMessage.Message) message).getFromNumber();
            String to = ((SmsDaoMessage.Message) message).getToNumber();
            String smsMessage = ((SmsDaoMessage.Message) message).getSmsMessage();

            log.info("Got a Sms Dao Message for from ----------------------------------------------------------------------------- > " + from);
            SmsDaoMessage.Message daoMessage = new SmsDaoMessage.Message(from, to, smsMessage);

            selection.tell(daoMessage, getSelf());
        }
        else if (message instanceof SmsDaoMessage.Response) {
            log.info( (String) ((SmsDaoMessage.Response) message).getMessage()  );
        }
        else {

           log.info("Unknown message -------------------> " + message);
            unhandled(message);
        }
    }



}
