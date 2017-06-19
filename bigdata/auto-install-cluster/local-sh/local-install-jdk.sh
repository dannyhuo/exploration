#!/bin/sh
declare install_dir
declare installed_ln
declare conf_location
#安装源文件
declare tar_file_source

#打印帮助文档##############################################################################################################
function print_help(){
	echo -e "\t-h | --help : Will print the help document."
	echo -e "\t-t(integrant) : what you want to install, like as jdk, scala, zookeeper, hadoop, hbase, spark"
	echo -e "\t-ts(integrant) : the path of tar file source that you want to install. like host:/home/hadoop/hadoop-2.8.0.gar.gz"
	echo -e "\t-d(integrant) : the directory that you want install to."
	echo -e "\t-conf(integrant) : the cont dir, hadoop $HADOOP_HOME/etc/hadoop, or $HBASE_HOME/conf..."
	echo -e "\t-sl(optional) : point the soft link location you want to, default at ~, if you don't point it."
}

#校验参数##############################################################################################################
function check_param(){
	if test -z "$type"; then
		echo "install_main(): please point the install type by -t, for what software you to install."
		exit $ST_ERR
	fi
	
	if test -z "$tar_file_source"; then
		echo "check_param(): please point the tar file by option -tar, is a tar file."
		exit $ST_ERR
	fi
	
	if test -z "$install_dir"; then
		echo "check_param(): please point the install directory by option -d, is a directory."
		exit $ST_ERR
	fi
	
	if test -z "$conf_location"; then
		echo "check_param(): please point the conf location by option -conf, like as 'host@:/home/hadoop/upload/conf/hadoop'."
		exit $ST_ERR
	fi
	
	#软链接目录如果没指定，默认给定为根目录
	if test -z $installed_ln; then
		echo "check_param: you don't point the soft link path, default is current user's root directory!"
		installed_ln="~/"
	fi
}

#参数解析入口##############################################################################################################
while test -n "$1"; do
	case "$1" in
		--help | -h)
			print_help
			exit $ST_OK
		;;
		
		#tar包所在路径，必须参数
		-ts)
			tar_file_source=$2
			shift
		;;
		
		#安装目录，必须参数
		-d)
			install_dir=$2	
			shift
		;;
		
		#软链接目录
		-sl)
			installed_ln=$2
			shift
		;;
		
		#配置文件， host:path
		-conf)
			conf_location=$2
			shift
		;;
		
		*)
			echo "parse param : unknow options: $1"
			print_help
			exit $ST_ERR
		;;
	esac
	shift
done


#校验参数
check_param
#创建源文件临时目录，将指定的安装包下载到此目录中
local source_dir="$install_dir/source_tmp"
mkdir $source_dir
scp $tar_file_source $source_dir
#调用安装脚本
sh local_install.sh -t jdk -tar $tar_file -d $install_dir -sl $installed_ln -conf $conf_location
#安装完毕后删除安装包
rm -rf $source_dir