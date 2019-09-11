package com.dynamic.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.remote.shared.Call;
import example.akka.remote.shared.SmsApiMessages;
import example.akka.remote.shared.SmsDaoMessage;
import example.akka.remote.shared.SmsValidationMessage;

public class DynamicActorService extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    static volatile int actorCount = 0;

    @Override
    public void preStart() {
        log.info("Started Api Service Actor");
    }

    @Override
    public void onReceive(Object message) throws Exception {
       if (message instanceof Call.CreateCall) {
           ActorRef aRef = getContext().actorOf(Props.create(CallLeg.class), ((Call.CreateCall) message).getCallID());
           aRef.tell(message, getSelf());
           //System.out.println("Created Actor Ref for Call id " + ((Call.CreateCall) message).getCallID());
           actorCount++;
           //System.out.println("================> ");
       }
       else if (message instanceof Call.CreateCallResponse) {

          if(((Call.CreateCallResponse) message).getMessage().equalsIgnoreCase("Created"))
          {
              //actorCount++;
              //System.out.println("================> ");
          }
           //System.out.println("---------------> ");

       }
       else if (message instanceof Call.DeleteCall) {

           //ActorSelection aRef = getContext().actorSelection("/user/"+ ((Call.DeleteCall) message).getCallID());
           ActorRef aRef = getContext().getChild(((Call.DeleteCall) message).getCallID());
           aRef.tell(message, getSelf());
           //System.out.println("Deleted Actor Ref for Call id " + ((Call.DeleteCall) message).getCallID());

           aRef.tell(PoisonPill.getInstance(), ActorRef.noSender());
       }
       else if (message instanceof Call.DeleteCallResponse) {

           if(((Call.DeleteCallResponse) message).getMessage().equalsIgnoreCase("Destroyed"))
           {
               actorCount--;
           }



       }

       else if (message instanceof String) {

           if(((String) message).equalsIgnoreCase("GetCount"))
           {
               System.out.println("Current Actor Count is : " + actorCount);
           }

       }

    }
}
