package org.apache.lucene.search.similarities;

public abstract class Similarity{
	public Similarity(){}

	/**计算协调因子*/
	public float coord(int overlap, int maxOverlap){
		return 1f;
	}

	/**计算query的查询因子
	*@param valueForNormalization 为query中各term的boost值之和
	*/
	public float queryNorm(float valueForNormalization){
		return 1f;
	}

	/**计算给定域的标准化因子*/
	public abstract long computeNorm(FieldInvertState state);

	/**计算集合级别 的相似度*/
	public abstract SimWeight computeWeight(float queryBoost, CollectionStatics collectionStats, TermStatics... ternStats);

	public abstract SimScore simScore(SimWeight weight, AtomicReaderContext context);

	public static abstract class SimScorer{
		
	}

	
}
