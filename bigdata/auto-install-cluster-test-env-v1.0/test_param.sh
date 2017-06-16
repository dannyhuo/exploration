function test_param(){
	local conf_dir=$1
	if [ -n $conf_dir ]; then
		echo "conf_dir param is not null, $conf_dir"
	else
		echo "conf_dir param is null"

	fi
}


function gener_myid(){
        local host=$1
	local conf_dir=$2
        local zoocfg="$conf_dir/zookeeper/zoo.cfg"
	local line=$(cat $zoocfg | grep $host)
	local left=${line%=*}
	local myid=${left#*.}

	local dd_line=$(cat $zoocfg | grep 'dataDir=')
	local dataDir=${dd_line#*=}

	if [ ! -d $dataDir ]; then
		sudo mkdir -p $dataDir
	fi
	
	echo "create myid in folder $dataDir, myid is $myid"
	sudo sh -c "echo $myid > $dataDir/myid"
}

gener_myid $1 $2

#test_param $1
