#!/bin/sh

########################hbase#############################
ST_OK=0
ST_ER=1
#call the method:connect_to_mysql,create the jdbc variable:username, password, jdbc_uri to the method
USER="lvmama_user.username"
PASSWORD="lvmama_user.password"
JDBC_URI="lvmama_user.uri"
jdbc_property_path="/home/hadoop/cdh5.2.4/sqoop-1.4.5-cdh5.2.4/sqoop-scripts/mysql_pwd.txt"

#job_name
full_job_name="user_user_mysql.full"
incr_job_name="user_user_mysql.incr"

mysql_table=lvmama_user.user_user
query="select user_id as \"user_id\", user_no as \"user_no\", city_id as \"city_id\", user_name as \"user_name\", real_name as \"real_name\", address as \"address\", created_date as \"created_date\", updated_date as \"updated_date\", is_valid as \"is_valid\", if(mobile_number is null, null, '1') \"mobile_number\", if(email is null, null, '1') as \"email\", gender as \"gender\", id_number as \"id_number\", point as \"point\", nick_name as \"nick_name\", memo as \"memo\", birthday as \"birthday\", if(qq_account is null, null, '1') as \"qq_account\", if(msn_account is null, null, '1') as \"msn_account\", space_url as \"space_url\", image_url as \"image_url\", is_email_checked as \"is_email_checked\",  if(phone_number is null, null, '1') as \"phone_number\", is_accept_edm as \"is_accept_edm\", is_mobile_checked as \"is_mobile_checked\", membership_card as \"membership_card\", active_mscard_date as \"active_mscard_date\", primary_channel as \"primary_channel\", grade as \"grade\", level_validity_date as \"level_validity_date\", group_id as \"group_id\", last_login_date as \"last_login_date\", name_is_update as \"name_is_update\", if(wechat_id is null, null, '1') as \"wechat_id\", subscribe as \"subscribe\", update_time as \"update_time\", save_credit_card as \"save_credit_card\", is_zj as \"is_zj\", user_type as \"user_type\", user_status as \"user_status\", point_change_version as \"point_change_version\", active_status as \"active_status\", wechat_unionid as \"wechat_unionid\" from lvmama_user.user_user "
split_by=user_id
where=" where "
boundary_query="select min(user_id), max(user_id) from lvmama_user.user_user "
boundary_where=" where "

hbase_table=user_user
hbase_row_key=user_id
hbase_column_family=info

full_map_num=10
incr_map_num=1
sync_type=""
startdate=""
enddate=""

sqoop_log_path=""
log_path=""
log_root_dir=/home/hadoop/cdh5.2.4/sqoop-1.4.5-cdh5.2.4/sqoop-scripts/logs

#initialize the log output path
init_log_output(){
	start_date_range=${startdate}
	end_date_range=${endDate}
	
	#end date is null, default today
	if [ "$endDate"x = ""x ]; then
			end_date_range=$(date +"%Y%m%d")
	fi

	curDayLogDir=$log_root_dir/$end_date_range
	
	if [ ! -d "$curDayLogDir" ]; then
			mkdir $curDayLogDir
	fi
	
	if [ "$sync_type"x = "incr"x  ]; then
		log_path=$curDayLogDir/${incr_job_name}.'date_range['${start_date_range}-${end_date_range}']'.'exe_time['$(date +"%Y-%m-%d %H-%M")']'.log
		sqoop_log_path=$curDayLogDir/${incr_job_name}.'date_range['${start_date_range}-${end_date_range}']'.'exe_time['$(date +"%Y-%m-%d %H-%M")']'.sqoop.log
	else
		log_path=$log_root_dir/${full_job_name}.'exe_time['$(date +"%Y-%m-%d %H-%M")']'.log
		sqoop_log_path=$log_root_dir/${full_job_name}.'exe_time['$(date +"%Y-%m-%d %H-%M")']'.sqoop.log
	fi
}


#command help manual
print_help() {
	echo "desc:"
	     echo "	$0 sync hbase table data to mysql"
	echo "Options:"
	     echo "	--help|-h)"
	     echo "		help option"
	     echo "	--sync_type|-s)"
	     echo "		incr type Incremental sync mysql table to hbase table,default type is Incremental sync"
	     echo "		|| all type sync full mysql to hbase"
	     echo "	--start_date|-sd"
	     echo "		sync date start date"
	     echo "	--end_date|-ed"
	     echo "		sync date end date"
	exit  $ST_OK
}

sync_type=""

while test -n "$1"; do
    case "$1" in
        --help|-h)
            print_help
            exit $ST_OK
            ;;
        --sync_type|-s)
            sync_type=$2
            shift
            ;;
	--start_date|-sd)
	    startdate=$2
	    shift
	    ;;
	--end_date|-ed)
	    enddate=$2
	    shift
	    ;;
        *)
            echo "Unknown argument: $1"
            print_help
            exit $ST_ER
            ;;
        esac
    shift
