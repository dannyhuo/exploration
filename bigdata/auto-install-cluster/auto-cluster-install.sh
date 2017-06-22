#!/bin/sh

#安装软件的名称
declare app
#当前执行shell脚本的根目录
declare cur_sh_parent_dir="$(dirname $0)"
echo "$0 cur_sh_parent_dir is '$cur_sh_parent_dir'"

#安装集群时允许在每个节点上创建的一个临时目录，用于同步安装脚本的，安装完毕后会删除
declare install_sh_home_tmp="~/.tmp-auto-install-cluster-shs"
#集群安装的默认配置根目录
declare cluster_etc_root="$cur_sh_parent_dir/etc"
echo "cluster_etc_root is '$cluster_etc_root'"

#手动指定的clusterhosts
declare cluster_hosts_conf="$cluster_etc_root/hadoop-hosts"
declare cluster_hosts_conf_zk="$cluster_etc_root/zk-hosts"
declare current_cluster_sync_shell_hosts

#集群安装配置，默认给定shell根目录下的etc/install.con
declare cluster_install_conf="$cluster_etc_root/install.conf"
echo "default cluster_install_conf is '$cluster_install_conf'"

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

#读取配置
function init_conf(){
	if [ ! -f "$cluster_install_conf" ]; then
		echo "the cluster install config file of '$cluster_install_conf' not found!"
		exit $ST_ERR
	fi
	local conf_read_from_conf=`cat $cluster_install_conf`
	for line in $conf_read_from_conf
	do
		if [ "${line:0:1}" != "#" ]; then
			if [[ "$line" =~ "=" ]]; then
				local value=${line#*=}
				if [[ "$line" =~ "jdk.tar.source" ]]; then
					if ssh ${value%:*} test -e ${value#*:}; then
						jdk_tar_source=$value
					else
						echo "the file '$value' that you conf at the '$cluster_install_conf' not exists!"
						exit $ST_ERR
					fi
				elif [[ "$line" =~ "jdk.install.dir" ]]; then
					jdk_install_dir=$value
				elif [[ "$line" =~ "jdk.slink.path" ]]; then
					jdk_slink_path=$value
					
				elif [[ "$line" =~ "scala.tar.source" ]]; then
					if ssh ${value%:*} test -e ${value#*:}; then
						scala_tar_source=$value
					else
						echo "the file '$value' that you conf at the '$cluster_install_conf' not exists!"
						exit $ST_ERR
					fi
				elif [[ "$line" =~ "scala.install.dir" ]]; then
					scala_install_dir=$value
				elif [[ "$line" =~ "scala.slink.path" ]]; then
					scala_slink_path=$value
				
				elif [[ "$line" =~ "zookeeper.tar.source" ]]; then
					if ssh ${value%:*} test -e ${value#*:}; then
						zookeeper_tar_source=$value
					else
						echo "the file '$value' that you conf at the '$cluster_install_conf' not exists!"
						exit $ST_ERR
					fi
				elif [[ "$line" =~ "zookeeper.install.dir" ]]; then
					zookeeper_install_dir=$value
				elif [[ "$line" =~ "zookeeper.slink.path" ]]; then
					zookeeper_slink_path=$value
				elif [[ "$line" =~ "zookeeper.conf.url" ]]; then
					zookeeper_conf_url=$value
					
				elif [[ "$line" =~ "hadoop.tar.source" ]]; then
					if ssh ${value%:*} test -e ${value#*:}; then
						hadoop_tar_source=$value
					else
						echo "the file '$value' that you conf at the '$cluster_install_conf' not exists!"
						exit $ST_ERR
					fi
				elif [[ "$line" =~ "hadoop.install.dir" ]]; then
					hadoop_install_dir=$value
				elif [[ "$line" =~ "hadoop.slink.path" ]]; then
					hadoop_slink_path=$value
				elif [[ "$line" =~ "hadoop.conf.url" ]]; then
					hadoop_conf_url=$value
				
				elif [[ "$line" =~ "hbase.tar.source" ]]; then
					if ssh ${value%:*} test -e ${value#*:}; then
						hbase_tar_source=$value
					else
						echo "the file '$value' that you conf at the '$cluster_install_conf' not exists!"
						exit $ST_ERR
					fi
				elif [[ "$line" =~ "hbase.install.dir" ]]; then
					hbase_install_dir=$value
				elif [[ "$line" =~ "hbase.slink.path" ]]; then
					hbase_slink_path=$value
				elif [[ "$line" =~ "hbase.conf.url" ]]; then
					hbase_conf_url=$value
				
				elif [[ "$line" =~ "spark.tar.source" ]]; then
					if ssh ${value%:*} test -e ${value#*:}; then
						spark_tar_source=$value
					else
						echo "the file '$value' that you conf at the '$cluster_install_conf' not exists!"
						exit $ST_ERR
					fi
				elif [[ "$line" =~ "spark.install.dir" ]]; then
					spark_install_dir=$value
				elif [[ "$line" =~ "spark.slink.path" ]]; then
					spark_slink_path=$value
				elif [[ "$line" =~ "spark.conf.url" ]]; then
					spark_conf_url=$value
				else
					echo "you config file($cur_sh_parent_dir/etc/install.conf) found the invalid conf: $line"
				fi
			fi
		fi
	done
}


function exec_install(){
	local type=$1
	local hosts_conf=$cluster_hosts_conf
	if [ "$type" == "zookeeper" ]; then
		hosts_conf=$cluster_hosts_conf_zk
	fi
	
	if [ ! -f $hosts_conf ]; then
		echo "the host conf of '$hosts_conf' not found, check it!"
		exit $ST_ERR
	fi
	
	local hosts=`cat $hosts_conf`
	case $type in
		jdk)
			for host in $hosts
			do
				ssh -t $host "$install_sh_home_tmp/local-sh/local-install.sh -t jdk -tar $jdk_tar_source -sl $jdk_slink_path -d $jdk_install_dir"
			done
		;;
		scala)
			for host in $hosts
			do
				ssh -t $host "$install_sh_home_tmp/local-sh/local-install.sh -t scala -tar $scala_tar_source -sl $scala_slink_path -d $scala_install_dir"
			done
		;;
		zookeeper)
			for host in $hosts
			do
				ssh -t $host "$install_sh_home_tmp/local-sh/local-install.sh -t zookeeper -tar $zookeeper_tar_source -sl $zookeeper_slink_path -conf $zookeeper_conf_url -d $zookeeper_install_dir"
			done
		;;
		hadoop)
			for host in $hosts
			do
				ssh -t $host "$install_sh_home_tmp/local-sh/local-install.sh -t hadoop -tar $hadoop_tar_source -sl $hadoop_slink_path -conf $hadoop_conf_url -d $hadoop_install_dir"
			done
		;;
		hbase)
			for host in $hosts
			do
				ssh -t $host "$install_sh_home_tmp/local-sh/local-install.sh -t hbase -tar $hbase_tar_source -sl $hbase_slink_path -conf $hbase_conf_url -d $hbase_install_dir"
			done
		;;
		spark)
			for host in $hosts
			do
				ssh -t $host "$install_sh_home_tmp/local-sh/local-install.sh -t spark -tar $spark_tar_source -sl $spark_slink_path -conf $spark_conf_url -d $spark_install_dir"
			done
		;;
		*)
			echo "the install $type shell not found."
		;;
	esac
}

