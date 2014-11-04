public abstract class CompositeReader extends IndexReader

####################################
	数据域解析
####################################

provite volatile CompositeReaderContext = null;			//CompositeReader 的Context对象

####################################
	方法解析
####################################

protected abstract List<? extends IndexReader> getSequentialSubReaders();		//返回当前索引的子索引列表
public final CompositeReaderContext getContext();					//获得当前索引的Context对象
