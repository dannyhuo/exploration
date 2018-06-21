package com.lvmama.crm.up.enumerate;

/**
 * Created by huoqiang on 18/7/2017.
 */
/**
 * 模型任务枚举
 * @author huoqiang
 *
 */
public class UpModelJobEnum {
    /**
     * 模型任务执行状态
     * 1：正常状态
     * 2：等待状态
     * 3：任务已提交状态
     * 4：运行状态
     * @author huoqiang
     *
     */
    public static enum EXE_STATUS {
        running((short) 1, "运行状态"),
        submitted((short) 2, "已发送运行指令"),
        waiting((short) 3, "等待发送运行指令"),
        normal((short) 4, "正常状态");

        private Short code;
        private String cnName;

        EXE_STATUS(Short code, String cnName){
            this.code = code;
            this.cnName = cnName;
        }

        public Short getCode(){
            return this.code;
        }

        /**
         * 根据值获取枚举名称
         * @param code
         * @return
         */
        public static String getName(Short code){
            for (EXE_STATUS status : EXE_STATUS.values()) {
                if(status.code == code){
                    return status.name();
                }
            }
            return null;
        }

        /**
         * 根据值获取枚举名称
         * @param code
         * @return
         */
        public static String getCnName(Short code){
            for (EXE_STATUS status : EXE_STATUS.values()) {
                if(status.code == code){
                    return status.cnName;
                }
            }
            return null;
        }

        /**
         * 根据名称，获取code
         * @param name
         * @return
         */
        public static Short getCode(String name){
            for (EXE_STATUS status : EXE_STATUS.values()) {
                if(status.name().equals(name)){
                    return status.code;
                }
            }
            return null;
        }

        public String toString(){
            return this.name();
        }
    }

    /**
     * 模型任务执行状态
     * 1：进入队列
     * 2：任务已提交
     * 3：任务执行中
     * 4：任务执行成功
     * 5：任务执行失败
     * 6：任务手动停止
     * 7：异常自动停止
     * @author huoqiang
     *
     */
    public static enum EXE_EVENT {
        enqueue((short) 1, "进入待执行队列"),
        submitted((short) 2, "已发送运行指令"),
        running((short) 3, "任务运行中"),//开始执行事件
        runSucessful((short) 4, "任务运行成功"),//执行成功事件
        runFailed((short) 5, "任务运行失败"),//执行失败事件
        manualStop((short) 6, "手动停止"),//手动停止事件
        emergencyStop((short) 7, "异常自动停止");//异常自动停止

        private Short code;
        private String cnName;

        EXE_EVENT(Short code, String cnName){
            this.code = code;
            this.cnName = cnName;
        }

        public Short getCode(){
            return this.code;
        }

        /**
         * 根据值获取枚举名称
         * @param code
         * @return
         */
        public static String getName(Short code){
            for (EXE_EVENT status : EXE_EVENT.values()) {
                if(status.code == code){
                    return status.name();
                }
            }
            return null;
        }

        /**
         * 根据值获取枚举中文名称
         * @param code
         * @return
         */
        public static String getCnName(Short code){
            for (EXE_EVENT status : EXE_EVENT.values()) {
                if(status.code == code){
                    return status.cnName;
                }
            }
            return null;
        }

        /**
         * 根据名称，获取code
         * @param name
         * @return
         */
        public static Short getCode(String name){
            for (EXE_EVENT status : EXE_EVENT.values()) {
                if(status.name().equals(name)){
                    return status.code;
                }
            }
            return null;
        }

        public String toString(){
            return this.name();
        }
    }

    /**
     * 模型任务执行结果状态
     * 0：执行失败
     * 1：执行成功
     * @author huoqiang
     *
     */
    public static enum EXE_RESULT {
        successful((short) 1, "运行成功"),
        failed((short) 0, "运行失败"),
        manualStop((short)2, "手动停止");

        private Short code;
        private String cnName;

        EXE_RESULT(Short code, String cnName){
            this.code = code;
            this.cnName = cnName;
        }

        public Short getCode(){
            return this.code;
        }

        /**
         * 根据值获取枚举名称
         * @param code
         * @return
         */
        public static String getName(Short code){
            for (EXE_RESULT status : EXE_RESULT.values()) {
                if(status.code.equals(code)){
                    return status.name();
                }
            }
            return null;
        }

        /**
         * 根据值获取枚举名称
         * @param code
         * @return
         */
        public static String getCnName(Short code){
            for (EXE_RESULT status : EXE_RESULT.values()) {
                if(status.code.equals(code)){
                    return status.cnName;
                }
            }
            return null;
        }

        /**
         * 根据名称，获取code
         * @param name
         * @return
         */
        public static Short getCode(String name){
            for (EXE_RESULT status : EXE_RESULT.values()) {
                if(status.name().equals(name)){
                    return status.code;
                }
            }
            return null;
        }

        public String toString(){
            return this.name();
        }
    }

    /**
     * ES索引是否删除
     * 0：未删除
     * 1：删除
     *
     * @author jiangdanwei
     */
    public static enum IS_ES_REMOVE {
        NOT_REMOVED((short) 0, "未删除"),
        REMOVED((short) 1, "删除");

        private short key;
        private String cnName;

        IS_ES_REMOVE(short key, String name) {
            this.key = key;
            this.cnName = name;
        }

        public String getCode() {
            return this.name();
        }

        public String getCnName() {
            return this.cnName;
        }

        public short getKey() {
            return this.key;
        }

        public String toString() {
            return this.name();
        }
    }
}
