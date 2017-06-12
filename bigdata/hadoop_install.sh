#!/usr/bin/env bash

#hadoop��scalaѹ��������
declare hadoop_tar=/home/hadoop/hadoop-2.6.0-cdh5.7.6.tar.gz

#��ѹ���������
declare hadoop_version=hadoop-2.6.0-cdh5.7.6

#ָ������������
declare hadoop_ln=/home/hadoop/hadoop

#��װ��ָ��Ŀ¼
declare hadoop_install_dir=/home/hadoop/cdh5.7.6

#��������sh��
declare hadoop_env=/etc/profile.d/hadoop_env.sh


##�Զ��幦�ܺ�������start#####################################################################################################
#Ԥ���
function fun_prepare(){
	#1���ж�hadoopѹ�����Ƿ����
	if [ ! -e $hadoop_tar ]; then
		echo "fun_prepare(): the hadoop tar file($hadoop_tar) not found, exit!"
		return 0
	fi
	
	#2/���ж��������Ƿ����
	if [ -d $hadoop_version ]; then
		echo "fun_prepare(): the ln file($hadoop_version) exists, exit!"
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
	sudo echo "build hadoop envirement in file $hadoop_env"
	sudo touch $hadoop_env
	sudo echo "#!/bin/sh" > $hadoop_env
	sudo echo "export HADOOP_HOME=$hadoop_ln" >> $hadoop_env
	sudo echo 'export HADOOP_PREFIX=$HADOOP_HOME' >> $hadoop_env
	sudo echo 'export HADOOP_COMMON_HOME=$HADOOP_PREFIX' >> $hadoop_env
	sudo echo 'export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_PREFIX/lib/native' >> $hadoop_env
	sudo echo 'export HADOOP_CONF_DIR=$HADOOP_PREFIX/etc/hadoop' >> $hadoop_env
	sudo echo 'export HADOOP_HDFS_HOME=$HADOOP_PREFIX' >> $hadoop_env
	sudo echo 'export HADOOP_MAPRED_HOME=$HADOOP_PREFIX' >> $hadoop_env
	sudo echo 'export HADOOP_YARN_HOME=$HADOOP_PREFIX' >> $hadoop_env
	sudo echo 'export LD_LIBRARY_PATH=$HADOOP_PREFIX/lib/native' >> $hadoop_env
	sudo echo 'export PATH=$PATH:$HADOOP_HOME/bin' >> $hadoop_env
	sudo echo 'export PATH=$PATH:$HADOOP_HOME/sbin' >> $hadoop_env
	
	source /etc/profile
}

#��װhadoop��scala
function install_hadoop(){
	#��װǰ���
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#��ѹ
	echo "uncompresing $hadoop_tar"
	fun_un_cmprs $hadoop_tar $hadoop_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install hadoop failed, exit!"
		return
	fi
	
	#����������
	cd $hadoop_install_dir
	fun_crt_sln "$hadoop_install_dir/$hadoop_version" $hadoop_ln
	
	out_env
}

install_hadoop