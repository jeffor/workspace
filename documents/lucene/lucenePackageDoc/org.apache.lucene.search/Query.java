
public abstract class Query{
		/**data field*/
		private float boost = 1.0;		//该query的boost值

		/**method*/

		//boost 读写操作
		public void setBoost(float boost){ this.boost = boost; }
		public float getBoost(){ return this.boost; }

		//重建query
		public Query rewrite(IndexReader reader);

		//创建Weight对象
		public Weight createWeight(IndexSearcher searcher);

		//提取terms
		public void extractTerms(Set<Term> terms);		

}


