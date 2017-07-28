package crm.up.model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zoubin on 2017-7-11.
 */
public class SqlUtil {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args){
        getSqlInfo("a");
    }

    //由json串解析出sql、表与列
    public static Map<String, Object> getSqlInfo(String modelItemsJson) {
//        modelItemsJson = "[{\"upModelId\":27,\"upModelItemDefId\":1,\"operator\":\"NGT\",\"value1\":\"2016-02-08 13:48:01\",\"status\":\"1\",\"tableName\":\"user_portrait\",\"columnName\":\"created_date\",\"groupId\":\"U\",\"itemDesc\":\"注册时间：不大于    2016-02-08 13:48:01\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Date\"},{\"upModelId\":27,\"upModelItemDefId\":2,\"operator\":\"IN\",\"value1\":\"'GP_GRONT','GP_TECH','GP_ORDER','GP_PHONE','O2O','IPHONE','ANDORID','IPAD','TOUCH','WAP','WP8'\",\"status\":\"1\",\"tableName\":\"user_portrait\",\"columnName\":\"group_id\",\"groupId\":\"U\",\"itemDesc\":\"注册来源:  等于  主站,分销或后台,下单,电话,O2O,IPHONE,ANDORID,IPAD,TOUCH,WAP,WP8\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"VR\"},{\"upModelId\":27,\"upModelItemDefId\":4,\"operator\":\"IN\",\"value1\":\"'M','F'\",\"status\":\"1\",\"tableName\":\"user_portrait\",\"columnName\":\"gender\",\"groupId\":\"U\",\"itemDesc\":\"性别:  等于  男,女\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"VR\"},{\"upModelId\":27,\"upModelItemDefId\":9,\"operator\":\"NLT\",\"value1\":\"40\",\"status\":\"1\",\"tableName\":\"user_portrait\",\"columnName\":\"point\",\"groupId\":\"U\",\"itemDesc\":\"积分：不小于    40\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Int\"},{\"upModelId\":27,\"upModelItemDefId\":10,\"operator\":\"GTS\",\"value1\":\"8\",\"value2\":\"d\",\"status\":\"1\",\"tableName\":\"user_portrait\",\"columnName\":\"last_login_date\",\"groupId\":\"U\",\"itemDesc\":\"最近登录时间：时间点至运行时间之间    8 天\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Date\"},{\"upModelId\":27,\"upModelItemDefId\":11,\"operator\":\"NULL\",\"value1\":\"\",\"status\":\"1\",\"tableName\":\"user_portrait\",\"columnName\":\"mobile_number\",\"groupId\":\"U\",\"itemDesc\":\"手机号(填写):  空\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Null\"},{\"upModelId\":27,\"upModelItemDefId\":12,\"operator\":\"NULL\",\"value1\":\"\",\"status\":\"1\",\"tableName\":\"user_portrait\",\"columnName\":\"email\",\"groupId\":\"U\",\"itemDesc\":\"邮件(填写):  空\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Null\"},{\"upModelId\":27,\"upModelItemDefId\":13,\"operator\":\"LT\",\"value1\":\"2016-02-17 13:49:27\",\"status\":\"1\",\"tableName\":\"ord_order\",\"columnName\":\"create_time\",\"groupId\":\"O\",\"itemDesc\":\"订单时间：小于    2016-02-17 13:49:27\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Date\"},{\"upModelId\":27,\"upModelItemDefId\":15,\"operator\":\"IN\",\"value1\":\"'NORMAL','CANCEL','FINISHED'\",\"status\":\"1\",\"tableName\":\"ord_order\",\"columnName\":\"order_status\",\"groupId\":\"O\",\"itemDesc\":\"订单状态:  等于  正常,取消,SUPER结束\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"VR\"},{\"upModelId\":27,\"upModelItemDefId\":16,\"operator\":\"LT\",\"value1\":\"100\",\"status\":\"1\",\"tableName\":\"ord_order\",\"columnName\":\"ought_amount\",\"groupId\":\"O\",\"itemDesc\":\"购买金额(应付)：小于    100\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Int\"},{\"upModelId\":27,\"upModelItemDefId\":17,\"operator\":\"NLT\",\"value1\":\"55\",\"status\":\"1\",\"tableName\":\"ord_order\",\"columnName\":\"actual_amount\",\"groupId\":\"O\",\"itemDesc\":\"购买金额(实付)：不小于    55\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Int\"},{\"upModelId\":27,\"upModelItemDefId\":18,\"operator\":\"BT\",\"value1\":\"2016-02-01 13:50:21\",\"value2\":\"2016-02-22 13:50:24\",\"status\":\"1\",\"tableName\":\"ord_order\",\"columnName\":\"payment_time\",\"groupId\":\"O\",\"itemDesc\":\"支付时间：介于    2016-02-01 13:50:21  ~  2016-02-22 13:50:24\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Date\"},{\"upModelId\":27,\"upModelItemDefId\":20,\"operator\":\"NGT\",\"value1\":\"2016-02-11 13:50:31\",\"status\":\"1\",\"tableName\":\"ord_order\",\"columnName\":\"visit_time\",\"groupId\":\"O\",\"itemDesc\":\"出游时间：不大于    2016-02-11 13:50:31\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"Date\"},{\"upModelId\":27,\"upModelItemDefId\":21,\"operator\":\"IN\",\"value1\":\"'PAYED','PARTPAY'\",\"status\":\"1\",\"tableName\":\"ord_order\",\"columnName\":\"payment_status\",\"groupId\":\"O\",\"itemDesc\":\"支付状态:  等于  支付完成,部分支付\",\"creatorId\":\"op00001\",\"creatorName\":\"主管\",\"itemDefType\":\"VR\"}]";
        JSONArray itemListArray = JSONArray.fromObject(modelItemsJson);
        Iterator it = itemListArray.iterator();

        StringBuffer targetTables = new StringBuffer();
        StringBuffer whereClause = new StringBuffer(" where 1=1");
        Set<String> set = new HashSet<String>();//save tables to construct join condition for tables
        Map<String, Set<String>> tableColumnsMap = new HashMap<String, Set<String>>();//save table and it's columns

        Set<String> userColumnSet = new HashSet<String>();
        userColumnSet.add("user_id");
        tableColumnsMap.put("user_user", userColumnSet);
        while (it.hasNext()) {
            JSONObject itemJson = (JSONObject) it.next();
//            System.out.println(itemJson.toString());
            String value1 = itemJson.get("value1") == null ? null : itemJson.get("value1").toString().trim();
            String value2 = itemJson.get("value2") == null ? null : itemJson.get("value2").toString().trim();
            String operator = itemJson.get("operator") == null ? null : itemJson.get("operator").toString().trim();
            String itemDefType = itemJson.get("itemDefType") == null ? null : itemJson.get("itemDefType").toString().trim();
            String tableName = itemJson.get("tableName") == null ? null : itemJson.get("tableName").toString().trim();
            String columnName = itemJson.get("columnName") == null ? null : itemJson.get("columnName").toString().trim();

            //collect columns for every table
            Set<String> cs = new HashSet<String>();
            cs.add("user_id");//for the tableName to join with user_user
            if (tableColumnsMap.containsKey(tableName)) {
                cs = tableColumnsMap.get(tableName);
            }
            cs.add(columnName);
            tableColumnsMap.put(tableName, cs);

//            System.out.println("value1 is : " + value1 + " ,value2 is : " + value2
//                    + " ,operator is : " + operator + " ,itemDefType is " + itemDefType
//                    + " ,tableName is " + tableName + " ,columnName is " + columnName);

            //combine where clause
            columnName = tableName + "." + columnName;

            Calendar calendar = Calendar.getInstance();
            if ("GT".equals(operator)) {//大于
                whereClause = whereClause.append(" and " + columnName + ">" + "\'" + value1 + "\'");
            } else if ("LT".equals(operator)) {//小于
                whereClause = whereClause.append(" and " + columnName + "<" + "\'" + value1 + "\'");
            } else if ("NGT".equals(operator)) {//不大于
                whereClause = whereClause.append(" and " + columnName + "<=" + "\'" + value1 + "\'");
            } else if ("NLT".equals(operator)) {//不小于
                whereClause = whereClause.append(" and " + columnName + ">=" + "\'" + value1 + "\'");
            } else if ("BT".equals(operator)) {//介于
                whereClause = whereClause.append(" and " + columnName + ">" + "\'" + value1 + "\'" + " and " + columnName + "<" + "\'" + value2 + "\'");
            } else if ("NBT".equals(operator)) {//不介于
                whereClause = whereClause.append("and( " + columnName + "<" + "\'" + value1 + "\'" + " or " + columnName + ">" + "\'" + value2 + "\'" + ")");
            } else if ("GTS".equals(operator) || "LTS".equals(operator) || "DEQ".equals(operator) || "GTD".equals(operator)) {
                Integer v1 = new Integer(value1);
                if ("d".equals(value2)) {
                    v1 = v1;
                } else if ("w".equals(value2)) {
                    v1 = v1 * 7;
                } else if ("m".equals(value2)) {
                    v1 = v1 * 30;
                } else if ("y".equals(value2)) {
                    v1 = v1 * 365;
                }
                calendar.setTime(new Date());
                if ("GTS".equals(operator)) {//时间点至运行时间之间(最近时间)
                    String endTime = sdf.format(calendar.getTime());
                    calendar.set(Calendar.HOUR, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.add(Calendar.DAY_OF_MONTH, 1 - v1);
                    String beginTime = sdf.format(calendar.getTime());
                    whereClause.append(" and " + columnName + "<" + "\'" + endTime + "\'" + " and " + columnName + ">" + "\'" + beginTime + "\'");
                } else if ("LTS".equals(operator)) {//比运行时间小某段时间之前(最近时间之前）
                    String endTime = sdf.format(calendar.getTime());
                    calendar.set(Calendar.HOUR, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.add(Calendar.DAY_OF_MONTH, 1 - v1);
                    String beginTime = sdf.format(calendar.getTime());
                    whereClause.append(" and " + columnName + "<" + "\'" + beginTime + "\'");
                } else if ("DEQ".equals(operator)) {//（日期型）等于
                    calendar.set(Calendar.HOUR, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.add(Calendar.DAY_OF_MONTH, v1 - 1);
                    String beginTime = sdf.format(calendar.getTime());
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    String endTime = sdf.format(calendar.getTime());
                    whereClause.append(" and " + columnName + "<" + "\'" + endTime + "\'" + " and " + columnName + ">" + "\'" + beginTime + "\'");
                } else if ("GTD".equals(operator)) {//最近时间之后
                    calendar.set(Calendar.HOUR, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.add(Calendar.DAY_OF_MONTH, v1 + 1);
                    String beginTime = sdf.format(calendar.getTime());
                    whereClause.append(" and " + columnName + ">" + "\'" + beginTime + "\'");
                }
            } else if ("EQ".equals(operator)) {//（数字型）等于
                whereClause.append(" and " + columnName + "=" + value1);
            } else if ("NEQ".equals(operator)) {//（数字型）不等于
                whereClause.append(" and " + columnName + "!=" + value1);
            } else if ("NULL".equals(operator)) {
                whereClause.append(" and " + columnName + " is null");
            } else if ("NOTNULL".equals(operator)) {
                whereClause.append(" and " + columnName + " is not null");
            } else if ("IN".equals(operator) || "NOTIN".equals(operator)) {
                StringBuffer inBuffer = new StringBuffer();
                value1 = value1.trim();
                String[] inArray = value1.split("\\,");
                for (int i = 0; i < inArray.length; i++) {
                    if (!inArray[i].matches("^\\'\\w+\\'$|^\\\"\\w+\\\"$")) {   //eg.  convert "a,b,c" to "'a','b','c'"
                        if (i == inArray.length - 1) {
                            inBuffer.append("\'" + inArray[i] + "\'");
                        } else {
                            inBuffer.append("\'" + inArray[i] + "\'" + ",");
                        }
                    }
                }
                if ("IN".equals(operator)) {
                    whereClause.append(" and " + columnName + " in (" + inBuffer.toString() + ")");
                } else if ("NOTIN".equals(operator)) {
                    whereClause.append(" and " + columnName + " not in (" + value1 + ")");
                }
            } else {
                //TODO

            }
            //save all table name
            set.add(tableName);
        }


        int i = 0;
        for (String a : set) {
            if (!a.equalsIgnoreCase("user_user")) {
                whereClause.append(" and user_user.user_id = " + a + ".user_id");
                if (set.size() == 1 || i == set.size() - 1) {
                    targetTables.append(a);
                    break;
                } else {
                    targetTables.append(a).append(",");
                }
            }
            i++;
        }

        String querySql = "select user_user.user_id from user_user," + targetTables + whereClause;

//        JSONObject tableColumnJSON = JSONObject.fromObject(tableColumnsMap);
//        String tableColumnJsonString = tableColumnJSON.toString();

        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("querySql", querySql);
        resultMap.put("tableColumnsMap", tableColumnsMap);
        System.out.println("finally,query sql is : " + querySql);

        return resultMap;
    }


    //解析出字符串中所有的 <"表名/别名"."列名">
    private static Set<String> getColumns(String targetString) {
        Set<String> columnSet = new HashSet<String>();
        targetString = targetString.replaceAll(",|\\)|\\(", " ");
        String[] arr = targetString.split("\\s+");
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].contains(".")) {
                columnSet.add(arr[i].trim());
            }
        }
        return columnSet;
    }
}
