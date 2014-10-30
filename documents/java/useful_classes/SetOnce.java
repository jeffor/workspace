public final class SetOnce<T> implements Cloneable{
	T obj = null;				//缓存对象引用
	AtomicBoolean isSet;			//标识对象是否已经被设置

	/**默认构造器不设置缓存对象,故只标识 isSet = false*/
	public SetOnce(){
		isSet = new AtomicBoolean();	//isSet = false;
	}

	/**带参构造器,设置缓存对象,并标识 isSet = true*/
	public SetOnce(T obj){
		this.obj = obj;
		this.isSet = new AtomicBoolean(true);
	}

	/**设置缓存对象,限制二次设置*/
	public void set(T obj){
		if(isSet.compareAndSet(false, true)){
			this.obj = obj;
		}
		else
			throw new AlreadlySetException();
	}

	/**返回缓存对象的引用*/
	public T get(){
		return obj;
	}

	public static AlreadlySetException extends IllegalStateException{
		public AlreadlySetException{
			super("this object cannot be set twice");
		}
	}
}
