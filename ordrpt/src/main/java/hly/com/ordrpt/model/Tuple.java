package hly.com.ordrpt.model;

public class Tuple <T, V>{
	
	public Tuple(){
		
	}
	
	public Tuple(T item1){
		this.item1 = item1;
	}
	
	public Tuple(T item1, V item2){
		this.item1 = item1;
		this.item2 = item2;
	}

	public T item1;
	
	public V item2;
	
	

}