done

connect_to_mysql() {
#get jdbc info
while read line
do
        key=`echo $line | cut -d  ";"  -f 1 `
        value=` echo "$line" | cut -d ";" -f 2`
        case "$key"x in
                "$USER"x)
                        username=$value
                        ;;
                "$PASSWORD"x)
                        password=$value
                        ;;
                "$JDBC_URI"x)
                		jdbc_uri="$value"
                        ;;
        esac
done < $jdbc_property_path
}

mysql_to_hbase() {

	#update insert
	where="$where"" \$CONDITIONS"
	query='"'"$query""$where"'"'
	boundary_where=""
	boundary_query='"'"$boundary_query""$boundary_where"'"'
	
	echo ""import -Dmapreduce.map.speculative=false -Dmapreduce.reduce.speculative=false --mapreduce-job-name  "$full_job_name" --connect $jdbc_uri --password $password --username $username -m $full_map_num --split-by $split_by  --boundary-query "$boundary_query" --query "$query" --hbase-create-table --column-family  $hbase_column_family --hbase-table $hbase_table --hbase-row-key $hbase_row_key >> $log_path

	/home/hadoop/cdh5.2.4/sqoop-1.4.5-cdh5.2.4/bin/sqoop import -Dmapreduce.map.speculative=false -Dmapreduce.reduce.speculative=false --mapreduce-job-name  "$full_job_name" --connect $jdbc_uri --password $password --username $username -m $full_map_num --split-by $split_by --boundary-query "$boundary_query" --query "$query" --hbase-create-table --column-family  $hbase_column_family --hbase-table $hbase_table --hbase-row-key $hbase_row_key > $sqoop_log_path  2>&1

}

incr_sync() {
	echo "$(date +"%y/%m/%d %H:%M:%S") starting mysql table $mysql_table incr sync to hbase table $hbase_table ....." > $log_path
	
	if [ "$enddate"x != ""x ]; then
		where="$where"" (updated_date"">=""str_to_date('"$startdate"','%Y%m%d') "" or created_date"">=""str_to_date('"$startdate"','%Y%m%d')) and (updated_date""<=""str_to_date('"$enddate"','%Y%m%d') "" or created_date""<=""str_to_date('"$enddate"','%Y%m%d'))"" and \$CONDITIONS"
	else
		where="$where"" (updated_date"">=""str_to_date('"$startdate"','%Y%m%d') "" or created_date"">=""str_to_date('"$startdate"','%Y%m%d'))"" and \$CONDITIONS"
	fi

	query='"'"$query""$where"'"'
	
	echo ""import -Dmapreduce.map.speculative=false -Dmapreduce.reduce.speculative=false --mapreduce-job-name "$incr_job_name" --connect $jdbc_uri --password $password --username $username -m "$incr_map_num" --query "$query" --hbase-create-table --column-family $hbase_column_family --hbase-table $hbase_table --hbase-row-key $hbase_row_key >> $log_path

    	/home/hadoop/cdh5.2.4/sqoop-1.4.5-cdh5.2.4/bin/sqoop import -Dmapreduce.map.speculative=false -Dmapreduce.reduce.speculative=false --mapreduce-job-name "$incr_job_name" --connect $jdbc_uri --password $password --username $username -m "$incr_map_num" --query "$query" --hbase-create-table --column-family  $hbase_column_family --hbase-table $hbase_table --hbase-row-key $hbase_row_key > $sqoop_log_path  2>&1
	
	echo "$(date +"%y/%m/%d %H:%M:%S") ended mysql table $mysql_table incr sync to hbase table $hbase_table" >>  $log_path
}

full_sync() {
	echo "$(date +"%y/%m/%d %H:%M:%S") starting mysql table $mysql_table sync to hbase table $hbase_table ....." > $log_path
	
	mysql_to_hbase	

	echo "$(date +"%y/%m/%d %H:%M:%S") ended mysql table $mysql_table sync to hbase table $hbase_table" >> $log_path
}

sync_data() {
	   
	if [ "$sync_type"x = "incr"x  ]; then
	     if [ "$startdate"x != ""x ]; then
		echo "$(date +"%y/%m/%d %H:%M:%S") start inrc sysc date is $startdate"
	     else
		startdate=$(date -d "2 day ago" +"%Y%m%d")
		echo "$(date +"%y/%m/%d %H:%M:%S") default start inrc sysc 1 day ago date"
	     fi
	     if [ "$enddate"x != ""x ]; then
		echo "$(date +"%y/%m/%d %H:%M:%S") end inrc sysc date is $enddate"
	     fi
	     init_log_output
	     connect_to_mysql
	     incr_sync
	else
		 init_log_output
	     connect_to_mysql
	     full_sync
	fi
}

# Runtime
sync_data

