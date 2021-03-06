#!/bin/sh

SEED_INSTANCE=69446
NODES=( 69447 69448 )
SEED_HOST=voxxr-c4
HOSTS=( voxxr-c5 voxxr-c6 )
TOKENS=( 28356863910078205288614550619314017621 56713727820156410577229101238628035242 )
VOXXR_HOME=~/dev/wkspace/voxxr/voxxr

echo "starting nodes"
ovhcloud instance startInstance --instanceId $SEED_INSTANCE
for ((i=0; i < ${#NODES[@]}; i++)); do
  ovhcloud instance startInstance --instanceId ${NODES[$i]};
done

echo "waiting for seed node to startup..."
SEED_IP=''
while [ "$SEED_IP" = "" ]; do
 SEED_IP=`ovhcloud instance getInstances --projectName voxxr | grep $SEED_INSTANCE | grep 'running' | egrep -o '([0-9]+\.)+[0-9]+'`;
done

echo "Cassandra cluster seed machine running. IP=$SEED_IP"

$VOXXR_HOME/voxxr-room/src/main/scripts/deploy-cassandra.sh $SEED_INSTANCE $SEED_IP 0 $SEED_HOST
for ((i=0; i < ${#NODES[@]}; i++)); do
  echo "calling deploy cassandra ${NODES[$i]} $SEED_IP ${TOKEN[$i]} ${HOSTS[$i]}"
  $VOXXR_HOME/voxxr-room/src/main/scripts/deploy-cassandra.sh ${NODES[$i]} $SEED_IP ${TOKENS[$i]} ${HOSTS[$i]}
done

echo $SEED_IP > $VOXXR_HOME/voxxr-room/current-seed-ip