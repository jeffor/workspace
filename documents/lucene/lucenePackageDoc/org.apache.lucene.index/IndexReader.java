package org.apache.lucene.index;

public abstract class IndexReader implements Closeable{

	private boolean closed = false;		//关闭标识

	private boolean closeByChild = false;	//子索引关闭标识

	private final AtomicInteger refCount = new AtomicInteger(1);	//当前索引引用计数

	IndexReader(){
		if(!(this instanceof CompositeReader || this instanceof AtomicReader))
			throw new Error("IndexReader should never be directly extended");
	}

	/**客户端的监听器,当当前索引关闭时将触发onClose()*/
	public static interface ReaderClosedListener{
		public void onClose(IndexReader reader);
	}

	/**创建监听器集合*/
	private final Set<ReaderClosedListener> readerClosedListeners = 
		Collections.synchronizedSet(new LinkedHashSet<ReaderClosedListener>());

	/**父索引集合*/
	private final Set<IndexReader> parentReaders = 
		Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap(IndexReader, Boolean)()));

	public final void addReaderClosedListener(ReaderClosedListener listener){
		ensureOpen();
		readerClosedlisteners.add(listener);
	}

	public final void removeReaderClosedListener(ReaderClosedListener listener){
		ensureOpen();
		readerClosedlisteners.remove(listener);
	}

	public void registerParentReader(IndexReader reader){
		ensureOpen();
		parentReaders.add(reader);
	}

	/**通知监听器*/
	private void notifyReaderClosedListeners(Throwable th){
		synchronized(readerClosedListeners){
			for(ReaderClosedListener listener: readerClosedlisteners){
				try{
					listener.onclose(this);
				}catch (Throwable t){
					if(th == null)
						th = t;
					else
						th.addSuppressed(t)
				}
			}
			IOUtils.reThrowUnchecked(th);
		}
	}

	private void reportCloseToParentReaders(){
		synchronized(parentReaders){
			for(IndexReader parent: parentReaders){
				parent.closeByChild = true;
				parent.refCount.addAndGet(0);
				parent.reportCloseToParentReaders();
			}
		}
	}

	public final int getRefCount(){
		return refCount;
	}

	public final void incRef(){
		if(!tryIncRef())	//引用增加失败是由于当前索引已经关闭
			ensureOpen();	//抛出异常
	}

	/**同步状态下的加一操作*/
	public final boolean tryIncRef(){
		int count;
		while((count = refCount.get()) > 0){
			if(refCount.compareAndSet(count, count+1))
				return true;
		}
		return false;
	}

	public final void decRef() throws IOException{
		if(refCount.get() <= 0)
			throw new AlreadyClosedException("this indexReader is closed");

		final int rc = refCount.decrementAndGet();
		if(rc == 0){
			closed = true;
			Throwable throwable = null;
			try{
				doClose();
			}catch (Throwable th){
				throwable = th;
			}finally {
				try{
					reportCloseToParentReaders();
				}finally{
					notifyReaderClosedListeners(throwable);
				}
			}
		}else if(rc < 0)
			throw new IllegalStateException("too many decRef calls");
	}

	public final void ensureOpen(){
		if (refCount <= 0)
			throw new AlreadyClosedException("this index is closed");
		if (closedByChild = true)
			throw new AlreadyClosedException("this index cannot be used any more");
	}

	@Override
	public final boolean equals(Object obj){
		return (this == obj);
	}

	@Override
	public final int hashCode(){
		System.identityHashCode(this);
	}

	###    对索引中文档的操作    ###

	/**返回一篇文档所有索引field的term向量(二维)*/
	public abstract Fields getTermVectors(int docID)
		throw IOException;

	public final Terms getTermVectors(int docID, String field){
		Fields vectors = getTermVectors(docID);
		if(vectors == null)
			return null;
		return vectors.terms(field);
	}

	public abstract int numDoc();

	public abstract int maxDoc();

	public final int numDeletedDocs(){
		return maxDoc() - numDocs();
	}

	/**遍历文档*/
	public abstract void document(int docID, StoredFieldVisitor visitor) throws IOException;

	public final Document document(int docID) throws IOException{
		final DocumentStoredFieldVisitor visitor = new DocumentStoredFieldVisitor();
		document(docID, visitor);
		return visitor.getDocument();
	}

	public final Document document(int docID, Set<String> fieldsToLoad) throws IOException{
		final DocumentStoredFieldVisitor visitor = new DocumentStoredFieldVisitor(fieldsToLoad);
		document(docID, visitor);
		return visitor.getDocCount();
	}

	public final boolean hasDeletions(){
		return numDeletedDocs() > 0;
	}

	@Override
	public final synchronized void close() throws IOException{
		if(!closed){
			decRef();
			closed = true;
		}
	}

	protected abstract void doClose() throws IOException;

	public abstract IndexReaderContext getContext();

	public final List<AtomicReaderContext> leaves(){
		return getContext().leaves();
	}

	public Object getCoreCachekey(){
		return this;
	}

	public Object getCombinedCoreAndDeletesKey(){
		return this;
	}

	public abstract int docFreq(Term term) throws IOException;		//包含该term的文档频率
	public abstract int totalTermFreq(Term term) throws IOException;	//该term的词频

	public abstract int getSumSocFreq(Field field) throws IOException;	//对应域中所有term的docFreq之和
	public abstract int getSumTotalTermFreq(Field field) throws IOException;//对应域中所有term的totalTermFreq之和

	public abstract int getDocCount(Field field) throws IOException;	//对应field中包含term的文档总数
	
}
