package cluster.sharding;

import akka.actor.AbstractLoggingActor;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.sharding.ShardRegion;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

class ICMActor extends AbstractLoggingActor {
    private CallAsEntity entity;
    private final FiniteDuration receiveTimeout = Duration.create(6, TimeUnit.SECONDS);

    private int myCount = 0;

    @Override
    public Receive createReceive() {


        return receiveBuilder()
                .match(CallASMessage.ICMRequestMessage.class, this::processIcmMessage)
                .match(CallASMessage.CSMRequestMessage.class, this::processCsmMessage)
                .build();
    }

    private void processCsmMessage(CallASMessage.CSMRequestMessage command) {
        log().error("This should not come here. ++++++++++++++++++++++++++++++++++++++++++++++ ");

        //ActorRef ref = getContext().actorOf(Props.create(CSMActor.class), "CSMActor");
        //ref.tell(command, self());

    }

    private void processIcmMessage(CallASMessage.ICMRequestMessage command) {

        log().info("My reference is this ************************ " + this.getSelf().toString() + " &&&&& " + myCount++);
        log().info("{} <- {}", command, sender());
            entity = command.entity;
            final CallASMessage.CSMRequestMessage msg = new CallASMessage.CSMRequestMessage(command.entity);
            log().info("processing icm Message --------------------> .");
            Runner.shardingRegion.tell(msg, self());
    }


    private void passivate() {
        context().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), self());
    }

    @Override
    public void preStart() {
        log().info("Start");
        //context().setReceiveTimeout(receiveTimeout);
    }

    @Override
    public void postStop() {
        log().info("Stopping My reference is this ************************ " + this.getSelf().toString() + " &&&&& " + myCount++);

        log().info("Stop {}", entity == null ? "(not initialized)" : entity.id);
    }

    static Props props() {
        return Props.create(ICMActor.class);
    }
}