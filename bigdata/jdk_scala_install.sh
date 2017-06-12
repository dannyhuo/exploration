#!/usr/bin/env bash

#jdk和scala压缩包名称
declare jdk_tar=/home/hadoop/jdk-8u131-linux-x64.tar.gz
declare scala_tar=/home/hadoop/scala-2.12.2.tgz

#解压缩后的名称
declare jdk_version=jdk1.8.0_131
declare scala_version=scala-2.12.2

#指定软链接名称
declare jdk_ln=/usr/jdk
declare scala_ln=/usr/scala

#安装到指定目录
declare jdk_install_dir=/usr/local

#环境变量sh名
declare jdk_env=/etc/profile.d/jdk_env.sh
declare scala_env=/etc/profile.d/scala_env.sh


##自定义功能函数部分start#####################################################################################################
#预检查
function fun_prepare(){
	#1、判断jdk压缩包是否存在
	if [ ! -e $jdk_tar ]; then
		echo "fun_prepare(): the jdk tar file($jdk_tar) not found, exit!"
		return 0
	fi
	
	#2、判断scala压缩包是否存在
	if [ ! -e $scala_tar ]; then
		echo "fun_prepare(): the scala tar file($scala_tar) not found, exit!"
		return 0
	fi
	
	#判断软链接是否存在
	if [ -d $scala_ln -o -d $jdk_version ]; then
		echo "fun_prepare(): the ln file($jdk_version or $scala_ln) exists, exit!"
		return 0
	fi
	
	return 1
}


#创建软链接
function fun_crt_sln(){
	local sr=$1
	local ln=$2

	#判断源文件是否存在
	if [ ! -e $sr -a ! -d $sr ]; then
		echo "fun_crt_sln(): $sr is not a file or a directory, the arguments is invalid!"
		return 0
	fi

	#判断软链接是否存在
	if [ -e $ln -o -d $ln ]; then
		echo "fun_crt_sln(): $ln is exits, create slink failed!"
		return 0
	fi

	#创建软链接
	ln -s $sr $ln

	return 1
}

#解压到指定地方
function fun_un_cmprs(){
	local sr=$1
	local tgt=$2
	
	#判断源文件是否存在
	if [ ! -e $sr -a ! -d $sr ]; then
		echo "fun_un_cmprs(): $sr is not exists, uncompressing it failed!"
		return 0
	fi
	
	#判断指定解压的位置是否是个文件 , 不是目录给出提示
	if [ ! -e $tgt ]; then
		echo "fun_un_cmprs(): the destination you want to uncompres is an file, uncompresing failed!"
		return 0
	fi
	
	#如果目标目录不存在，则创建
	if [ ! -d $tgt ]; then
		mkdir -p $tgt
	fi
	
	#解压
	tar -zxf $sr -C $tgt
	
	return 1
}

##自定义功能函数部分end#####################################################################################################

#输出环境变量
function out_env(){
	#配置java环境变量
	echo "build jdk envirement in file $jdk_env"
	touch $jdk_env
	echo "#!/bin/sh" > $jdk_env
	echo "export JAVA_HOME=$jdk_ln" >> $jdk_env
	echo 'export PATH=$PATH:$JAVA_HOME/bin/' >> $jdk_env
	echo "export CLASSPATH=$JAVA_HOME/lib" >> $jdk_env
	echo "export JRE_HOME=$JAVA_HOME/jre" >> $jdk_env
	#配置scala环境变量
	echo "build scala envirement in file $scala_env"
	touch $scala_env
	echo "export SCALA_HOME=$scala_ln" > $scala_env
	echo 'export PATH=$PATH:$SCALA_HOME/bin' >> $scala_env
	
	source /etc/profile
}

#安装jdk和scala
function install(){
	#安装前检查
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#解压
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

	#修改owner
	cd $jdk_install_dir
	echo "change $jdk_install_dir/$jdk_version owner................................."
	chown root "$jdk_version"
	chgrp root "$jdk_version"
	echo "change $jdk_install_dir/$scala_version owner................................."
	chown root "$scala_version"
	chgrp root "$scala_version"

	#创建软链接
	cd $jdk_install_dir
	fun_crt_sln "$jdk_install_dir/$jdk_version" $jdk_ln
	fun_crt_sln "$jdk_install_dir/$scala_version" $scala_ln
	
	out_env
}

install