package org.apache.lucene.index;

public abstract class IndexReaderContext{
	
	public final CompositeReaderContext parent;		//该索引直接父对象的Context对象

	public final boolean isTopLevel;			//是否为顶层索引
	public final int docBaseInParent;			//在父索引中的偏移量
	public final int ordInParent;				//在父索引中的下标

	IndexReaderContext(CompositeReaderContext parent, int docBaseInParent, int ordInParent){
		if(!(this instanceof AtomicReaderContext || this instanceof CompositeReaderContext))
			throw new Error("this should never be extended by custom code");
		this.parent = parent;
		this.docBaseInParent = docBaseInParent;
		this.ordInParent = ordInParent;
		this.isTopLevel = parent == null;
	}

	/**返回该Context对象对应的IndexReader对象*/
	public abstract IndexReader reader();

	/**返回叶子索引的Context列表*/
	public abstract List<AtomicReaderContext> leaves() throws UnsupportedOperationException;

	/**返回子索引的Context对象列表*/
	public abstract List<IndexReaderContext> children(); 
}
