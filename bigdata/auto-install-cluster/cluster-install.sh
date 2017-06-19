#!/bin/sh

#安装软件的名称
declare app_name=$1


#jdk安装配置
declare jdk.tar.source=master1:/home/hadoop/hadoop_software_package/
declare jdk.install.dir=/home/hadoop/cluster_auto_install
declare jdk.slink.path=/home/hadoop/cluster_auto_install

#scala安装配置
declare scala.tar.source=master1:/home/hadoop/hadoop_software_package/
declare scala.install.dir=/home/hadoop/cluster_auto_install
declare scala.slink.path=/home/hadoop/cluster_auto_install


#hadoop安装配置
declare hadoop.tar.source=master1:/home/hadoop/hadoop_software_package/
declare hadoop.install.dir=/home/hadoop/cluster_auto_install
declare hadoop.slink.path=/home/hadoop/cluster_auto_install
declare hadoop.conf.url=master1:/home/hadoop/upload/conf/hadoop

#hbase安装配置
declare hbase.tar.source=master1:/home/hadoop/hadoop_software_package/
declare hbase.install.dir=/home/hadoop/cluster_auto_install
declare hbase.slink.path=/home/hadoop/cluster_auto_install
declare hbase.conf.url=master1:/home/hadoop/upload/conf/hbase

#spark安装配置
declare spark.tar.source=master1:/home/hadoop/hadoop_software_package/
declare spark.install.dir=/home/hadoop/cluster_auto_install
declare spark.slink.path=/home/hadoop/cluster_auto_install
declare spark.conf.url=master1:/home/hadoop/upload/conf/spark

while read line
do
	if [ -n $line -a "$line" != "#*" ]; then
		local value=${line#*=}
		if [ "$line" = "jdk.tar.source*" ]; then
			jdk.tar.source=$value
		else if [ "$line" = "jdk.install.dir*" ]; then
			jdk.install.dir=$value
		else if [ "$line" = "jdk.slink.path*" ]; then
			jdk.slink.path=$value
			
		else if [ "$line" = "scala.tar.source*" ]; then
			scala.tar.source=$value
		else if [ "$line" = "scala.install.dir*" ]; then
			scala.install.dir=$value
		else if [ "$line" = "scala.slink.path*" ]; then
			scala.slink.path=$value
			
		else if [ "$line" = "hadoop.tar.source*" ]; then
			hadoop.tar.source=$value
		else if [ "$line" = "hadoop.install.dir*" ]; then
			hadoop.install.dir=$value
		else if [ "$line" = "hadoop.slink.path*" ]; then
			hadoop.slink.path=$value
		else if [ "$line" = "hadoop.conf.url*" ]; then
			hadoop.conf.url=$value
		
		else if [ "$line" = "hbase.tar.source*" ]; then
			hbase.tar.source=$value
		else if [ "$line" = "hbase.install.dir*" ]; then
			hbase.install.dir=$value
		else if [ "$line" = "hbase.slink.path*" ]; then
			hbase.slink.path=$value
		else if [ "$line" = "hbase.conf.url*" ]; then
			hbase.conf.url=$value
		
		else if [ "$line" = "spark.tar.source*" ]; then
			spark.tar.source=$value
		else if [ "$line" = "spark.install.dir*" ]; then
			spark.install.dir=$value
		else if [ "$line" = "spark.slink.path*" ]; then
			spark.slink.path=$value
		else if [ "$line" = "spark.conf.url*" ]; then
			spark.conf.url=$value
		else
			echo "you config file(./etc/install.conf) found the invalid conf: $line"
		fi
	fi
done < ./etc/install.conf


#参数解析入口##############################################################################################################
while read line
do
	sh -t $line "./local-sh/local-install-${app_name}.sh -t $app_name -ts  ${app_name}.tar.source -sl ${app_name}.slink.path -conf ${app_name}.conf.url -d ${app_name}.install.dir"
done < ./etc/cluster-hosts

