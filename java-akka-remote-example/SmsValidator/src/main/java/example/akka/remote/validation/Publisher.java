package example.akka.remote.validation;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import example.akka.remote.shared.SmsDaoMessage;

public class Publisher extends AbstractActor {

    // activate the extension
    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        SmsDaoMessage.Message.class,
                        in -> {
                            boolean localAffinity = false;
                            mediator.tell(
                                    new DistributedPubSubMediator.Send("/user/SmsValidationInterfaceDispatcher", in, localAffinity),
                                    getSelf());
                        })
                .build();
    }
}
