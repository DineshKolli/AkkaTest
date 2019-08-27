#!/bin/bash

MODE=$1

LOG_LEVEL="INFO"
LOCAL_IP="172.31.5.242"

SEED_NODE_PORT="2556"
VAL_ROUTER_PORT="2559"
VAL_DISPATCHER_PORT="2800"

DAO_IP="172.31.15.218"
DAOPORT="5001"

if [[ ${MODE} == "" ]]
then
        java -jar -Dakka.remote.netty.tcp.hostname=$LOCAL_IP -Dakka.loglevel=$LOG_LEVEL -Dakka.cluster.seed-nodes.0=akka.tcp://SmsValidationCluster@$LOCAL_IP:$SEED_NODE_PORT SmsValidator-1.0-SNAPSHOT-allinone.jar DAOIP $DAO_IP DAOPORT $DAOPORT $VAL_DISPATCHER_PORT $VAL_ROUTER_PORT $SEED_NODE_PORT
else
        java -jar -Dakka.remote.netty.tcp.hostname=$LOCAL_IP -Dakka.loglevel=$LOG_LEVEL -Dakka.cluster.seed-nodes.0=akka.tcp://SmsValidationCluster@$LOCAL_IP:$SEED_NODE_PORT SmsValidator-1.0-SNAPSHOT-allinone.jar DAOIP $DAO_IP DAOPORT $DAOPORT $MODE
fi
