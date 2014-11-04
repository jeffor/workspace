package org.apache.lucene.search.similarities;

public abstract class TFIDFSimilarity{

	public TFIDFSimilarity(){}

	public abstract float coord(int overlap, int maxOverlap);
	public abstract float queryNorm(float sumOfSquaredWeights);
	public abstract float tf(float freq);

}
