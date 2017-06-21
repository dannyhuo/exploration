#!/bin/sh
#当前shell所在的根目录
declare sh_parent_dir=$(dirname $0)
echo "Current sh of '${0}' root directory is '$sh_parent_dir'"

declare sync_sh_tmp_dir=~/.tmp-auto-install-cluster-shs

#1、同步安装脚本及配置
sh $sh_parent_dir/sync-tool.sh -c "mkdir $sync_sh_tmp_dir" -cf "$sh_parent_dir/etc/zk-hosts"
sh $sh_parent_dir/sync-tool.sh -s $sh_parent_dir/local-sh -t $sync_sh_tmp_dir -cf "$sh_parent_dir/etc/zk-hosts"

#2、安装
sh $sh_parent_dir/cluster-install.sh zookeeper $sync_sh_tmp_dir

#3、删除安装脚本及配置
sh $sh_parent_dir/sync-tool.sh -c "rm -rf $sync_sh_tmp_dir" -cf "$sh_parent_dir/etc/zk-hosts"

#4、查看zk状态
echo "The each zk node's status as follows : "
sh $sh_parent_dir/sync-tool.sh -c '$ZOOKEEPER_HOME/bin/zkServer.sh status' -cf "$sh_parent_dir/etc/zk-hosts"
