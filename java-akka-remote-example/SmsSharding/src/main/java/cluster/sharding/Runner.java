package cluster.sharding;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.CoordinatedShutdown;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.management.javadsl.AkkaManagement;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class Runner {

    static boolean mngt_started = false;
    public static void main(String[] args) {
        if (args.length == 0) {
            startupClusterNodes(Arrays.asList("2551"));
        } else {
            startupClusterNodes(Arrays.asList(args));
        }
    }

    static ActorRef shardingRegion = null;
    //static ActorRef csmShardingRegion = null;
    //static ActorRef asmShardingRegion = null;

    private static void startupClusterNodes(List<String> ports) {
        System.out.printf("Start cluster on port(s) %s%n", ports);

        ports.forEach(port -> {
            ActorSystem actorSystem = ActorSystem.create("sharding", setupClusterNodeConfig(port));
            actorSystem.actorOf(ClusterListenerActor.props(), "clusterListener");

            if(port.equalsIgnoreCase("2551")) {
                shardingRegion = setupClusterSharding(actorSystem, port);
                actorSystem.actorOf(CipangoDispatcherActor.props(shardingRegion), "cipangoDispatcher");
                if(!mngt_started) {
                    AkkaManagement.get(actorSystem).start();
                    mngt_started = true;
                }

            } else if(port.equalsIgnoreCase("2552")) {
                shardingRegion = setupClusterSharding(actorSystem, port);
            } else {
                shardingRegion = setupClusterSharding(actorSystem, port);
            }


            addCoordinatedShutdownTask(actorSystem, CoordinatedShutdown.PhaseClusterShutdown());
            actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
        });
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                String.format("akka.remote.netty.tcp.port=%s%n", port) +
                        String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }

    private static ActorRef setupClusterSharding(ActorSystem actorSystem, String port) {
        ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem);

        if (port.equalsIgnoreCase("2551")) {
            ActorRef ref =
             ClusterSharding.get(actorSystem).start(
                    "callAs",
                     Props.create(CallAS.class),
                    settings,
                    CallASMessage.messageExtractor()
            );
            return ref;
        }

        else if (port.equalsIgnoreCase("2552")){
            return ClusterSharding.get(actorSystem).start(
                    "callAs",
                    Props.create(CallAS.class),
                    settings,
                    CallASMessage.messageExtractor()
            );
        }
        else
        {
            return ClusterSharding.get(actorSystem).start(
                    "callAs",
                    ASMActor.props(),
                    settings,
                    CallASMessage.messageExtractor()
            );
        }


    }
    private static void addCoordinatedShutdownTask(ActorSystem actorSystem, String coordindateShutdownPhase) {
        CoordinatedShutdown.get(actorSystem).addTask(
                coordindateShutdownPhase,
                coordindateShutdownPhase,
                () -> {
                    actorSystem.log().warning("Coordinated shutdown phase {}", coordindateShutdownPhase);
                    return CompletableFuture.completedFuture(Done.getInstance());
                });
    }
}