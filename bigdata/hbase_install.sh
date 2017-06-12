#!/usr/bin/env bash

#hbase��scalaѹ��������
declare hbase_tar=/home/hadoop/hbase-1.2.0-cdh5.7.6.tar.gz

#��ѹ���������
declare hbase_version=hbase-1.2.0-cdh5.7.6

#ָ������������
declare hbase_ln=/home/hadoop/hbase

#��װ��ָ��Ŀ¼
declare hbase_install_dir=/home/hadoop/cdh5.7.6

#��������sh��
declare hbase_env=/etc/profile.d/hbase_env.sh


##�Զ��幦�ܺ�������start#####################################################################################################
#Ԥ���
function fun_prepare(){
	#1���ж�hbaseѹ�����Ƿ����
	if [ ! -e $hbase_tar ]; then
		echo "fun_prepare(): the hbase tar file($hbase_tar) not found, exit!"
		return 0
	fi
	
	#2/���ж��������Ƿ����
	if [ -d $hbase_version ]; then
		echo "fun_prepare(): the ln file($hbase_version) exists, exit!"
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
	sudo echo "build hbase envirement in file $hbase_env"
	sudo touch $hbase_env
	sudo echo "#!/bin/sh" > $hbase_env
	sudo echo "export HBASE_HOME=$hbase_ln" >> $hbase_env
	sudo echo 'export PATH=$PATH:$HBASE_HOME/bin' >> $hbase_env
	sudo echo 'export CLASSPATH=$CLASSPATH:$HBASE_HOME/lib' >> $hbase_env
	
	source /etc/profile
}

#��װhbase��scala
function install_hbase(){
	#��װǰ���
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#��ѹ
	echo "uncompresing $hbase_tar"
	fun_un_cmprs $hbase_tar $hbase_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install hbase failed, exit!"
		return
	fi
	
	#����������
	cd $hbase_install_dir
	fun_crt_sln "$hbase_install_dir/$hbase_version" $hbase_ln
	
	out_env
}

install_hbase