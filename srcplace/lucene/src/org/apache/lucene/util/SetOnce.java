package org.apache.lucene.util;

public final class SetOnce<T> implements Cloneable{

	public static final AlreadySetException extends IllegalStateException{
		public AlreadySetException(){
			super("The object cannot be set twice");
		}
	}

	private volatile T obj = null;			//缓存目标对象
	private final AtomicBoolean set;		//原则类型的设置标识(boolean)

	public SetOnce(){
		set = new AtomicBoolean(false);
	}

	public SetOnce(T obj){
		this.obj = obj;
		set = new AtomicBoolean(true);
	}

	public void set(T obj){
		if(set.compareAndSet(false, true)){
			this.obj = obj;
		}else{
			throw new AlreadySetException();
		}
	}

	public T get(){
		return obj;
	}
}
