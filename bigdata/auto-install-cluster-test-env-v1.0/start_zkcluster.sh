#!/bin/sh
ssh -t crm-master2 "$ZOOKEEPER_HOME/bin/zkServer.sh start"
ssh -t crm-slave1 "$ZOOKEEPER_HOME/bin/zkServer.sh start"
ssh -t crm-slave2 "$ZOOKEEPER_HOME/bin/zkServer.sh start"

