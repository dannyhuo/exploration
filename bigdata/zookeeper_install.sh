#!/usr/bin/env bash

#zookeeper��scalaѹ��������
declare zookeeper_tar=/home/hadoop/zookeeper-3.4.5-cdh5.7.6.tar.gz

#��ѹ���������
declare zookeeper_version=zookeeper-3.4.5-cdh5.7.6

#ָ������������
declare zookeeper_ln=/home/hadoop/zookeeper

#��װ��ָ��Ŀ¼
declare zookeeper_install_dir=/home/hadoop/cdh5.7.6

#��������sh��
declare zookeeper_env=/etc/profile.d/zookeeper_env.sh


##�Զ��幦�ܺ�������start#####################################################################################################
#Ԥ���
function fun_prepare(){
	#1���ж�zookeeperѹ�����Ƿ����
	if [ ! -e $zookeeper_tar ]; then
		echo "fun_prepare(): the zookeeper tar file($zookeeper_tar) not found, exit!"
		return 0
	fi
	
	#2/���ж��������Ƿ����
	if [ -d $zookeeper_version ]; then
		echo "fun_prepare(): the ln file($zookeeper_version) exists, exit!"
		return 0
	fi
	
	return 1
}


#����������
function fun_crt_sln(){
	local sr=$1
	local ln=$2

	#�ж�Դ�ļ��Ƿ����
	if [ ! -e $sr -a ! -d $sr ]; then
		echo "fun_crt_sln(): $sr is not a file or a directory, the arguments is invalid!"
		return 0
	fi

	#�ж��������Ƿ����
	if [ -e $ln -o -d $ln ]; then
		echo "fun_crt_sln(): $ln is exits, create slink failed!"
		return 0
	fi

	#����������
	ln -s $sr $ln

	return 1
}

#��ѹ��ָ���ط�
function fun_un_cmprs(){
	local sr=$1
	local tgt=$2
	
	#�ж�Դ�ļ��Ƿ����
	if [ ! -e $sr -a ! -d $sr ]; then
		echo "fun_un_cmprs(): $sr is not exists, uncompressing it failed!"
		return 0
	fi
	
	#�ж�ָ����ѹ��λ���Ƿ��Ǹ��ļ� , ����Ŀ¼������ʾ
	if [ ! -e $tgt ]; then
		echo "fun_un_cmprs(): the destination you want to uncompres is an file, uncompresing failed!"
		return 0
	fi
	
	#���Ŀ��Ŀ¼�����ڣ��򴴽�
	if [ ! -d $tgt ]; then
		mkdir -p $tgt
	fi
	
	#��ѹ
	tar -zxf $sr -C $tgt
	
	return 1
}

##�Զ��幦�ܺ�������end#####################################################################################################

#�����������
function out_env(){
	#����java��������
	sudo echo "build zookeeper envirement in file $zookeeper_env"
	sudo touch $zookeeper_env
	sudo echo "#!/bin/sh" > $zookeeper_env
	sudo echo "export ZOOKEEPER_HOME=$zookeeper_ln" >> $zookeeper_env
	sudo echo 'export PATH=$PATH:$ZOOKEEPER_HOME/bin' >> $zookeeper_env
	sudo echo 'export CLASSPATH=$CLASSPATH:$ZOOKEEPER_HOME/lib:$ZOOKEEPER_HOME/share' >> $zookeeper_env
	
	source /etc/profile
}

#��װzookeeper��scala
function install_zookeeper(){
	#��װǰ���
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#��ѹ
	echo "uncompresing $zookeeper_tar"
	fun_un_cmprs $zookeeper_tar $zookeeper_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install zookeeper failed, exit!"
		return
	fi
	
	#����������
	cd $zookeeper_install_dir
	fun_crt_sln "$zookeeper_install_dir/$zookeeper_version" $zookeeper_ln
	
	out_env
}

install_zookeeper