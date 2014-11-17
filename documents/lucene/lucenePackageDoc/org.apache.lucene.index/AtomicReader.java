package org.lucene.apache.index;

public abstract class AtomicReader{
	private AtomicReaderContext readerContext = new AtomicReaderContext(this);

	protected AtomicReader{
		super();
	}

	@Override
	public AtomicReaderContext getContext(){
		ensureOpen();
		return readerContext;
	}

	@Override
	public final boolean hasNorms(String field) throws IOException{
		ensureOpen();
		FieldInfo info = getFieldInfos().fieldInfo(field);
		return info != null && info.hasNorms();
	}

	public static interface CoreClosedListener{
		public void onClose(Object owenerCoreCacheKey);
	}

	private static class CoreClosedListenerWrapper implements ReaderClosedListener{
		private CoreClosedListener listener;

		CoreClosedListenerWrapper(CoreClosedListener listener){
			this.listener = listener;
		}

		@Override
		public void onClose(IndexReader reader){
			listener.onClose(reader.getCoreCacheKey());
		}

		@Override
		public int hashCode(){
			return listener.hashCode();
		}

		@Override
		public boolean equals(Object obj){
			if(!(obj instanceof CoreClosedListenerWrapper))
				return false;
			return listene.equals((CoreClosedListenerWrapper)obj.listener);
		}
	}

	protected static void addCoreClosedListenerAsReaderClosedListener(IndexReader reader, CoreClosedListener core){
		reader.addReaderClosedListener(new CoreClosedListenerWrapper(core));
	}

	protected static void removeCoreClosedListenerAsReaderClosedListener(IndexReader reader, CoreClosedListener core){
		reader.removeReaderClosedListener(new CoreClosedListenerWrapper(core));
	}

	public abstract void addCoreClosedListener(CoreClosedListener listener);
	public abstract void removeCoreClosedListener(CoreClosedListener listener);

	public abstract Fields fields() throws IOException;
}
