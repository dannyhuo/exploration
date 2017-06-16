#!/usr/bin/env bash

declare source_dir=""
declare target_dir=""

function print_help(){
	echo "the shell $1, for help as follows:"
	echo "-h | --help: print the helper for you"
	echo "-s | -source: point the source that you want cope to target directory, is a file or directory"
	echo "-t | -target: point the target that you want cope to, it's a directory"
}

function sync_once(){
	local tg=$2
	local host=$3
	
	if [ -d $sr ]; then
		echo "scp -r $sr $host:$tg"
		scp -r $sr $host:$tg
	fi
	
	if [ -f $sr ]; then
		echo "$sr $host:$tg"
		scp $sr $host:$tg
	fi
}


function sync(){
	local sr=$1
	local tg=$2
	
	if [ ! -d $tg ]; then
		return
	fi
	
	source=$sr
	
	targetDir=$tg

	for host in ${sync_hosts[@]}
	do
		echo "sync the source($source) to the host(${host}) of $targetDir"
		sync_once $source $targetDir $host
	done
}


#循环处理参数，入口
while test -n "$1"; do
	case "$1" in
		--help | -h)
			print_help
			exit $ST_OK
		;;
		
		-s | -source)
			if [ -d $2 -o -f $2 ]; then
				source_dir=$2
			else
				echo "the -s or -source option must be point a file or directory!"
				exit $ST_OK
			fi
			shift
		;;
		
		-t | -target)
			if [ -d $2 ]; then
				target_dir=$2
			else
				echo "the -t or -target must be an exists directory!"
				exit $ST_OK
			fi
			shift
		;;
		
		*)
			echo "unknow argument: $1"
			
			exit $ST_ERR
		;;
	esac
	
	shift
done

echo $source_dir
echo $target_dir
#sync $1 $2

