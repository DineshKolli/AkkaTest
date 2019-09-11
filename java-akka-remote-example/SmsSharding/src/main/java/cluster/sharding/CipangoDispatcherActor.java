package cluster.sharding;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

class CipangoDispatcherActor extends AbstractLoggingActor {
    private final ActorRef shardRegion;
    private Cancellable ticker;
    private FiniteDuration tickInterval = Duration.create(6, TimeUnit.SECONDS);
    private int messageNumber;
    private final Receive sending;
    private final Receive receiving;

    {
        sending = receiveBuilder()
                .matchEquals("tick", t -> tickSending())
                .match(CallASMessage.ICMRequestMessage.class, this::commandAckSending)
                .build();

        receiving = receiveBuilder()
                .matchEquals("tick", t -> tickReceiving())
                .match(CallASMessage.ICMResponseMessage.class, this::commandAckReceiving)
                .build();
    }

    private CipangoDispatcherActor(ActorRef shardRegion) {
        this.shardRegion = shardRegion;
    }

    @Override
    public Receive createReceive() {
        return sending;
    }

    private void commandAckSending(CallASMessage.ICMRequestMessage commandAck) {
        log().warning("(late) {} <- {}", commandAck, sender());
    }

    private void tickSending() {
        CallASMessage.ICMRequestMessage command = command();
        log().info("Sending ICM message from cipango -----------------------> {} -> {}", command, shardRegion);
        shardRegion.tell(command, self());
        getContext().become(receiving);
    }

    private void commandAckReceiving(CallASMessage.ICMResponseMessage commandAck) {
        log().info("{} <- {}", commandAck, sender());
        getContext().become(sending);
    }

    private void tickReceiving() {
        log().warning("No response to last command {}", messageNumber);
        getContext().become(sending);
    }

    private CallASMessage.ICMRequestMessage command() {
        return new CallASMessage.ICMRequestMessage(randomEntity());
    }

    private CallAsEntity randomEntity() {
        return new CallAsEntity(Random.entityId(1, 100), new CallAsEntity.Value(String.format("%s-%d", self().path().name(), ++messageNumber)));
    }

    @Override
    public void preStart() {
        log().info("Start");
        ticker = context().system().scheduler().schedule(
                Duration.Zero(),
                tickInterval,
                self(),
                "tick",
                context().system().dispatcher(),
                null
        );
    }

    @Override
    public void postStop() {
        log().info("Stop");
        ticker.cancel();
    }

    static Props props(ActorRef shardRegion) {
        return Props.create(CipangoDispatcherActor.class, shardRegion);
    }
}