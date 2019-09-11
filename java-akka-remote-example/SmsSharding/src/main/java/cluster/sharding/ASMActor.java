package cluster.sharding;

import akka.actor.AbstractLoggingActor;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.sharding.ShardRegion;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

class ASMActor extends AbstractLoggingActor {
    private CallAsEntity entity;
    private final FiniteDuration receiveTimeout = Duration.create(60, TimeUnit.SECONDS);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CallASMessage.CSMRequestMessage.class, this::command)
                .build();
    }

    private void command(CallASMessage.CSMRequestMessage command) {
        log().info("{} <- {}", command, sender());

            entity.value = command.entity.value;
            final CallASMessage.ASMRequestMessage asmMessage = new CallASMessage.ASMRequestMessage(command.entity);
            log().info("Received ASM message from CSM -------------> ");
            //sender().tell(asmMessage, self());
    }



    private void passivate() {
        context().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), self());
    }

    @Override
    public void preStart() {
        log().info("Start");
        context().setReceiveTimeout(receiveTimeout);
    }

    @Override
    public void postStop() {
        log().info("Stop {}", entity == null ? "(not initialized)" : entity.id);
    }

    static Props props() {
        return Props.create(ASMActor.class);
    }
}