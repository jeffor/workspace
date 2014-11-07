package org.apache.lucene.search.similarities;

public abstract class TFIDFSimiarity{

	public TFIDFSimiarity() {}

	/**协调因子*/
	@Override
	public float coord(int overlap, int maxOverlap){
		return overlap / (float)maxOverlap;
	}

	/**查询椅子计算*/
	@Override
	public float queryNorm(float sumOfSquaredWeights){
		return 1f;
	}

	/**词频计算
	 *@param freq query term 在 field 中 出现的词数
	 *<p>由于商品数据普遍较短，词频不具有明显权重意义，因此不做特别计算</p>
	 *@return 1.0
	 */
	public float tf(float freq){
		return 1f;
	}

	/**逆向文档频率
	 *@param docFreq 该 field:term 在索引中匹配到的文档数目
	 *@param numDocs 包含目标 field 的所有文档数目
	 *<p>Invert Document Frequence = <code>log(numDocs/(docFreq+1)) + 1</code></p>
	 *@return idf value
	 */
	public float idf(long docFreq, long numDocs){
		return (float)(Math.log(numDocs/(double)(docFreq+1))+1.0);
	}

	/**计算当前field的boost norm(索引时设置的提权信息)
	 *@param state 一个 field 的反向索引信息对象，包含该域的索引信息
	 *<p><code>norm = documentBoost * fieldBoost * lengthNorm</code>
	 *由于商品信息普遍简短,lengthNorm不具有权重特征,因此在此只提取boost值</p>
	 *@return 返回field的norm值
	 */
	@Override
	public final long computeNorm(FieldInvertState state){
		encodeNormValue(state.getBoost());
	}

	/**创建idf描述对象 针对一个field中的一个 matched term*/
	public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats){
		final long df = termStats.docFreq();
		final long max = collectionStats.maxDoc();
		final float idf = idf(df, max);
		return new Explanation(idf, "idf(docFreq = "+df+", maxDoc = "+max+ ")");
	}

	/**创建idf描述对象 针对一个field中的多个 matched term*/
	public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats[]){
		long final max = collectionStats.maxDoc();
		float idf = 0f;
		Explanation exp = new Explanation();
		exp.setDescription("idf(), sum of");
		for(TermStatistics stat: termStats){
			long final termDf = stat.docFreq();
			long float termIdf = idf(termDf, max);
			exp.addDetail(new Explanation(termIdf, "idf(docFreq = " +termDf+ ", maxDoc = "+max+")"));
			idf += termIdf;
		}
		exp.setValue(idf);
		return exp;
	}

	/**创建 Weight对象（针对一个query term）
	*/
	@Override
	public final SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics termStats[]){
		final Explanation idf = termStats.length == 1?
			new Explanation(collectionStats, termStats[0]):
			new Explanation(collectionStats, termStats);
	}
}
