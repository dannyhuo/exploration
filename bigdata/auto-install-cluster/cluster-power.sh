#!/bin/sh
declare journal_hosts
declare namenode_hosts
declare yarn_hosts

#主namenode节点
declare master_host

declare sh_name=$0
declare power_flag=$1
declare type=$2

#1、读取配置
function parse_config(){
	local hdfs_site=`cat $HADOOP_HOME/etc/hadoop/hdfs-site.xml`
	local flag="false"
	for line in $hdfs_site
	do
		if [[ "$line" =~ "dfs.ha.namenodes" ]]; then
			flag="true"
		elif [ "$flag" == "true" ]; then
			#<name>dfs.ha.namenodes.hadoop-cluster</name>
			#<value>crm-master1,crm-master2</value>
			if [[ "$line" =~ "<value>" ]]; then
				flag="false"
				local v_right=${line#*>}
				local value=${v_right%%<*}
				local tmp_arr=${value//,/ };
				local arr=($tmp_arr)
				namenode_hosts=${arr[*]}
				echo "the namenodes from the config file is '$namenode_hosts'"
				#取配置中的第一个作为主namenode节点
				master_host=${arr[0]}
				echo "choose the first namenode '$master_host' as the master namenode"
			fi
		fi
	done
	
	#<name>yarn.resourcemanager.hostname.rm1</name>
	#<value>master1</value>
	local yarn_site=`cat $HADOOP_HOME/etc/hadoop/yarn-site.xml`
	flag="false"
	local index=0
	for line in $yarn_site
	do
		if [[ "$line" =~ "<name>yarn.resourcemanager.hostname." ]]; then
			flag="true"
		elif [ "$flag" == "true" ]; then
			if [[ "$line" =~ "<value>" ]]; then
				flag="false"
				local v_right=${line#*>}
				local value=${v_right%%<*}
				echo "the value is '$value', yarn_hosts[$index]=$value"
				yarn_hosts[index]=$value
				let index++
			fi
		fi
	done
}

#2、开关hadoop
function power_hadoop(){
	local power=$1
	echo "will ${power} hadoop at host '$master_host' : $HADOOP_HOME/sbin/${power}-all.sh"
	ssh -t $master_host "$HADOOP_HOME/sbin/${power}-all.sh"
	
	for m_host in ${yarn_hosts[@]}
	do
		if [ "$m_host" != "$master_host" ]; then
			echo "will ${power} resourcemanager at host '$m_host' : yarn-daemons.sh $power resourcemanager"
			ssh -t $m_host "yarn-daemons.sh $power resourcemanager"
		fi
	done
}

#3、开关hbase
function power_hbase(){
	local power=$1
	echo "will ${power} hbase at host '$master_host' : ${power}-hbase.sh"
	ssh -t $master_host "${power}-hbase.sh"
	
	for m_host in $namenode_hosts
	do
		if [ "$m_host" != "$master_host" ]; then
			echo "will ${power} hmaster at host '$m_host' : hbase-daemons.sh $power master"
			ssh -t $m_host "hbase-daemons.sh $power master"
		fi
	done
}

#4、开关spark
function power_spark(){
	local power=$1
	echo "will ${power} spark at host '$master_host' : $SPARK_HOME/sbin/${power}-all.sh"
	ssh -t $master_host "$SPARK_HOME/sbin/${power}-all.sh"
	
	for m_host in $namenode_hosts
	do
		if [ "$m_host" != "$master_host" ]; then
			echo "will ${power} master at host '$m_host' : ${power}-master.sh"
			ssh -t $m_host "${power}-master.sh"
		fi
	done
}

#5、开关所有
function power_all(){
	if [ "$1" == "start" ]; then
		power_hadoop $1
		echo -e "\n"
		power_hbase $1
		echo -e "\n"
		power_spark $1
	elif [ "$1" == "stop" ]; then
		power_spark $1
		echo -e "\n"
		power_hbase $1
		echo -e "\n"
		power_hadoop $1
	fi
}

#6、使用说明
function print_usage(){
	echo "you can use shell $sh_name as follows :"
	echo -e "\t$sh_name start hadoop, will start hadoop"
	echo -e "\t$sh_name start hbase, will start hbase"
	echo -e "\t$sh_name start spark, will start spark"
	echo -e "\t$sh_name start all, will start hadoop, hbase and spark"
	echo -e "\t$sh_name stop hadoop, will stop hadoop"
	echo -e "\t$sh_name stop hbase, will stop hbase"
	echo -e "\t$sh_name stop spark, will stop spark"
	echo -e "\t$sh_name stop all, will stop hadoop, hbase and spark"
}

#读取配置
parse_config

if [ "$#" != 2 ]; then
  print_usage
  exit
fi


if [ "$power_flag" != "start" -a  "$power_flag" != "stop" ]; then
	echo "unresolved arguments : $power_flag"
	print_usage
	exit
fi

if [ "$type" == "hadoop" ]; then
	power_hadoop $power_flag
elif [ "$type" == "hbase" ]; then
	power_hbase $power_flag
elif [ "$type" == "spark" ]; then
	power_spark $power_flag
elif [ "$type" == "all" ]; then
	power_all $power_flag
else
	echo "unresolved arguments : $type"
	print_usage
	exit
fi
