#!/usr/bin/env bash

#����Ҫͬ���Ļ���
declare sync_hosts=("slave1" "slave2" "slave3" "slave4" "slave5" "slave6")
#����ԴĿ¼
declare source=/home/hadoop/jdk-8u131-linux-x64.tar.gz

#����ͬ����Ŀ��Ŀ¼
declare targetDir=/home/hadoop/

function read_parameter(){
	local sr=$1
	local tg=$2
	
	
}

function sync_once(){
	local sr=$1
	local tg=$2
	local hst=$3
	
	if [ -d $sr ]; then
		echo "scp -r $sr $host:$tg"
		scp -r $sr $host:$tg
	fi
	
	if [ -f $sr ]; then
		echo "$sr $host:$tg"
		scp $sr $host:$tg
	fi
}


function sync(){
	read_parameter
	
	if [ $1 -lt 1 ]; then
		echo "you input parameter is invalid, exit!"
		return 
	fi

	for host in ${sync_hosts[@]}
	do
		echo "sync the source($source) to the host(${host}) of $targetDir"
		sync_once $source $targetDir ${host}
	done
}


sync
