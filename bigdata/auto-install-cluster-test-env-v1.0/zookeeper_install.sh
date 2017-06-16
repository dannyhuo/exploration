#!/usr/bin/env bash

#zookeeper和scala压缩包名称
declare zookeeper_tar=/home/hadoop/upload/zookeeper-3.4.5-cdh5.10.0.tar.gz

#解压缩后的名称
declare zookeeper_version=zookeeper-3.4.5-cdh5.10.0

#指定软链接名称
declare zookeeper_ln=/home/hadoop/app/zookeeper

#安装到指定目录
declare zookeeper_install_dir=/home/hadoop/app/cdh5.10.0

#环境变量sh名
declare zookeeper_env=/etc/profile.d/zookeeper_env.sh


##自定义功能函数部分start#####################################################################################################
#预检查
function fun_prepare(){
	#1、判断zookeeper压缩包是否存在
	if [ ! -e $zookeeper_tar ]; then
		echo "fun_prepare(): the zookeeper tar file($zookeeper_tar) not found, exit!"
		return 0
	fi
	
	#2/、判断软链接是否存在
	if [ -d $zookeeper_version ]; then
		echo "fun_prepare(): the ln file($zookeeper_version) exists, exit!"
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

function gener_myid(){
        local host=$(hostname)
        local zoocfg="$zookeeper_install_dir/$zookeeper_version/conf/zoo.cfg"
        local line=$(cat $zoocfg | grep $host)
        local left=${line%=*}
        local myid=${left#*.}

        local dd_line=$(cat $zoocfg | grep 'dataDir=')
        local dataDir=${dd_line#*=}

        if [ ! -d $dataDir ]; then
		mkdir -p $dataDir
        fi
	
        echo "create myid in folder $dataDir, myid is $myid"
        sh -c "echo $myid > $dataDir/myid"

	#create data log directory
	local dd_line=$(cat $zoocfg | grep 'dataLogDir=')
        local dataLogDir=${dd_line#*=}

        if [ ! -d $dataLogDir ]; then
                mkdir -p $dataLogDir
        fi

}

#输出环境变量
function out_env(){
	#配置java环境变量
	echo "build zookeeper envirement in file $zookeeper_env"
	
	local zk_tmp=.zk_tmp.sh
	echo "#!/bin/sh" > $zk_tmp
	echo "export ZOOKEEPER_HOME=$zookeeper_ln" >> $zk_tmp
	echo 'export PATH=$PATH:$ZOOKEEPER_HOME/bin' >> $zk_tmp
	echo 'export CLASSPATH=$CLASSPATH:$ZOOKEEPER_HOME/lib:$ZOOKEEPER_HOME/share' >> $zk_tmp

	sudo touch $zookeeper_env
	sudo sh -c "cat $zk_tmp > $zookeeper_env"
	rm -f $zk_tmp
	
	source /etc/profile
}

#安装zookeeper和scala
function install_zookeeper(){
	local conf_dir=$1
	
	#安装前检查
	fun_prepare
	if [ $? -lt 1 ]; then
		return
	fi

	#解压
	echo "uncompresing $zookeeper_tar"
	fun_un_cmprs $zookeeper_tar $zookeeper_install_dir
	if [ $? -lt 1 ]; then
		echo "install(): Install zookeeper failed, exit!"
		return
	fi
	
	#创建软链接
	cd $zookeeper_install_dir
	fun_crt_sln "$zookeeper_install_dir/$zookeeper_version" $zookeeper_ln
	
	out_env
	
	echo "the conf parameter is $conf_dir."
	if [ -n "$conf_dir" ]; then
		echo "scp the conf to the dir $zookeeper_install_dir/$zookeeper_version/conf/ from $conf_dir"
		scp "$conf_dir/*" "$zookeeper_install_dir/$zookeeper_version/conf/"
	fi

	gener_myid

	echo "starting zookeeper..........................."
	$zookeeper_install_dir/$zookeeper_version/bin/zkServer.sh start
}

install_zookeeper $1
