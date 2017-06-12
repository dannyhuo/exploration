#!/usr/bin/env bash

#jdk��scalaѹ��������
declare jdk_tar=/home/hadoop/jdk-8u131-linux-x64.tar.gz
declare scala_tar=/home/hadoop/scala-2.12.2.tgz

#��ѹ���������
declare jdk_version=jdk1.8.0_131
declare scala_version=scala-2.12.2

#ָ������������
declare jdk_ln=/usr/jdk
declare scala_ln=/usr/scala

#��װ��ָ��Ŀ¼
declare jdk_install_dir=/usr/local

#��������sh��
declare jdk_env=/etc/profile.d/jdk_env.sh
declare scala_env=/etc/profile.d/scala_env.sh


##�Զ��幦�ܺ�������start#####################################################################################################
#Ԥ���
function fun_prepare(){
	#1���ж�jdkѹ�����Ƿ����
	if [ ! -e $jdk_tar ]; then
		echo "fun_prepare(): the jdk tar file($jdk_tar) not found, exit!"
		return 0
	fi
	
	#2���ж�scalaѹ�����Ƿ����
	if [ ! -e $scala_tar ]; then
		echo "fun_prepare(): the scala tar file($scala_tar) not found, exit!"
		return 0
	fi
	
	#�ж��������Ƿ����
	if [ -d $scala_ln -o -d $jdk_version ]; then
		echo "fun_prepare(): the ln file($jdk_version or $scala_ln) exists, exit!"
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
	echo "build jdk envirement in file $jdk_env"
	touch $jdk_env
	echo "#!/bin/sh" > $jdk_env
	echo "export JAVA_HOME=$jdk_ln" >> $jdk_env
	echo 'export PATH=$PATH:$JAVA_HOME/bin/' >> $jdk_env
	echo "export CLASSPATH=$JAVA_HOME/lib" >> $jdk_env
	echo "export JRE_HOME=$JAVA_HOME/jre" >> $jdk_env
	#����scala��������
	echo "build scala envirement in file $scala_env"
	touch $scala_env
	echo "export SCALA_HOME=$scala_ln" > $scala_env
	echo 'export PATH=$PATH:$SCALA_HOME/bin' >> $scala_env
	
	source /etc/profile
}

#��װjdk��scala
function install(){
	#��װǰ���
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#��ѹ
	echo "uncompresing $jdk_tar"
	fun_un_cmprs $jdk_tar $jdk_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install jdk failed, exit!"
		return
	fi
	
	echo "uncompresing $scala_tar"
	fun_un_cmprs $scala_tar $jdk_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install scala failed!"
		return
	fi

	#�޸�owner
	cd $jdk_install_dir
	echo "change $jdk_install_dir/$jdk_version owner................................."
	chown root "$jdk_version"
	chgrp root "$jdk_version"
	echo "change $jdk_install_dir/$scala_version owner................................."
	chown root "$scala_version"
	chgrp root "$scala_version"

	#����������
	cd $jdk_install_dir
	fun_crt_sln "$jdk_install_dir/$jdk_version" $jdk_ln
	fun_crt_sln "$jdk_install_dir/$scala_version" $scala_ln
	
	out_env
}

install