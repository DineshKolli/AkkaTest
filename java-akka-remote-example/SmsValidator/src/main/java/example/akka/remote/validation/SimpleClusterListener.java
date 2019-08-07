package example.akka.remote.validation;

import akka.actor.AbstractActor;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.time.Duration;


public class SimpleClusterListener extends AbstractActor {
    private final Cluster cluster = Cluster.get(context().system());
    private Cancellable showClusterStateCancelable;

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }

    private void showClusterState(ShowClusterState showClusterState) {
        log.info("{} sent to {}", showClusterState, cluster.selfMember());
        logClusterMembers(cluster.state());
        showClusterStateCancelable = null;
    }

    private void logClusterEvent(Object clusterEventMessage) {
        log.info("{} sent to {}", clusterEventMessage, cluster.selfMember());
        logClusterMembers();
    }

    @Override
    public void preStart() {
        log.debug("Start");
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.ClusterDomainEvent.class);
    }

    @Override
    public void postStop() {
        log.debug("Stop");
        cluster.unsubscribe(self());
    }

    static Props props() {
        return Props.create(SimpleClusterListener.class);
    }

    private void logClusterMembers() {
        logClusterMembers(cluster.state());

        if (showClusterStateCancelable == null) {
            showClusterStateCancelable = context().system().scheduler().scheduleOnce(
                    Duration.ofSeconds(15),
                    self(),
                    new ShowClusterState(),
                    context().system().dispatcher(),
                    null);
        }
    }

    private void logClusterMembers(CurrentClusterState currentClusterState)
    {

    }

    private static class ShowClusterState {
        @Override
        public String toString() {
            return ShowClusterState.class.getSimpleName();
        }
    }
}

