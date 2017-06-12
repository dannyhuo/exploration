#!/usr/bin/env bash

#spark��scalaѹ��������
declare spark_tar=/home/hadoop/spark-1.6.0-cdh5.7.6.tar.gz

#��ѹ���������
declare spark_version=spark-1.6.0-cdh5.7.6

#ָ������������
declare spark_ln=/home/hadoop/spark

#��װ��ָ��Ŀ¼
declare spark_install_dir=/home/hadoop/cdh5.7.6

#��������sh��
declare spark_env=/etc/profile.d/spark_env.sh


##�Զ��幦�ܺ�������start#####################################################################################################
#Ԥ���
function fun_prepare(){
	#1���ж�sparkѹ�����Ƿ����
	if [ ! -e $spark_tar ]; then
		echo "fun_prepare(): the spark tar file($spark_tar) not found, exit!"
		return 0
	fi
	
	#2/���ж��������Ƿ����
	if [ -d $spark_version ]; then
		echo "fun_prepare(): the ln file($spark_version) exists, exit!"
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
	sudo echo "build spark envirement in file $spark_env"
	sudo touch $spark_env
	sudo echo "#!/bin/sh" > $spark_env
	sudo echo "export SPARK_HOME=$spark_ln" >> $spark_env
	sudo echo 'export PATH=$PATH:$SPARK_HOME/sbin' >> $spark_env
	
	source /etc/profile
}

#��װspark��scala
function install_spark(){
	#��װǰ���
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#��ѹ
	echo "uncompresing $spark_tar"
	fun_un_cmprs $spark_tar $spark_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install spark failed, exit!"
		return
	fi
	
	#����������
	cd $spark_install_dir
	fun_crt_sln "$spark_install_dir/$spark_version" $spark_ln
	
	out_env
}

install_spark