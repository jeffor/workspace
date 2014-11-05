package org.apache.lucene.searcher;

public class IndexSearcher{
	final IndexReader reader;

	final IndexReader reader;
	protected final IndexReaderContext readerContext;
	protected final List<AtomicReaderContext> leafContexts;

	protected final LeafSlice[] leafSlices;

	private static final Similarity defaultSimilarity = new DefaultSimilarity();
	private Similarity similarity = defaultSimilarity;

	private final ExecutorService executor;

	public static final Similarity getSimilarity(){
		return defaultSimilarity;
	}

	/**构造函数*/
	public IndexSearcher(IndexReader reader){
		this(reader, null);
	}

	public IndexSearcher(IndexReaderContext context){
		this(context, null);
	}

	public IndexSearcher(IndexReader reader, ExecutorService executor){
		this(reader.getContext(), executor);
	}

	public IndexSearcher(IndexReaderContext context, ExecutorService executor){
		this.readerContext = context;
		this.reader = context.getReader;
		this.executor = executor;
		leafContexts = context.getLeaves();
		leafSlices = executor == null? null: slice(leafContexts);
	}

	protected LeafSlice[] silce(List<AtomicReaderContext> leaves){
		LeafSlice[] slices = new LeafSlice[leaves.size()];
		for(int i = 0; i < slices.length; ++i){
			slices[i] = new LeafSlice(leaves.get(i));
		}
		return slices;
	}

	/**常用接口*/

	public IndexReader getIndexReader(){
		return reader;
	}

	public Document doc(int docID) throws IOException{
		return reader.document(docID);
	}

	public void doc(int docID, StoredFieldVisitor visitor) throws IOException{
		return reader.document(docID, visitor);
	}

	public Document doc(int docID, Set<String> fieldToLoad) throws IOException{
		return reader.document(docID, fieldToLoad);
	}

	public void setSimilarity(Similarity similarity){
		this.similarity = similarity;
	}

	public Similarity getSimilarity(){
		return similarity;
	}

	/**内部接口*/

	protected Query wrapFilter(Query query, Filter filter){
		return filter == null? query: new FilteredQuery(query, filter);
	}

	public TopDocs searchAfter(ScoreDoc after, Query query, int n) throws IOException{
		return search(createNormilizedWeight(query), after, n);
	}

	public TopDocs searchAfter(ScoreDoc after, Query query, Filter filter, int n) throws IOException{
		return search(createNormilizedWeight(wrapFilter(query, filter)), after, n);
	}

	public TopDocs search(Query query, int n) throws IOException{
		return search(query, null, n);
	}

	public TopDocs search(Query query, Filter filter, int n) throws IOException{
		return search(createNormilizedWeight(wrapFilter(query, filter)), null, n);
	}

}
