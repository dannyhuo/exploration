package cn.hly.ordrpt.model.meta;

public class CellMeta<T, D> {
	
	public CellMeta() {
		
	}
	
	public CellMeta(T index){
		this.index = index;
	}
	
	public CellMeta(T index, D data){
		this.index = index;
		this.data = data;
	}
	
	public T index;
	
	public D data;

}