#同步shell安装脚本到待安装的机器上
function sync_shell(){
	current_cluster_sync_shell_hosts=$cluster_hosts_conf
	if [ "$app" == "all" ] || [ "$app" == "jdk" ] || [ "$app" == "scala" ]; then
		sort $cluster_hosts_conf $cluster_hosts_conf_zk | uniq > $cluster_etc_root/hadoop-zk-union-hosts
		if [ $? -ne 0 ]; then
			echo "the host $cluster_hosts_conf or $cluster_hosts_conf_zk not exists."
			exit $ST_ERR
		fi
		current_cluster_sync_shell_hosts=$cluster_etc_root/hadoop-zk-union-hosts
	elif [ "$app" == "zookeeper" ]; then
		current_cluster_sync_shell_hosts=$cluster_hosts_conf_zk
	else
		current_cluster_sync_shell_hosts=$cluster_hosts_conf
	fi
	
	if [ ! -f "$current_cluster_sync_shell_hosts" ]; then
		echo "please check the install conf '$current_cluster_sync_shell_hosts', not exists"
		exit $ST_ERR
	fi
	
	#1、在集群上创建执行shell上传的临时目录
	sh $cur_sh_parent_dir/sync-tool.sh -c "mkdir $install_sh_home_tmp" -cf $current_cluster_sync_shell_hosts
	echo "create the install tmp dir at '$install_sh_home_tmp' ok!"
	#2、将shell上传到shell临时目录中去
	sh $cur_sh_parent_dir/sync-tool.sh -s $cur_sh_parent_dir/local-sh -t $install_sh_home_tmp -cf $current_cluster_sync_shell_hosts
	echo "upload install shell to '$install_sh_home_tmp' of each host ok!"
}

