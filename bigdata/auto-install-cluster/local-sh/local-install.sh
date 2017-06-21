#!/bin/sh

#安装什么软件
declare type
declare has_conf="true"
declare conf_load_ok=1
#tar包的地址，本地或远程
declare tar_url
#存放从远程下载安装包的目录
declare downloaded_tmp_dir=~/.tmp_cluster_auto_install_downloaded_tars
declare is_mked_tmp_dir=1
declare is_tar_file_download="false"
#安装包文件
declare tar_file
#软件版本，从安装包解压后的目录名
declare app_version
#软件安装目录
declare install_dir
#安装软链接，可选
declare installed_ln
#配置文件所在目录
declare conf_location
#输出环境变量
declare out_env_path

declare OK=0
declare FAILED=1

##自定义功能函数部分start#####################################################################################################
#预检查
function fun_prepare(){
	
	#1、判断是否指定了安装目录
	if test -z $install_dir; then
		echo "fun_prepare(): you must point the install directory!"
		return $FAILED
	fi
	
	if [ -f $install_dir ]; then
		echo "fun_prepare(): you point the install dir by -d is a exists file, $install_dir"
		exit $FAILED
	fi
	
	#2、判断安装目录是否存在，不存在则创建
	if [ ! -d $install_dir -a ! -f $install_dir ]; then
		echo "fun_prepare(): you point the directory not exist, will exec mkdir $install_dir"
		mkdir -p $install_dir
	fi
	
	#3、判断压缩包是否存在
	if test -z "$tar_url"; then
		echo "fun_prepare(): you must point the tar file by option -tar, is a local path or remote path(host:localpath)!"
		return $FAILED
	else
		#解析tar_url, 如果为远程url，则将远程的tar包下载到本地目录中
		if [[ "$tar_url" =~ ":" ]]; then
			#如果安装包在远程url中
			local tar_parth=${tar_url#*:}
			if [ -f $tar_parth ]; then
				#安装包在本地已存在，不用再下载
				tar_file=$tar_parth
				app_version=$(tar -tf $tar_file | awk -F "/" '{print $1}' | sed -n '1p')
			else
				if [ ! -d $downloaded_tmp_dir ]; then
					mkdir $downloaded_tmp_dir
					echo "1" $downloaded_tmp_dir/.is_auto_cluster_install_sh_mkdired.flag
					is_mked_tmp_dir=$OK
				fi
				
				#远程tar file， 下载到本地
				scp $tar_url $downloaded_tmp_dir
				if [ $? -ne 0 ]; then
					echo "the tar file not exist at the url : $tar_url"
					if [ $is_mked_tmp_dir -eq $OK ]; then
						rm -rf $downloaded_tmp_dir
					fi
					exit $ST_ERR
				else
					#下载安装包完成
					tar_file="$downloaded_tmp_dir/${tar_url##*/}"
					app_version=$(tar -tf $tar_file | awk -F "/" '{print $1}' | sed -n '1p')
					is_tar_file_download="true"
				fi
			fi
		else
			#如果配置安装地址是在本机path中
			if [ ! -f $tar_url ]; then
				echo "the local tar file($tar_url) not exists!"
				exit $ST_ERR
			fi
			tar_file=$tar_url
			app_version=$(tar -tf $tar_file | awk -F "/" '{print $1}' | sed -n '1p')
		fi
	fi
	
	#4、判断安装文件是否存在
	local installed_dir=$install_dir/$app_version
	if [ -d "$installed_dir" ]; then
		local files=$(ls $installed_dir)
		if test -n "$files"; then
			#已安装过，则退出，且删除下载的安装包
			echo "fun_prepare(): you have installed the $installed_dir, exit!"
			if [ $is_mked_tmp_dir -eq $OK ]; then
				rm -rf $downloaded_tmp_dir
			else
				rm -f $tar_file
			fi
			return $FAILED
		fi
	fi
	
	#5、检查防火墙
	firewallStatus=$(firewall-cmd --state)
	local cur_host=$(hostname)
	echo "fun_prepare(): the host $cur_host firewall status is  $firewallStatus."
	if [ "$firewallStatus" == "running" ]; then
		echo "fun_prepare(): the host $cur_host firewall not closed, then will close the firewall....."
		#关闭防火墙
		sudo systemctl stop firewalld.service
	fi
	
	return $OK
}


#创建软链接
function fun_crt_sln(){
	local sr=$1
	local ln=$2

	#判断源文件是否存在
	if [ ! -e $sr -a ! -d $sr ]; then
		echo "fun_crt_sln(): $sr is not a file or a directory, the arguments is invalid!"
		return $FAILED
	fi

	#判断软链接是否存在
	if [ -e $ln -o -d $ln ]; then
		echo "fun_crt_sln(): $ln is exits, create slink failed!"
		return $FAILED
	fi

	#创建软链接
	local cur_ln_dir=`dirname $ln`
	if [ -x $cur_ln_dir -a -w $cur_ln_dir ]; then
		ln -s $sr $ln
	else
		echo "curent user for the '$cur_ln_dir' cann't writeable, will use sudo comand."
		sudo ln -s $sr $ln
	fi
	
	return $OK
}

#解压到指定地方
function fun_un_cmprs(){
	local sr=$1
	local tgt=$2
	
	#判断源文件是否存在
	if [ ! -e $sr -a ! -d $sr ]; then
		echo "fun_un_cmprs(): $sr is not exists, uncompressing it failed!"
		return $FAILED
	fi
	
	#判断指定解压的位置是否是个文件 , 不是目录给出提示
	if [ ! -e $tgt ]; then
		echo "fun_un_cmprs(): the destination you want to uncompres is an file, uncompresing failed!"
		return $FAILED
	fi
	
	#如果目标目录不存在，则创建
	if [ ! -d $tgt ]; then
		mkdir -p $tgt
	fi
	
	#解压
	if [ -x $tgt -a -w $tgt ]; then
		tar -zxf $sr -C $tgt
	else
		echo "curent user for the '$tgt' cann't writeable, will use sudo comand."
		sudo tar -zxf $sr -C $tgt
	fi
	
	return $OK
}

#安装
function fun_install(){
	local conf_dir=$conf_location

	#1、安装前检查
	fun_prepare
	if [ $? -eq $FAILED ]; then
		exit $ST_ERR
	fi

	#2、解压
	echo "install(): uncompresing , tar -zxf $tar_file ..............................................."
	fun_un_cmprs $tar_file $install_dir
	if [ $? -eq $FAILED ]; then
		echo "install(): Install hadoop failed, exit!"
		exit $ST_ERR
	fi
	
	#3、创建软链接
	if test -n "$installed_ln"; then
		if [ -d $installed_ln ]; then
			local ln_tmp=$installed_ln/$type
			echo "install(): you point the link dir is $installed_ln, the link file will be $ln_tmp"
			installed_ln=$ln_tmp
		else
			echo "install(): you point the soft link path is $installed_ln"
		fi
		fun_crt_sln "$install_dir/$app_version" $installed_ln
	fi

	#4、下载配置文件
	if [ -n "$conf_dir" -a "$has_conf" = "true" ]; then
		echo "install(): the config file at $conf_dir, then will download it."
		if [ "$type" = "hadoop" ]; then
			echo "install(): scp the conf to the dir $install_dir/$app_version/etc/hadoop/ from $conf_dir"
			scp "$conf_dir/*" "$install_dir/$app_version/etc/hadoop/"
			if [ $? -ne $OK ]; then
				conf_load_ok=$OK
				echo "install(): download the config file failed from the url of '$conf_dir', you should sync manual"
			fi
		else
			echo "install(): scp the conf to the dir $install_dir/$app_version/conf/ from $conf_dir"
			scp "$conf_dir/*" "$install_dir/$app_version/conf/"
			if [ $? -ne $OK ]; then
				conf_load_ok=$OK
				echo "install(): download the config file failed from the url of '$conf_dir', you should sync manual"
			fi
		fi
	fi
	
	#5、安装完毕，清理文件
	echo "install(): install finished, clearing the tmp file and jar file........................................"
	if [ -f "$downloaded_tmp_dir/.is_auto_cluster_install_sh_mkdired.flag" ]; then
		rm -rf $downloaded_tmp_dir
	elif [ "$is_tar_file_download" == "true" ]; then
		rm -f $tar_file
	fi
}

##自定义功能函数部分end#####################################################################################################

#安装JDK##############################################################################################################
function out_jdk_env(){
	#配置java环境变量
	echo "build jdk envirement in file $out_env_path"
	local jdk_env="./.jdk_tmp.sh.tmp"
	echo "#!/bin/sh" > $jdk_env
	echo "export JAVA_HOME=$installed_ln" >> $jdk_env
	echo 'export PATH=$PATH:$JAVA_HOME/bin/' >> $jdk_env
	echo "export CLASSPATH=$JAVA_HOME/lib" >> $jdk_env
	echo "export JRE_HOME=$JAVA_HOME/jre" >> $jdk_env
	sudo sh -c "cat $jdk_env > $out_env_path"
	rm -f $jdk_env
	sleep 1
	source /etc/profile
}

function install_jdk(){
	echo "installing JDK..........................................................................."
	fun_install
	out_jdk_env
}

#安装SCALA##############################################################################################################
function out_scala_env(){
	#配置scala环境变量
	echo "build scala envirement in file $out_env_path"
	local scala_env="./.scala_tmp.sh.tmp"
	echo "export SCALA_HOME=$installed_ln" > $scala_env
	echo 'export PATH=$PATH:$SCALA_HOME/bin' >> $scala_env
	sudo sh -c "cat $scala_env > $out_env_path"
	rm -f $scala_env
	sleep 1
	source /etc/profile
}

function install_scala(){
	echo "installing SCALA..........................................................................."
	fun_install
	out_scala_env
}

#安装zookeeper##############################################################################################################
function gener_zk_myid(){
	local host=$(hostname)
	local zoocfg="$install_dir/$app_version/conf/zoo.cfg"
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
function out_zk_env(){
	#配置java环境变量
	echo "build zookeeper envirement in file $out_env_path"
	local zk_tmp="./.zk_tmp.sh.tmp"
	echo "#!/bin/sh" > $zk_tmp
	echo "export ZOOKEEPER_HOME=$installed_ln" >> $zk_tmp
	echo 'export PATH=$PATH:$ZOOKEEPER_HOME/bin' >> $zk_tmp
	echo 'export CLASSPATH=$CLASSPATH:$ZOOKEEPER_HOME/lib:$ZOOKEEPER_HOME/share' >> $zk_tmp
	sudo sh -c "cat $zk_tmp > $out_env_path"
	rm -f $zk_tmp
	sleep 1
	source /etc/profile
}

#安装zookeeper
function install_zookeeper(){
	echo "installing zookeeper..........................................................................."
	fun_install
	out_zk_env
	gener_zk_myid
	echo "starting zookeeper..........................................................................."
	$install_dir/$app_version/bin/zkServer.sh start
}


#安装hadoop##############################################################################################################
function out_hadoop_env(){
	#配置java环境变量
	echo "build hadoop envirement in file $out_env_path"
	local hadoop_env_tmp="./.hadoop_tmp.sh.tmp"
	echo "#!/bin/sh" > $hadoop_env_tmp
	echo "export HADOOP_HOME=$installed_ln" >> $hadoop_env_tmp
	echo 'export HADOOP_PREFIX=$HADOOP_HOME' >> $hadoop_env_tmp
	echo 'export HADOOP_COMMON_HOME=$HADOOP_PREFIX' >> $hadoop_env_tmp
	echo 'export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_PREFIX/lib/native' >> $hadoop_env_tmp
	echo 'export HADOOP_CONF_DIR=$HADOOP_PREFIX/etc/hadoop' >> $hadoop_env_tmp
	echo 'export HADOOP_HDFS_HOME=$HADOOP_PREFIX' >> $hadoop_env_tmp
	echo 'export HADOOP_MAPRED_HOME=$HADOOP_PREFIX' >> $hadoop_env_tmp
	echo 'export HADOOP_YARN_HOME=$HADOOP_PREFIX' >> $hadoop_env_tmp
	echo 'export LD_LIBRARY_PATH=$HADOOP_PREFIX/lib/native' >> $hadoop_env_tmp
	echo 'export PATH=$PATH:$HADOOP_HOME/bin' >> $hadoop_env_tmp
	echo 'export PATH=$PATH:$HADOOP_HOME/sbin' >> $hadoop_env_tmp
	
	sudo sh -c "cat $hadoop_env_tmp > $out_env_path"
	rm -f $hadoop_env_tmp
	sleep 1
	source /etc/profile
}

#安装hadoop
function install_hadoop(){
	echo "installing hadoop..........................................................................."
	fun_install
	out_hadoop_env
}

#安装hbase##############################################################################################################
function out_hbase_env(){
	#配置java环境变量
	echo "build hbase envirement in file $out_env_path"
	local hbase_tmp="./.hbase_env_tmp.sh.tmp"
	echo "#!/bin/sh" > $hbase_tmp
	echo "export HBASE_HOME=$installed_ln" >> $hbase_tmp
	echo 'export PATH=$PATH:$HBASE_HOME/bin' >> $hbase_tmp
	echo 'export CLASSPATH=$CLASSPATH:$HBASE_HOME/lib' >> $hbase_tmp
	
	sudo sh -c "cat $hbase_tmp > $out_env_path"
	rm -f $hbase_tmp
	sleep 1
	source /etc/profile
}

#安装hbase
function install_hbase(){
	echo "installing hbase..........................................................................."
	fun_install
	out_hbase_env
}

#安装spark##############################################################################################################
function out_spark_env(){
	#配置java环境变量
	echo "build spark envirement in file $out_env_path"
	local spark_tmp="./.spark_env_tmp.sh.tmp"
	echo "#!/bin/sh" > $spark_tmp
	echo "export SPARK_HOME=$installed_ln" >> $spark_tmp
	echo 'export PATH=$PATH:$SPARK_HOME/sbin' >> $spark_tmp
	
	sudo sh -c "cat $spark_tmp > $out_env_path"
	rm -r $spark_tmp
	sleep 1
	source /etc/profile
}

#安装spark
function install_spark(){
	echo "installing spark..........................................................................."
	fun_install
	out_spark_env
}

#打印帮助文档##############################################################################################################
function print_help(){
	echo -e "\t-h | --help : Will print the help document."
	echo -e "\t-t(integrant) : what you want to install, like as jdk, scala, zookeeper, hadoop, hbase, spark"
	echo -e "\t-tar(integrant) : the path of tar file that you want to install."
	echo -e "\t-d(integrant) : the directory that you want install to."
	echo -e "\t-conf(integrant) : the cont dir, hadoop $HADOOP_HOME/etc/hadoop, or $HBASE_HOME/conf..."
	echo -e "\t-sl(optional) : point the soft link location you want to, default at ~, if you don't point it."
}

#参数解析入口##############################################################################################################
while test -n "$1"; do
	case "$1" in
		--help | -h)
			print_help
			exit $ST_OK
		;;
		
		#分发上传参数###################################################################################
		-t)
			if test -z "$2"
			then
				echo "parse param : please input type by -t, zookeeper, hadoop, hbase, spark....."
				exit $ST_ERR
			fi
			echo "parse param : you point the type is $2, will install $2!"
			type=$2	
			shift
		;;
		
		#tar包所在路径，必须参数
		-tar)
			if test -z "$2"; then
				echo "parse param : you must point the tar url by optio -tar, is a local path or a remote path(host:localpath)"
				exit $ST_ERR
			fi
			tar_url=$2
			shift
		;;
		
		-d)
			if [ ! -d $2 ]; then
				#指定的安装目录不存在
				echo "parse param : the install directory is not found, check it"
				exit $ST_ERR
			fi
			install_dir=$2	
			shift
		;;
		
		-sl)
			if [ ! -d $2 -o -f $2 ]; then
				echo "parse param : you pointed the soft link directory ($2) not exist or is a exists file, check it."
				exit $ST_ERR
			fi
			installed_ln=$2
			shift
		;;
		
		-conf)
			if test -z $2; then
				echo "parse param : the -conf don't point the conf location, is like host:/home/hadoop/conf/etc/hadoop"
				$ST_ERR
			fi
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

