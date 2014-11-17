package org.apache.lucene.index;

public class IndexWriterConfig{

	public static enum OpenMode{
		CREATE,
		APPEND,
		CREATE_OR_APPEND
	}

	public static final int DEFAULT_TERM_INDEX_INTERVAL = 32;
	public static final int DISABLE_AUTO_FLUSH = -1;
	public static final int DEFAULT_MAX_BUFFERED_DELETE_TERMS = DISABLE_AUTO_FLUSH;
	public static final int DEFAULT_MAX_BUFFERED_DOCS = DISABLE_AUTO_FLUSH;
	public static final int DEFAULT_RAM_BUFFER_SIZE = 16.0;

	public static long WRITE_LOCK_TIMEOUT = 1000;
	public final static boolean DEFAULT_WRITE_LOCK_POOLING = false;
}
