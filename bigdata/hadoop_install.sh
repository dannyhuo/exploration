#!/usr/bin/env bash

#hadoop和scala压缩包名称
declare hadoop_tar=/home/hadoop/hadoop-2.6.0-cdh5.7.6.tar.gz

#解压缩后的名称
declare hadoop_version=hadoop-2.6.0-cdh5.7.6

#指定软链接名称
declare hadoop_ln=/home/hadoop/hadoop

#安装到指定目录
declare hadoop_install_dir=/home/hadoop/cdh5.7.6

#环境变量sh名
declare hadoop_env=/etc/profile.d/hadoop_env.sh


##自定义功能函数部分start#####################################################################################################
#预检查
function fun_prepare(){
	#1、判断hadoop压缩包是否存在
	if [ ! -e $hadoop_tar ]; then
		echo "fun_prepare(): the hadoop tar file($hadoop_tar) not found, exit!"
		return 0
	fi
	
	#2/、判断软链接是否存在
	if [ -d $hadoop_version ]; then
		echo "fun_prepare(): the ln file($hadoop_version) exists, exit!"
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

#安装hadoop和scala
function install_hadoop(){
	#安装前检查
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#解压
	echo "uncompresing $hadoop_tar"
	fun_un_cmprs $hadoop_tar $hadoop_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install hadoop failed, exit!"
		return
	fi
	
	#创建软链接
	cd $hadoop_install_dir
	fun_crt_sln "$hadoop_install_dir/$hadoop_version" $hadoop_ln
	
	out_env
}

install_hadoop