#安装应用主入口##############################################################################################################
function install_main(){
	echo "∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧"
	if test -z "$type"; then
		echo "install_main(): please point the install type by -t, for what software you to install."
		exit $ST_ERR
	fi
	
	if test -z "$tar_url"; then
		echo "install_main(): please point the tar file by option -tar, is a tar file."
		exit $ST_ERR
	fi
	
	if test -z "$install_dir"; then
		echo "install_main(): please point the install directory by option -d, is a directory."
		exit $ST_ERR
	fi

	out_env_path=/etc/profile.d/${type}_evn.sh
	case "$type" in
		jdk)
			has_conf="false"
			install_jdk
		;;
		scala)
			has_conf="false"
			install_scala
		;;
		zookeeper)
			if test -z "$conf_location"; then
				echo "install_main(): please point the conf location by option -conf, like as 'host@:/home/hadoop/upload/conf/$type'."
				exit $ST_ERR
			fi
			install_zookeeper
		;;
		hadoop)
			if test -z "$conf_location"; then
				echo "install_main(): please point the conf location by option -conf, like as 'host@:/home/hadoop/upload/conf/$type'."
				exit $ST_ERR
			fi
			install_hadoop
		;;
		hbase)
			if test -z "$conf_location"; then
				echo "install_main(): please point the conf location by option -conf, like as 'host@:/home/hadoop/upload/conf/$type'."
				exit $ST_ERR
			fi
			install_hbase
		;;
		spark)
			if test -z "$conf_location"; then
				echo "install_main(): please point the conf location by option -conf, like as 'host@:/home/hadoop/upload/conf/$type'."
				exit $ST_ERR
			fi
			install_spark
		;;
		
		*)
			echo "your type '$type' is unresolved, please point type as jdk, scala, zookeeper, hadoop, hbase, spark."
		;;
	esac
	echo "∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨"
}

install_main