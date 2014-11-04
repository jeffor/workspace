package org.apache.lucene.index;

/**基础的复合索引类（读索引）*/
public abstract class BaseCompositeReader<R extends IndexReader> extends CompositeReader{
	private final R[] subReaders;		//子索引动态数组
	private final int[] starts;		//子索引文档偏移量缓存
	private final int maxDoc;		//最大文档数
	private final int numDocs;		//该索引集合的实际文档总数

	private final List<R> subReaderList;	//子索引列表

	/**使用子索引数组初始化 复合索引类*/
	protected BaseCompositeReader(R[] subReaders){
		this.subReaders = subReaders;
		this.subReaderList = Collections.unmodifiableList(Arrays.asList(subReaders));	//创建只读列表
		starts = new int[subReaders.length + 1];
		int maxDoc = 0, numDocs = 0;
		for (int i = 0; i < subReaders.length; ++i){
			starts[i] = maxDoc;			//设置文档偏移量
			final IndexReader r = subReaders[i];
			maxDoc += r.maxDoc();			//更新文档偏移量
			if (maxDoc < 0 /*整型溢出*/)
				throw new IllehalArgumentException("Too many documents, composite IndexReader cannot exceed " + Integer.MAX_VALUE);
			numDocs += r.numDocs();			//更新文档数
			r.registerParentReader(this);
		}
		starts[subReaders.length] = maxDoc;
		this.maxDoc = maxDoc;
		this.numDocs = numDocs;
	}

	@Override
	public final Fields getTermVectors(int docID) throws IOException{
		ensureOpen();
		final int i = readerIndex(docID);			//定位子索引
		return subReaders[i].getTermVectors(docID = starts[i]);	//获得对应文档的域向量
	}

	@Override
	public final int numDocs(){
		return numDocs;
	}

	@Override
	public final int maxDoc(){
		return maxDoc;
	}

	@Override
	public final void document(int docID, StoredFieldVisitor visitor) throws IOException{
		ensureOpen();
		final int i = readerIndex(docID);
		subReaders[i].document(docID - starts[i], visitor);
	}

	@Override
	public final int docFreq(Term term) throws IOException{
		ensureOpen();
		int total = 0;
		for(int i = 0; i < subReaders.length; ++i){
			total += subReaders[i].docFreq(term);
		}
		return total;
	}

	@Override
	public final int totalTermFreq(Term term) throws IOException{
		ensureOpen();
		int total = 0;
		for(int i = 0; i < subReaders.length; ++i){
			long sub = subReaders[i].totalTermFreq(term);
			if(sub == -1)
				return -1;
			total += sub;
		}
		return total;
	}

	@Override
	public final long getSumDocFreq(String field) throws IOException{
		ensureOpen();
		int total = 0;
		for(R reader: subReaders){
			long sub = reader.getSumDocFreq(field);
			if(sub == -1)
				return -1;
			total += sub;
		}
		return total;
	}

	@Override
	public final long getSumTotalTermFreq(String field) throws IOException{
		ensureOpen();
		int total = 0;
		for(R reader: subReaders){
			long sub = reader.getSumTotalTermFreq(field);
			if(sub == -1)
				return -1;
			total += sub;
		}
		return total;
	}

	@Override
	public final long getDocCount(string field) throws IOException{
		ensureOpen();
		int total = 0;
		for(R reader: subReaders){
			long sub = reader.getDocCount(field);
			if(sub == -1)
				return -1;
			total += sub;
		}
		return total;
	}


	/**定位对应文档ID的子索引编号*/
	protected final int readerIndex(int docID){
		if(docID < 0 || docID >= maxDoc)
			throw new IllehalArgumentException("docID overflow");
		return ReaderUtil.subIndex(docID, this.starts);
	}

	/**返回对应下标子索引的文档偏移起始地址*/
	protected final int readerBase(int readerIndex){
		if(readerIndex < 0 || readerIndex >= subReaders.length)
			throw new IllehalArgumentException("readerIndex overflow");
		return this.starts[readerIndex];
	}

	@Override
	protected final List<? extends R> getSequentialSubReaders(){
		return subReaderList;
	}
}
