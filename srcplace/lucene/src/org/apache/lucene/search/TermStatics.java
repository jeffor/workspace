package org.apache.lucene.search;


/**词的统计类*/
public class TermStatics{
	private final BytesRef term;		//字节形式的词语信息
	private final long docFreq;		//包含term的文档总数
	private final long totalTermFreq;	//term在索引中出现的总数

	public TermStatics(BytesRef term, long docFreq, long totalTermFreq){
		this.term = term;
		this.docFreq = docFreq;
		this.totalTermFreq = totalTermFreq;
	}

	public final BytesRef term(){
		return term;
	}

	public final long docFreq(){
		return docFreq;
	}

	public final long totalTermFreq(){
		return totalTermFreq;
	}
}
