package org.apache.lucene.index;

/**索引读取抽象类*/
public abstract class IndexReader{
	private boolean closed = false;					//索引关闭标识
	private boolean closedByChild = false;				
	private final AtomicInteger refCount = new AtomicInteger();	//索引引用数(线程同步类型)

	/**读取类构造器, 必须为CompositeReader 或者 AtomicReader 对象*/
	IndexReader(){
		if(!(this instanceof CompositeReader || this instanceof AtomicReader))
			throw new Error("IndexReader should never be directory extended, subclass AtomicReader or CompositeReader instead");
	}

	/**读取类关闭监控 接口*/
	public static interface ReaderClosedListener{
		/**在IndexReader已经关闭时调用, 通知监控器该对象已经关闭*/
		public void onClose(IndexReader reader);
	}

	//监控对象集合
	private final Set<ReaderClosedListener> readerClosedListeners = 
		Collections.synchronizedSet(new LinkedHashSet<ReaderClosedListener>());

	//索引读取对象 集合
	private final Set<IndexReader> parentReaders = 
		Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<IndexReader, Boolean>()));

	/**添加一个监控对象*/
	public final void addReaderClosedListener(ReaderClosedListener listener){
		ensureOpen();
		readerClosedListeners.add(listener);
	}

	/**移除一个监控对象*/
	public final void removeReaderClosedListener(ReaderClosedListener listener){
		ensureOpen();
		readerClosedListeners.remove(listener);
	}

	/**添加索引读对象*/
	public final void registerParentReader(IndexReader reader){
		ensureOpen();
		parentReaders.add(reader);
	}

	/**通知监控对象 this 已经关闭*/	
	private void notifyReaderClosedListener(Throwable th){
		synchronized(readerClosedListeners){
			for(ReaderClosedListener listener: readerClosedListeners){
				try{
					listener.onClose();
				}catch (Throwable t){
					if(th == null)
						th = t;
					else
						th.addSuppressed(t);
				}
			}
			IOUtils.reThrowUnchecked(th);
		}
	}

	/**通知父读取对象集合，子对象已经关闭*/
	private void reportCloseToParentReaders(){
		synchronized(parentReaders){
			for(IndexReader parent: parentReaders){
				parent.closedByChild = true;
				parent.refCount.addAndGet(0);
				parent.reportCloseToParentReaders();
			}
		}
	}

	/**返回父引用计数*/
	public final int getRefCount(){
		return refCount.get();
	}

	/**增加父引用计数*/
	public final void incRef(){
		if(!tryIncRef()){
			ensureOpen();
		}
	}

	/**增加引用计数*/
	public final boolean tryIncRef(){
		int count;
		while((count = refCount.get()) > 0){
			if(refCount.compareAndSet(count, count+1))
				return true;
		}
		return false;
	}

	/**减少引用计数*/
	public final void decRef() throws IOException{
		if(refCount.get() <= 0)
			throw new AlreadyClosedException("this indexer is closed");

		final int rc = refCount.decrementAndGet();
		if(rc == 0){
			closed = true;
			Throwable throwable = null;
			try{
				doClose();
			} catch (Throwable th){
				throwable = th;
			} finally{
				notifyReaderClosedListener(throable);
			}
		} else if(rc < 0){
			throw new IllegalStateExceprion("too many decRef calls: refCount is " +rc+ "after decment");
		}
	}

	/**确保该对象打开，否则抛出异常*/
	protected final void ensureOpen() throws AlreadyClosedException{
		if(refCount.get() <= 0){
			throw new AlreadyClosedException("this IndexReader is closed");
		}

		if(closedByChild){
			throw new AlreadyClosedException("this IndexReader cannot be used anymore as one of its child readers was closed");
		}
	}

	public final boolean equals(Object obj){
		return this == obj;
	}

	public final int hashCode(){
		return System.identifyHashCode(this);
	}

	public static DirectoryReader open(final Directory directory) throws IOException{
		return DirectoryReader.open(directory);
	}

	public static DirectoryReader open(final Directory directory, int termInfoIndexDivisor) throws IOException{
		return DirectoryReader.open(directory, termInfoIndexDivisor);
	}

	/**根据文档ID获得域向量*/
	public abstract Fields getTermVector(int docID) throws IOException;

	/**更具文档ID 和 域名称 获得 词向量*/
	public final Terms getTermVector(int docID, String field) throws IOException{
		Fields vectors = getTermVector(docID);
		if(vectors == null)
			return null;
		return vectors.terms(field);
	}

	/**该索引的有效文档数*/
	public abstract int numDocs();

	/**该索引的文档总数*/
	public abstract int maxDoc();

	/**该索引的删除文档数*/
	public final int numDeletedDocs(){
		return maxDoc() - numDocs();
	}

	/**遍历文档,并将数据保存在visitor遍历对象中*/
	public abstract void document(int docID, StoredFieldVisitor visitor) throws IOException;

	/**遍历文档，返回文档对象*/
	public final Document document(int docID) throws IOException{
		final DocumentStoredVisitor visitor = new DocumentStoredVisitor();
		document(docID, visitor);
		return visitor.getDocument();
	}

	/**遍历文档，返回文档对象*/
	public final Document document(int docID, Set<String> fieldsToLoad) throws IOException{
		final DocumentStoredVisitor visitor = new DocumentStoredVisitor(fieldsToLoad);
		document(docID, visitor);
		return visitor.getDocument();
	}

	/**判断是否存在删除文档*/
	public boolean hasDeletions(){
		return numDeletedDocs() > 0;
	}

	/**关闭当前索引*/
	public final synchronized void close() throws IOException{
		if(!closed){
			decRef();
			closed = true;
		}
	}

	/**关闭索引对象*/	
	public abstract void doClose() throws IOException;

	/**获得索引对象的环境*/
	public abstract IndexReaderContext getContext();

	/**获得叶子对象*/
	public final List<AtomicReaderContext> leaves(){
		return getContext().leaves();
	}

	/**获得this的key*/
	public Object getCoreCacheKey(){
		return this;
	}

	/**获得this的key*/
	public Object getCombinedCoreAndDeletesKey(){
		return this;
	}

	/**获得索引中包含term的文档总数*/
	public abstract int docFreq(Term term) throws IOException;

	/**获得索引中term出现的总数*/
	public abstract long totalTermFreq(Term term) throws IOException;

	
	public abstract long getSumDocFreq(String field) throws IOException;

	public abstract int getDocCounts(String field) throws IOException;

	public abstract long getSumTotalTermFreq(String field) throws IOException;
}