function clear_tmp(){
	sh $cur_sh_parent_dir/sync-tool.sh -c "rm -rf $install_sh_home_tmp" -cf $current_cluster_sync_shell_hosts
	rm -f $current_cluster_sync_shell_hosts
}

function install(){
	echo -e "\n"
	echo -e "\n"
	
	#1、初始化配置
	if test -z $app; then
		echo "you must point the app by option -a that tell shell what to install , for example jdk, scala, hadoop, zookeeper, hbase, spark. "
		exit $ST_ERR
	fi
	
	init_conf
	
	#2、同步安装脚本
	sync_shell
	
	#3、安装
	if [ $app == "all" ]; then
		#安装jdk
		exec_install jdk
		
		#安装scala
		exec_install scala
		
		#安装zookeeper
		exec_install zookeeper
		
		#安装hadoop
		exec_install hadoop
		
		#安装hbase
		exec_install hbase
		
		#安装spark
		exec_install spark
	else
		exec_install $app
	fi

	#4、删除安装脚本及配置
	clear_tmp
	
	exit $ST_OK
}

#打印帮助文档
function print_help(){
	echo -e "\t--help | -h : print the help document for you."
	echo -e "\t-a : tell shell what app will to install. is you point it as all, will install jdk, scala, zookeeper, hadoop, hbase, spark."
	echo -e "\t-hdp-hosts : you can point the hosts that the shell will exe on each of them. it will replace the default at $cluster_etc_root/hadoop-hosts"
	echo -e "\t-zk-hosts : you can point the hosts that the shell will exe on each of them. it will replace the default at $cluster_etc_root/zk-hosts"
	echo -e "\t-install-conf : you can point the install conf file that tell shell where to install, etc. it will replace the default at $cluster_etc_root/install.conf"
}

#循环处理参数，入口
while test -n "$1"; do
	case "$1" in
		--help | -h)
			print_help
			exit $ST_OK
		;;
		
		-a)
			if test -z $2; then
				echo "please point the type as jdk, scala, zookeeper, hadoop, hbase, spark or all"
				exit $ST_ERR
			fi
			app=$2
			shift
		;;
		
		-hdp-hosts)
			if test -z $2; then
				echo "you pointed the hosts conf is null."
				exit $ST_ERR
			fi
			cluster_hosts_conf=$2
			shift
		;;
		-zk-hosts)
			if test -z $2; then
				echo "you pointed the zookeeper hosts conf is null."
				exit $ST_ERR
			fi
			cluster_hosts_conf_zk=$2
			shift
		;;
		-install-conf)
			if test -z $2; then
				echo "you pointed the install conf is null."
				exit $ST_ERR
			fi
			cluster_install_conf=$2
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

install