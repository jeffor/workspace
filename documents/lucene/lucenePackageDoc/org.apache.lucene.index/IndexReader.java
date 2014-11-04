public abstract class IndexReader


#################################
	数据域解析
#################################

private boolean closed = false;					//当前索引关闭标识
private boolean closedByChild = false;				//子索引关闭标识（若子索引已经关闭则该索引也将不能使用）
private AtomicInteger refCount = new AtomicInteger(1);		//该索引的引用计数

private final Set<ReaderClosedListener> readerClosedlisteners =
	Collections.synchronizedSet(new LinkedHashSet<ReaderClosedListener>());			//索引监控器集合
private final Set<IndexReader> parentReaders =
	Collections.synchronizedSet(Collections.newSetFromMap(
		new WeakHashMap<IndexReader, Boolean>()));					//父索引引用集合 

#################################	
	方法解析
#################################

public final void addReaderClosedListener(ReaderClosedListener listener);			//增加一个监控器
public final void removeReaderClosedListener(ReaderClosedListener listener);			//删除一个监控器

public final void registerParentReader(IndexReader reader);					//注册一个父引用

private void notifyReaderClosedListeners(Throwable th);						//提醒监控器该索引已经关闭
private void reportCloseToParentReaders();							//递归提醒父索引，其子索引已经关闭


public final int getRefCount();									//获得当前索引的引用计数
public final void incRef();									//增加引用计数
public final boolean tryIncRef();								//尝试增加引用计数（由于在并发运行中索引随时可能关闭，因此只是尝试增加）
public final void decRef() throws IOException;							//关闭当前索引并通知监控器和父索引

protected final void ensureOpen() throws AlreadyClosedException;				//确保当前索引可用，若当前索引不可用则抛出 关闭异常

public abstract Fields getTermVectors(int docID);						//根据文档ID读取对应文档的域向量
public final Terms getTermVectors(int docID, String field);					//根据文档ID 和 域名 读取对应文档域中的词向量

public abstract int numDocs();									//获得该索引的文档总数
public abstract int maxDoc();									//获取该索引的最大文档数（包含曾删除的文档数）
public final int numDeletedDocs();								//获得该索引删除的文档数
public final boolean hasDeletions();								//判断该索引是否存在被删除的文档

public final synchronized void close() throws IOException;					//关闭当前索引
protected abstract void doClose() throws IOException;						//实现关闭操作

public abstract void document(int docID, StoredFieldVisitor visitor) throws IOException;	//遍历文档
public final Document document(int docID);							//使用默认访问器遍历文档，返回筛选的文档集合
public final Document document(int docID, Set<String> fieldToLoad) throws IOException;		//获得存在指定域集的文档集合

public abstract IndexReaderContext getContext();						//获得当前索引的Context信息
public List<AtomicReaderContext> leaves();							//获得叶子索引的Context信息

public abstract int docFreq(Term term) throws IOException;					//获得当前索引中包含term的文档总数(文档频率)
public abstract long totalTermFreq(Term term) throws IOException;				//获得当前索引中term出现的总数(词频)
public abstract long getSumDocFreq(String field) throws IOException;				//获得当前索引中对应域中所有term的文档总数和(文档频率和)
public abstract long getSumTotalTermFreq(String field) throws IOException;			//获得当前索引中对应域中所有term的出现次数数和(词频和)

public abstract int getDocCount(String field) throws IOException;				//获得当前索引中对应域中至少包含一个term的文档数
