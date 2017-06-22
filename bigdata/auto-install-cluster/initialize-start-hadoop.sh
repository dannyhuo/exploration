#!/bin/sh
declare journal_hosts
declare namenode_hosts

#主namenode节点
declare master_host

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
				echo "the value = $value, will split by ',' to a arr."
				local tmp_arr=${value//,/ };
				local arr=($tmp_arr)
				namenode_hosts=${arr[*]}
				echo "the namenodes from the config file is '$namenode_hosts'"
				master_host=${arr[0]}
				echo "choose the first namenode '$master_host' as the master namenode"
			fi
		elif [[ "$line" =~ "qjournal://" ]]; then
			#<value>qjournal://crm-master2:8485;crm-slave1:8485;crm-slave2:8485/hadoop-cluster</value>
			local v_right=${line#*//}
			local value=${v_right%%/*}
			local tmp_arr=${value//;/ };
			local arr=($tmp_arr)
			journal_hosts=${arr[*]}
			
			#for item in ${arr[*]}
			#do
			#	local j_host=${item%:*}
			#	echo "will start journalnode at $j_host"
			#	ssh -t $j_host '$HADOOP_HOME/sbin/hadoop-daemon.sh start journalnode'
			#done
		fi
	done
}

#2、启动journalnode
function start_journal_node(){
	for item in $journal_hosts
	do
		local j_host=${item%:*}
		echo "will start journalnode at $j_host"
		ssh -t $j_host '$HADOOP_HOME/sbin/hadoop-daemon.sh start journalnode'
	done
}

#3、格式化namenode和zknode
function format_namenode(){
	echo "will format namenode at host '$master_host' : hdfs namenode -format"
	#$HADOOP_HOME/bin/hdfs namenode -format
	ssh -t $master_host "hdfs namenode -format"
	
	echo "will start namenode at host '$master_host' : hadoop-daemon.sh start namenode"
	#$HADOOP_HOME/sbin/hadoop-daemon.sh start namenode
	ssh -t $master_host "$HADOOP_HOME/sbin/hadoop-daemon.sh start namenode"
	
	#$HADOOP_HOME/bin/hdfs namenode -bootstrapStandby
	for m_host in $namenode_hosts
	do
		if [ "$m_host" != "$master_host" ]; then
			echo "will format secondary namenode at host '$m_host' : hdfs namenode -bootstrapStandby"
			ssh -t $m_host "hdfs namenode -bootstrapStandby"
		fi
	done
	
	echo "will format zkfc at host '$master_host' : hdfs zkfc -formatZK"
	#$HADOOP_HOME/bin/hdfs zkfc -formatZK
	ssh -t $master_host "hdfs zkfc -formatZK"
	
	#$HADOOP_HOME/sbin/hadoop-daemon.sh start zkfc
	for m_host in $namenode_hosts
	do
		echo "will start zkfc at host '$m_host' : hadoop-daemon.sh start zkfc"
		ssh -t $m_host "$HADOOP_HOME/sbin/hadoop-daemon.sh start zkfc"
	done
	
	#$HADOOP_HOME/sbin/start-dfs.sh
	echo "will start dfs at host '$master_host' : start-dfs.sh"
	ssh -t $master_host "$HADOOP_HOME/sbin/start-dfs.sh"
	
	#$HADOOP_HOME/sbin/start-yarn.sh
	echo "will start yarn at host '$master_host' : start-yarn.sh"
	ssh -t $master_host "$HADOOP_HOME/sbin/start-yarn.sh"
}

parse_config
start_journal_node
format_namenode
