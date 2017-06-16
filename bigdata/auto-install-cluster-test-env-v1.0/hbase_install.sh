#!/usr/bin/env bash

#hbase和scala压缩包名称
declare hbase_tar=/home/hadoop/upload/hbase-1.2.0-cdh5.10.0.tar.gz

#解压缩后的名称
declare hbase_version=hbase-1.2.0-cdh5.10.0

#指定软链接名称
declare hbase_ln=/home/hadoop/app/hbase

#安装到指定目录
declare hbase_install_dir=/home/hadoop/app/cdh5.10.0

#环境变量sh名
declare hbase_env=/etc/profile.d/hbase_env.sh


##自定义功能函数部分start#####################################################################################################
#预检查
function fun_prepare(){
	#1、判断hbase压缩包是否存在
	if [ ! -e $hbase_tar ]; then
		echo "fun_prepare(): the hbase tar file($hbase_tar) not found, exit!"
		return 0
	fi
	
	#2/、判断软链接是否存在
	if [ -d $hbase_version ]; then
		echo "fun_prepare(): the ln file($hbase_version) exists, exit!"
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
	echo "build hbase envirement in file $hbase_env"
	local hbase_tmp=.hbase_tmp.sh
	echo "#!/bin/sh" > $hbase_tmp
	echo "export HBASE_HOME=$hbase_ln" >> $hbase_tmp
	echo 'export PATH=$PATH:$HBASE_HOME/bin' >> $hbase_tmp
	echo 'export CLASSPATH=$CLASSPATH:$HBASE_HOME/lib' >> $hbase_tmp
	
	sudo touch $hbase_env
	sudo sh -c "cat $hbase_tmp > $hbase_env"
	rm -f $hbase_tmp
	
	source /etc/profile
}

#安装hbase和scala
function install_hbase(){
	local conf_dir=$1

	#安装前检查
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#解压
	echo "uncompresing $hbase_tar"
	fun_un_cmprs $hbase_tar $hbase_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install hbase failed, exit!"
		return
	fi
	
	#创建软链接
	cd $hbase_install_dir
	fun_crt_sln "$hbase_install_dir/$hbase_version" $hbase_ln
	
	out_env

	echo "the conf parameter is $conf_dir."
        if [ -n "$conf_dir" ]; then
                echo "scp the conf to the dir $hbase_install_dir/$hbase_version/conf from $conf_dir"
                scp "$conf_dir/*" "$hbase_install_dir/$hbase_version/conf/"
        fi

}

install_hbase $1
