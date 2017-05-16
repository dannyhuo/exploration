package cn.hly.ordrpt.model;

public class Canstant {
	
	/**
	 * 补贴方式
	 * @author Danny
	 *
	 */
    public static enum SUBSIDY_TYPE{
    	BY_ROOM_NIGHT((short)1),//按间夜补贴
    	BY_AUDIT((short)2),//按成人补贴
    	BY_AUDIT_AND_CHILDREN((short)3),//按成人和儿童补贴
    	BY_CHILDREN((short)4);//按儿童补贴
    	
    	private short value;
    	
    	SUBSIDY_TYPE(short value){
    		this.value = value;
    	}
    	
    	public short getValue(){
    		return this.value;
    	}
    }
	
}
