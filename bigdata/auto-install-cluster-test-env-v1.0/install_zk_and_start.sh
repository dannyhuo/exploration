#!/bin/sh

#the conf files which your configration, that's a directory contains zookeeper dir, hadoop dir, hbase dir, spark dir.
declare conf_dir=$1
#the host that you config file located.
declare host=$2

echo "First, installing zookeeper....................."
ssh -t crm-master2 "sh /home/hadoop/install_sh/zookeeper_install.sh $host:$conf_dir/zookeeper"
ssh -t crm-slave1 "sh /home/hadoop/install_sh/zookeeper_install.sh $host:$conf_dir/zookeeper"
ssh -t crm-slave2 "sh /home/hadoop/install_sh/zookeeper_install.sh $host:$conf_dir/zookeeper"

