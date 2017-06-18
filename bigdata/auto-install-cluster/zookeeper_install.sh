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

#安装什么软件
declare type
#安装包文件
declare tar_file
#软件版本，从安装包解压后的目录名
declare app_version
#软件安装目录
declare install_dir
#软链接目录
declare ln_dir
#配置文件所在目录
declare conf_location


function print_help(){
	echo -e "\t-t : what you want to install, zookeeper, hadoop, hbase, spark"
	echo -e "\t-tar : the path of tar file that you want to install."
	echo -e "\t-v : the software full version."
	echo -e "\t-d : the directory that you want install to."
	echo -e "\t-sl : point the soft link location you want to, default at ~, if you don't point it."
	echo -e "\t-conf : the cont dir, hadoop $HADOOP_HOME/etc/hadoop, or $HBASE_HOME/conf..."
}



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



#install_zookeeper $1

#安装前检查
function prepare(){
	#1、检查防火墙
	firewallStatus=$(firewall-cmd --state)
	if [ ! $firewallStatus = "not running" ]; then
		local cur_host=$(hostname)
		echo "the host $cur_host firewall not closed, then will close the firewall....."
		#关闭防火墙
		systemctl stop firewalld.service
	fi
	
	#2检查其它事项
	
}






#循环处理参数，入口
while test -n "$1"; do
	case "$1" in
		--help | -h)
			print_help
			exit $ST_OK
		;;
		
		#分发上传参数###################################################################################
		-t)
			if test -z $2
			then
				echo "please input type by -t, zookeeper, hadoop, hbase, spark....."
				exit $ST_ERR
			fi
			echo "you point the type is $2, "
			type=$2	
			shift
		;;
		
		#tar包所在路径，必须参数
		-tar)
			if [ ! -f $2 ]; then
				echo "the tar file not found, please check you tar file and retry."
				exit $ST_ERR
			fi
			tar_file=$2
			#从tar包中获取解压后的目录名
			app_version=$(tar -tf $2 | awk -F "/" '{print $1}' | sed -n '1p')
			shift
		;;
		
		-d)
			if [ ! -d $2 ]; then
				#指定的安装目录不存在
				echo "the install directory is not found, check it"
				exit $ST_ERR
			fi
			install_dir=$2	
			shift
		;;
		
		-sl)
			if [ ! -d $2 ]; then
				echo "you pointed the soft link directory not exist, check it."
				exit $ST_ERR
			fi
			shift
		;;
		
		-conf)
			if test -z $2
			then
				echo "the -conf don't point the conf location, is like host:/home/hadoop/conf/etc/hadoop"
				$ST_ERR
			fi
			conf_location=$2
			shift
		;;
		
		*)
			echo "unknow options: $1"
			print_help
			exit $ST_ERR
		;;
	esac
	
	shift
done

#安装zookeeper
function install_zookeeper(){
	echo "installing zookeeper.................."
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
#安装hadoop
function install_hadoop(){
	echo "installing hadoop.................."
}
#安装hbase
function install_hbase(){
	echo "installing hbase.................."
}
#安装spark
function install_spark(){
	echo "installing spark.................."
}

function install(){
	case "$type" in
		zookeeper)
			install_zookeeper
		;;
		
		hadoop)
			install_hadoop
		;;
		
		hbase)
			install_hbase
		;;
		
		spark)
			install_spark
		;;
		
		*)
			echo "you type $type is unresolved."
		;;
	esac
}

install