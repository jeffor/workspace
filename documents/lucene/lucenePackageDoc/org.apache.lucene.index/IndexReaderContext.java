package org.apache.lucene.index;

/**用于表示 IndexReader 实例 之间的层级关系*/
public abstract class IndexReaderContext{
	public final CompositeReaderContext parent;

	public final boolean isTopLevel;

	public final int docBaseInParent;	//该索引的起始文档偏移量

	public final int ordInParent;		//该文档的位置

	IndexReaderContext(CompositeReaderContext parent, int ordInParent, int docBaseInParent) {
		if (!(this instanceof CompositeReaderContext || this instanceof AtomicReaderContext))
			throw new Error("This class should never be extended by custom code!");
		this.parent = parent;
		this.docBaseInParent = docBaseInParent;
		this.ordInParent = ordInParent;
		this.isTopLevel = parent==null;
	}

	public abstract IndexReader reader();		//获得对应的 IndexReader;

	public abstract List<AtomicReaderContext> leaves() throws UnsupportedOperationException;

	public abstract List<IndexReaderContext> children();
}
