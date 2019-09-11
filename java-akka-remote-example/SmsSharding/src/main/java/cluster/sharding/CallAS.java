package cluster.sharding;

import akka.actor.*;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class CallAS extends AbstractPersistentActor {

//public class CallAS extends AbstractLoggingActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private CallAsEntity entity;
    private final FiniteDuration receiveTimeout = Duration.create(6, TimeUnit.SECONDS);

    private ActorRef cipangoActorRef = null;

    private int myCount = 0;

    @Override
    public String persistenceId() {
        return "CallAS-" + getSelf().path().name();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder().match(Integer.class, this::updateState).build();
    }


    void updateState(int myCount) {

        log.error("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& - Switch over happened");

        this.myCount += myCount;
    }


    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(CallASMessage.ICMRequestMessage.class, this::processIcmRequest)
                .match(CallASMessage.CSMRequestMessage.class, this::processCsmRequest)
                .match(CallASMessage.ASMRequestMessage.class, this::processAsmRequest)
                .match(CallASMessage.CSMResponseMessage.class, this::processCsmResponse)
                .match(CallASMessage.ICMResponseMessage.class, this::processIcmResponse)
                .matchEquals(ReceiveTimeout.getInstance(), t -> passivate())
                .build();
    }

    private void processIcmResponse(CallASMessage.ICMResponseMessage command) {
        log.info("ICM Response received ICM reference is this ************************ " + this.getSelf().toString() + " &&&&& " + ++myCount);
        log.info("{} <- {}", command, sender());
        entity = command.entity;
        final CallASMessage.ICMResponseMessage cipangoResp = new CallASMessage.ICMResponseMessage(command.entity);
        log.info("Processing ICM Response --------------------> ");
        cipangoActorRef.tell(cipangoResp, self());
    }

    private void processCsmResponse(CallASMessage.CSMResponseMessage command) {
        log.info("CSM Response received CSM reference is this ************************ " + this.getSelf().toString() + " &&&&& " + ++myCount);
        log.info("{} <- {}", command, sender());
        entity = command.entity;
        final CallASMessage.ICMResponseMessage icmResp = new CallASMessage.ICMResponseMessage(command.entity);
        log.info("Processing CSM Response --------------------> ");
        Runner.shardingRegion.tell(icmResp, self());
    }

    private void processCsmRequest(CallASMessage.CSMRequestMessage command) {
        log.info("CSM Request received CSM reference is this ************************ " + this.getSelf().toString() + " &&&&& " + ++myCount);
        log.info("{} <- {}", command, sender());
        entity = command.entity;
        final CallASMessage.ASMRequestMessage asmMessage = new CallASMessage.ASMRequestMessage(command.entity);
        log.info("Processing CSM Request --------------------> ");
        Runner.shardingRegion.tell(asmMessage, self());
    }

    private void processAsmRequest(CallASMessage.ASMRequestMessage command) {

        log.info("ASM Request received ASM reference is this ************************ " + this.getSelf().toString() + " &&&&& " + ++myCount);
        log.info("{} <- {}", command, sender());
        entity = command.entity;
        final CallASMessage.CSMResponseMessage msg = new CallASMessage.CSMResponseMessage(command.entity);
        log.info("Processing ASM Request --------------------> ");
        Runner.shardingRegion.tell(msg, self());
    }

    private void processIcmRequest(CallASMessage.ICMRequestMessage command) {

        log.info("ICM Request received ICM reference is this ************************ " + this.getSelf().toString() + " &&&&& " + ++myCount);
        cipangoActorRef = getSender();
        log.info("{} <- {}", command, sender());
        entity = command.entity;
        final CallASMessage.CSMRequestMessage msg = new CallASMessage.CSMRequestMessage(command.entity);
        log.info("Processing ICM Request --------------------> ");

        Runner.shardingRegion.tell(msg, self());


    }


    private void passivate() {
        context().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), self());
    }

    @Override
    public void preStart() {
        log.info("Start");
        //context().setReceiveTimeout(receiveTimeout);
    }

    @Override
    public void postStop() {
        log.info("Stopping My reference is this ************************ " + this.getSelf().toString() + " &&&&& " + myCount);
        log.info("Stop {}", entity == null ? "(not initialized)" : entity.id);
    }

    static Props props() {
        return Props.create(ICMActor.class);
    }
}