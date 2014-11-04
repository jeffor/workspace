package org.apache.lucene.index;

public abstract class AtomicReader{
	private final AtomicReaderContext readerContext = new AtomicReaderContext(this);

	protected AtomicReader(){
		super();
	}

	@Override
	public final AtomicReaderContext getContext(){
		ensureOpen();
		return readerContext;
	}

	/**判断对应雨中存在标准化因子*/
	public final boolean hasNorms(String field) throws IOException{
		ensureOpen();
		FieldInfo fi = getFieldInfos().fieldInfo(field);
		return fi != null && fi.hasNorms();
	}

	/**监控器接口*/
	public static interface CoreClosedListener{
		public void onClose(Object ownerCoreCacheKey);
	}

	private static class CoreClosedListenerWrapper implements ReaderClosedListener{
		private final CoreClosedListener listener;

		CoreClosedListenerWrapper(CoreClosedListener listener){
			this.listener = listener;
		}

		@Override
		public void onClose(IndexReader reader){
			listener.onClose(reader.getCoreCacheKey());
		}
	}

	/**为一个索引添加监控器*/
	protected static void addCoreClosedListener(IndexReader reader, CoreClosedListener listener){
		reader.addReaderClosedListener(new CoreClosedListenerWrapper(listener));
	}

	/**为一个索引删除监控器*/
	protected static void removeCoreClosedListener(IndexReader reader, CoreClosedListener listener){
		reader.removeReaderClosedListener(new CoreClosedListenerWrapper(listener));
	}

	/**为自身添加监控器*/
	public abstract void addCoreClosedListener(CoreClosedListener listener);

	/**为自身删除监控器*/
	public abstract void removeReaderClosedListener(CoreClosedListener listener);

	/**获得该索引的域信息*/
	public abstract Fields fields() throws IOException;

	@Override
	public int docFreq(Term term){
		
	}

	
}
