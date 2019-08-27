#!/bin/bash

MODE=$1

LOG_LEVEL="ERROR"
LOCAL_IP="172.31.5.242"
SEED_NODE_PORT="2900"
SEED_NODE_IP="172.31.5.242"

POSTGRES_IP="172.31.12.148"
POSTGRES_PORT="5432"
SMS_DATABASE_NAME="testdb"
JDBC_THREADS="45"

SMS_DAO_ROUTER_PORT="5001"
ACTOR_COUNT="10"

MIN_POOL_SIZE=10
MAX_POOL_SIZE=45

#-Dmy-dispatcher.thread-pool-executor.max-pool-size-min=200
#-Dmy-dispatcher.thread-pool-executor.max-pool-size-max=400

if [[ ${MODE} == "" ]]
then
            java -jar -Dmy-dispatcher.thread-pool-executor.max-pool-size-min=$MIN_POOL_SIZE -Dmy-dispatcher.thread-pool-executor.max-pool-size-max=$MAX_POOL_SIZE -Dakka.actor.deployment.cluster.allow-local-routees=on -Dakka.remote.netty.tcp.hostname=$LOCAL_IP -Dakka.loglevel=$LOG_LEVEL -Dakka.cluster.seed-nodes.0="akka.tcp://SmsDaoCluster@$SEED_NODE_IP:$SEED_NODE_PORT" -Ddatabase.slick-postgres.db.numThreads=$JDBC_THREADS -Ddatabase.slick-postgres.db.properties.url="jdbc:postgresql://$POSTGRES_IP:$POSTGRES_PORT/testdb" SmsDao-1.0-SNAPSHOT-allinone.jar ACTOR_COUNT $ACTOR_COUNT $SEED_NODE_PORT $SMS_DAO_ROUTER_PORT
    else
               java -jar -Dmy-dispatcher.executor="fork-join-executor" -Dmy-dispatcher.fork-join-executor.parallelism-min=$MIN_POOL_SIZE -Dmy-dispatcher.fork-join-executor.parallelism-factor="2.0" -Dmy-dispatcher.fork-join-executor.parallelism-max=$MAX_POOL_SIZE -Dakka.remote.netty.tcp.hostname=$LOCAL_IP -Dakka.loglevel=$LOG_LEVEL  -Dakka.cluster.seed-nodes.0="akka.tcp://SmsDaoCluster@$SEED_NODE_IP:$SEED_NODE_PORT" -Ddatabase.slick-postgres.db.numThreads=$JDBC_THREADS -Ddatabase.slick-postgres.db.properties.url="jdbc:postgresql://$POSTGRES_IP:$POSTGRES_PORT/testdb" SmsDao-1.0-SNAPSHOT-allinone.jar ACTOR_COUNT $ACTOR_COUNT TEST_MODE 0 $MODE $2 $3
               #java -jar -Dmy-dispatcher.thread-pool-executor.max-pool-size-min=$MIN_POOL_SIZE -Dmy-dispatcher.thread-pool-executor.max-pool-size-max=$MAX_POOL_SIZE -Dakka.remote.netty.tcp.hostname=$LOCAL_IP -Dakka.loglevel=$LOG_LEVEL  -Dakka.cluster.seed-nodes.0="akka.tcp://SmsDaoCluster@$SEED_NODE_IP:$SEED_NODE_PORT" -Ddatabase.slick-postgres.db.numThreads=$JDBC_THREADS -Ddatabase.slick-postgres.db.properties.url="jdbc:postgresql://$POSTGRES_IP:$POSTGRES_PORT/testdb" SmsDao-1.0-SNAPSHOT-allinone.jar ACTOR_COUNT $ACTOR_COUNT $MODE $2 $3
fi
