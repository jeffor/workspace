package org.apache.lucene.search.similarities;

public abstract class TFIDFSimilarity{

	public TFIDFSimilarity(){}

	public abstract float coord(int overlap, int maxOverlap);
	public abstract float queryNorm(float sumOfSquaredWeights);
	public abstract float tf(float freq);
	public abstract float idf(long docFreq, long numDocs);
	public abstract float lengthNorm(FieldInvertState state);	//通过域的状态信息查询标准化因子

	public abstract long encodeNormValue(float norm);
	public abstract long decodeNormValue(long norm);

	public abstract float sloppyFreq(int distance);
	public abstract float scorePayload(int docID, int staet, int end, BytesRef payload);

	/**计算对应term的IDF*/
	public Explanation idfExplain(CollectionStatics collectionstats, TermStatics termStats){
		final long df = termStats.docFreq();
		final long max = collectionstats.maxDoc();
		final float idf = idf(df, max);
		return new Explanation(idf, "idf(docFreq="+df+",maxDocs="+max+")");
	}

	/**计算term列表的idf和*/
	public Explanation idfExplain(CollectionStatics collectionstats, TermStatics termStats[]){
		final long max = collectionstats.maxDoc();
		float idf = 0f;
		final Explanation exp = new Explanation();
		exp.setDescription("idf(),sum of:");
		for(final TermStatics stat: termStats){
			final long df = stat.docFreq();
			final float termIdf = idf(df, max);
			exp.addDetail(new Explanation(termIdf, "idf(docFreq="+df+",maxDocs="+max+")"));
			idf += termIdf;
		}
		exp.setValue(idf);
		return exp;
	}

	/**计算长整型形式的norm信息*/
	public final long computeNorm(FieldInvertState state){
		float norm = lengthNorm(state);
		return encodeNormValue(norm);
	}

	

	

	

}
