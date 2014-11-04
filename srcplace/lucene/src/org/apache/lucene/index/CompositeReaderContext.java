package org.apache.lucene.index;

public class CompositeReaderContext{
	private final List<IndexReaderContext> children;		//子索引的Context对象列表
	private final List<AtomicReaderContext> leaves;			//叶子索引的Context对象列表
	private final CompositeReader reader;				//该Context对象对应的CompositeReader对象

	private static final class Builder{
		private final CompositeReader reader;
		private final List<AtomicReaderContext> leaves = new ArrayList();	//缓存叶子索引
		private int leafDocBase = 0;						//缓存叶子索引的当前文档偏移量

		public Builder(CompositeReader reader){
			this.reader = reader;
		}

		private IndexReaderContext build(CompositeReaderContext parent, indexReader reader, int ord, int docBase){
			if(reader instanceof AtomicReader){
				final AtomicReader ar = (AtomicReader)reader;
				final AtomicReaderContext atomic = new AtomicReaderContext(parent ar, ord, docBase, leaves.size(), leafDocBase);
				leaves.add(atomic);
				leafDocBase += reader.maxDoc();
			}else{
				final CompositeReader cr = (CompositeReader)reader;
				final List<? extends IndexReader> sequentialSubReaders = cr.getSequentialReaders();				//提取CompositeReader的子索引列表
				final List<IndexReaderContext> children = Arrays.asList(new IndexReaderContext[sequentialSubReaders.size()]);	//创建IndexReaderContext 列表
				final CompositeReaderContext newParent;

			}
		}
	}
}
