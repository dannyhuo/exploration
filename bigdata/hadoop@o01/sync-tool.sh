#!/bin/sh

declare source
declare target_dir

declare download_host
declare download_source
declare to_location

declare cluster_cmd

declare sh_parent_dir=$(dirname $0)

declare cluster_conf="$sh_parent_dir/etc/hadoop-hosts"

declare cluster_hosts

#使用sudo默认为false,可手动指定为true
declare sudo_flag="false"

#展示帮助命令
function print_help(){
	echo -e "the shell $1, for help as follows:"
	echo -e "\t-h | --help: print the helper for you"
	
	echo -e "\t-s | -source: point the source that you want upload to target directory for each host, is a file or directory"
	echo -e "\t-t | -target: point the target that you want sync to at each host, it's a directory"
	
	echo -e "\t-dh : download host, the download host, is a host or ip address"
	echo -e "\t-ds : download source path, the directory or file on the remote host, witch point by the -lh option!"
	echo -e "\t-tl : to location, it't a local derectory to whitch you want download the remote file!"
	
	echo -e "\t-c : to point the command that will exec on each machine of the cluster!"
	echo -e "\t-sudo : sudo eq true, will use sudo exec the command, the others will not use sudo!"
	
	echo -e "\t-cf: to point the path of cluster hosts config file. default at ./etc/hadoop-hosts!"
}

#循环处理参数，入口
while test -n "$1"; do
	case "$1" in
		--help | -h)
			print_help
			exit $ST_OK
		;;
		
		#分发上传参数###################################################################################
		-s | -source)
			if [ -a $2 ]; then
				source=$2
			else
				echo "the -s or -source option must be point a file or directory, you give the path '$2' not a directory!"
				exit $ST_OK
			fi
			shift
		;;
		
		-t | -target)
			if [ -n $2 ]; then
				target_dir=$2
			else
				echo "you must point the target directory by options -t or -target!"
				exit $ST_OK
			fi
			shift
		;;
		
		#远程下载参数###################################################################################
		-dh)
			if [ -n $2 ]; then
				download_host=$2
			else
				echo "the -dh point a host whitch you want to download files!"
				exit $ST_OK
			fi
			shift
		;;
		
		-ds)
			if [ -n $2 ]; then
				download_source=$2
			else
				echo "the -ds must not be null , if this path not exist in remote host, will download failed!"
				exit $ST_OK
			fi
			shift
		;;
		
		-tl)
			if [ -d $2 ]; then
				to_location=$2
			else
				echo "the -tl must be point an exists directory at localhost!"
				exit $ST_OK
			fi
			shift
		;;
		
		#同步执行集群命令###################################################################################
		-c)
			if ! test -z "$2" 
			then
				cluster_cmd=$2
			else
				echo "the -c must be point a command that will exec on the cluster!"
				exit $ST_OK
			fi
			shift
		;;
		
		-sudo)
			sudo_flag=$2
			shift
		;;
		
		#同步执行集群命令###################################################################################
		-cf)
			if [ -f $2 ]; then
				cluster_conf=$2
			fi
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


function main(){
	
	if [ ! -f $cluster_conf ]; then
		echo "you must point the cluster hosts by -cf or write in default './etc/hadoop-hosts file'."
		exit $ST_ERR
	fi
	
	#上传
	if ! test -z $source  && ! test -z $target_dir 
	then
		local hosts_read_from_conf=`cat $cluster_conf`
		for host in $hosts_read_from_conf
		do
			echo "will sync the path '$source' to the directory '$target_dir' at host of '$host'"
			scp -r $source $host:$target_dir 
		done
	fi

	#下载
	if test -n "$download_host" && test -n "$download_source"  && test -n "$to_location"
	then
		echo "will download from '$download_host:$download_source' to '$to_location'! "
		scp -r $download_host:$download_source $to_location
	fi

	#同步执行命令
	if test -n "$cluster_cmd"
	then
		local hosts=`cat $cluster_conf`
		for host in $hosts
		do
			echo "will exec the command '$cluster_cmd' at host '$host'."
			if [ "$sudo_flag" == "true" ]; then
				ssh -t $host "sudo $cluster_cmd"
			else
				ssh -t $host "$cluster_cmd"
			fi
			echo -e "\n"
		done
	fi
}

main