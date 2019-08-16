package example.akka.remote.smsDao;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

public class SmsDaoRouter extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


    ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(),
            "SmsDaoBackEndRouter");
    @Override
    public void preStart() {
       log.info("SmsDaoRouter Started ============================= ");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        backend.forward(message, getContext());
    }
}