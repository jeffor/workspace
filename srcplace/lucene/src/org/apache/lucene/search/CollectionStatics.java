package org.apache.lucene.search;

/**域的统计信息类*/
public class CollectionStatics{
	private final String field;		//域名
	private final long maxDoc;		//最大文档数目
	private final long docCount;		//包含该域的文档数目
	private final long sumTotalTermFreq;	//该域的termFreq总和
	private final long sumDocFreq;		//该域的DocFreq总和

	public CollectionStatics(String field, long maxDoc, long docCount, long sumTotalTermFreq, long sumDocFreq){
		this.field = field;
		this.maxDoc = maxDoc;
		this.docCount = docCount;
		this.sumTotalTermFreq = sumTotalTermFreq;
		this.sumDocFreq = sumDocFreq;
	}

	public final String field(){
		return field;
	}

	public final long maxDoc(){
		return maxDoc;
	}

	public final long docCount(){
		return docCount;
	}

	public final long sumTotalTermFreq(){
		return sumTotalTermFreq;
	}

	public final long sumDocFreq(){
		return sumDocFreq;
	}
}
