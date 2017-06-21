#!/bin/sh

#安装软件的名称
declare app_name=$1
declare install_sh_home=$2
#当前执行shell脚本的根目录
declare sh_parent_dir=$(dirname $0)

#jdk安装配置
declare jdk_tar_source
declare jdk_install_dir
declare jdk_slink_path

#scala安装配置
declare scala_tar_source
declare scala_install_dir
declare scala_slink_path

#zookeeper安装配置
declare zookeeper_tar_source
declare zookeeper_install_dir
declare zookeeper_slink_path
declare zookeeper_conf_url

#hadoop安装配置
declare hadoop_tar_source
declare hadoop_install_dir
declare hadoop_slink_path
declare hadoop_conf_url

#hbase安装配置
declare hbase_tar_source
declare hbase_install_dir
declare hbase_slink_path
declare hbase_conf_url

#spark安装配置
declare spark_tar_source
declare spark_install_dir
declare spark_slink_path
declare spark_conf_url

function init_conf(){
	local conf_read_from_conf=`cat $sh_parent_dir/etc/install.conf`
	for line in $conf_read_from_conf
	do
		if [ "${line:0:1}" != "#" ]; then
			if [[ "$line" =~ "=" ]]; then
				local value=${line#*=}
				if [[ "$line" =~ "jdk.tar.source" ]]; then
					jdk_tar_source=$value
				elif [[ "$line" =~ "jdk.install.dir" ]]; then
					jdk_install_dir=$value
				elif [[ "$line" =~ "jdk.slink.path" ]]; then
					jdk_slink_path=$value
					
				elif [[ "$line" =~ "scala.tar.source" ]]; then
					scala_tar_source=$value
				elif [[ "$line" =~ "scala.install.dir" ]]; then
					scala_install_dir=$value
				elif [[ "$line" =~ "scala.slink.path" ]]; then
					scala_slink_path=$value
				
				elif [[ "$line" =~ "zookeeper.tar.source" ]]; then
					zookeeper_tar_source=$value
				elif [[ "$line" =~ "zookeeper.install.dir" ]]; then
					zookeeper_install_dir=$value
				elif [[ "$line" =~ "zookeeper.slink.path" ]]; then
					zookeeper_slink_path=$value
				elif [[ "$line" =~ "zookeeper.conf.url" ]]; then
					zookeeper_conf_url=$value
					
				elif [[ "$line" =~ "hadoop.tar.source" ]]; then
					hadoop_tar_source=$value
				elif [[ "$line" =~ "hadoop.install.dir" ]]; then
					hadoop_install_dir=$value
				elif [[ "$line" =~ "hadoop.slink.path" ]]; then
					hadoop_slink_path=$value
				elif [[ "$line" =~ "hadoop.conf.url" ]]; then
					hadoop_conf_url=$value
				
				elif [[ "$line" =~ "hbase.tar.source" ]]; then
					hbase_tar_source=$value
				elif [[ "$line" =~ "hbase.install.dir" ]]; then
					hbase_install_dir=$value
				elif [[ "$line" =~ "hbase.slink.path" ]]; then
					hbase_slink_path=$value
				elif [[ "$line" =~ "hbase.conf.url" ]]; then
					hbase_conf_url=$value
				
				elif [[ "$line" =~ "spark.tar.source" ]]; then
					spark_tar_source=$value
				elif [[ "$line" =~ "spark.install.dir" ]]; then
					spark_install_dir=$value
				elif [[ "$line" =~ "spark.slink.path" ]]; then
					spark_slink_path=$value
				elif [[ "$line" =~ "spark.conf.url" ]]; then
					spark_conf_url=$value
				else
					echo "you config file($sh_parent_dir/etc/install.conf) found the invalid conf: $line"
				fi
			fi
		fi
	done
}

#主入口##############################################################################################################
function main(){
	#1、初始化配置
	init_conf
	
	local cluster_hosts_file=$sh_parent_dir/etc/cluster-hosts
	if [ $app_name == "zookeeper" ]; then
		cluster_hosts_file=$sh_parent_dir/etc/zk-hosts
	fi
	
	#2、循环host执行命令
	local hosts=`cat $cluster_hosts_file`
	for host in $hosts
	do
		case $app_name in
			jdk)
				ssh -t $host "$install_sh_home/local-sh/local-install.sh -t jdk -tar $jdk_tar_source -sl $jdk_slink_path -d $jdk_install_dir"
			;;
			scala)
				ssh -t $host "$install_sh_home/local-sh/local-install.sh -t scala -tar $scala_tar_source -sl $scala_slink_path -d $scala_install_dir"
			;;
			zookeeper)
				ssh -t $host "$install_sh_home/local-sh/local-install.sh -t zookeeper -tar $zookeeper_tar_source -sl $zookeeper_slink_path -conf $zookeeper_conf_url -d $zookeeper_install_dir"
			;;
			hadoop)
				ssh -t $host "$install_sh_home/local-sh/local-install.sh -t hadoop -tar $hadoop_tar_source -sl $hadoop_slink_path -conf $hadoop_conf_url -d $hadoop_install_dir"
			;;
			hbase)
				ssh -t $host "$install_sh_home/local-sh/local-install.sh -t hbase -tar $hbase_tar_source -sl $hbase_slink_path -conf $hbase_conf_url -d $hbase_install_dir"
			;;
			spark)
				ssh -t $host "$install_sh_home/local-sh/local-install.sh -t spark -tar $spark_tar_source -sl $spark_slink_path -conf $spark_conf_url -d $spark_install_dir"
			;;
		esac
	done
}

main