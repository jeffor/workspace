package org.apache.lucene.search;

public abstract class DocIdSetIterator{

	public static final int NO_MORE_DOCS = Integer.MAX_VALUE;

	public abstract int docID();		//返回当前docID

	public abstract int nextDoc() throws IOException;	//迭代至下一个DOC并返回其ID

	public abstract int advance(int target) throws IOException;	//定位到target处的doc

	public final int slowAdvance(int target){
		int doc;
		do{
			doc = nextDoc()
		}while(doc < target);
		return doc;
	}

	public abstract long cost();
}
