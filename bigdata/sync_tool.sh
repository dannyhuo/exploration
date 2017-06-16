#!/usr/bin/env bash

#����Ҫͬ���Ļ���
declare sync_hosts=("crm-master1" "crm-master2" "crm-slave1" "crm-slave2" "crm-slave3")
#����ԴĿ¼
declare source=/home/hadoop/jdk-8u131-linux-x64.tar.gz

#����ͬ����Ŀ��Ŀ¼
declare targetDir=/home/hadoop/


function sync_once(){
	local sr=$1
	local tg=$2
	local host=$3
	
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
	local sr=$1
	local tg=$2
	
	if [ ! -d $tg ]; then
		return
	fi
	
	source=$sr
	
	targetDir=$tg

	for host in ${sync_hosts[@]}
	do
		echo "sync the source($source) to the host(${host}) of $targetDir"
		sync_once $source $targetDir $host
	done
}


sync $1 $2
