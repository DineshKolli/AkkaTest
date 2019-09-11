package com.dynamic.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.remote.shared.Call;

public class CallLeg extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private String from;
    private String to;
    private String callId;

    private String state;


    @Override
    public void preStart() {
        log.info("Started Api Service Actor");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Call.CreateCall) {
            //System.out.println(" -------------> Actor Created");
            Call.CreateCall obj = (Call.CreateCall) message;
            from = obj.getFromHeader();
            to = obj.getToHeader();
            callId = obj.getCallID();

            state = "Created";

            Thread.sleep(DynamicActorMain.ACTOR_SLEEP);

            getSender().tell(new Call.CreateCallResponse(state, callId), self());
        }
        else if (message instanceof Call.DeleteCall) {
            //System.out.println(" -------------> Actor Deleted");
            Call.DeleteCall obj = (Call.DeleteCall) message;
            from = obj.getFromHeader();
            to = obj.getToHeader();
            callId = obj.getCallID();



            state = "Destroyed";
            getSender().tell(new Call.DeleteCallResponse(state, callId), self());
        }



    }
}
