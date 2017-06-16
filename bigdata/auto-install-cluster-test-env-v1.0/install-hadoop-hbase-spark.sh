#!/bin/sh

#the conf files which your configration, that's a directory contains zookeeper dir, hadoop dir, hbase dir, spark dir.
declare conf_dir=$1
#the host that you config file located.
declare host=$2

#echo "First, installing zookeeper....................."
#ssh -t crm-master2 "sh /home/hadoop/install_sh/zookeeper_install.sh $host:$conf_dir/zookeeper"
#ssh -t crm-slave1 "sh /home/hadoop/install_sh/zookeeper_install.sh $host:$conf_dir/zookeeper"
#ssh -t crm-slave2 "sh /home/hadoop/install_sh/zookeeper_install.sh $host:$conf_dir/zookeeper"


echo "Second, installing hadoop....................."
ssh -t crm-master1 "sh /home/hadoop/install_sh/hadoop_install.sh $host:$conf_dir/hadoop"
ssh -t crm-master2 "sh /home/hadoop/install_sh/hadoop_install.sh $host:$conf_dir/hadoop"
ssh -t crm-slave1 "sh /home/hadoop/install_sh/hadoop_install.sh $host:$conf_dir/hadoop"
ssh -t crm-slave2 "sh /home/hadoop/install_sh/hadoop_install.sh $host:$conf_dir/hadoop"
ssh -t crm-slave3 "sh /home/hadoop/install_sh/hadoop_install.sh $host:$conf_dir/hadoop"

echo "Third, installing spark....................."
ssh -t crm-master1 "sh /home/hadoop/install_sh/spark_install.sh $host:$conf_dir/spark"
ssh -t crm-master2 "sh /home/hadoop/install_sh/spark_install.sh $host:$conf_dir/spark"
ssh -t crm-slave1 "sh /home/hadoop/install_sh/spark_install.sh $host:$conf_dir/spark"
ssh -t crm-slave2 "sh /home/hadoop/install_sh/spark_install.sh $host:$conf_dir/spark"
ssh -t crm-slave3 "sh /home/hadoop/install_sh/spark_install.sh $host:$conf_dir/spark"

echo "Fourth, installing hbase....................."
ssh -t crm-master1 "sh /home/hadoop/install_sh/hbase_install.sh $host:$conf_dir/hbase"
ssh -t crm-master2 "sh /home/hadoop/install_sh/hbase_install.sh $host:$conf_dir/hbase"
ssh -t crm-slave1 "sh /home/hadoop/install_sh/hbase_install.sh $host:$conf_dir/hbase"
ssh -t crm-slave2 "sh /home/hadoop/install_sh/hbase_install.sh $host:$conf_dir/hbase"
ssh -t crm-slave3 "sh /home/hadoop/install_sh/hbase_install.sh $host:$conf_dir/hbase"


