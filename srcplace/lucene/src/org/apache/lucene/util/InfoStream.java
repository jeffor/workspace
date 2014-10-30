package org.apache.lucene.util;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SegmentInfos;
import java.io.closeable;

public abstract class InfoStream implements Closeable{
	private static final InfoStream NO_OUTPUT = new NoOutput();
	private static final class NoOutput extends InfoStream{
		public void message(String component, String message){
			assert false: "meaasge() couldn't be called when isEnable() return false";
		}

		public boolean isEnable(){
			return false;
		}

		public close(){}
	}

	/**返回模块信息*/
	public abstract void message(String component, String message);

	/**模块是否启用*/
	public abstract boolean isEnable(String component);

	private InfoStream defaultInfoStream = NO_OUTPUT;

	public static synchronized InfoStream getDefault(){
		return defaultInfoStream;
	}

	public static synchronized void setDefault(InfoStream infoStream){
		if(infoStream == null)
			throw new IllegalArgumentException("Cannot set InfoStream default implementation to null");
		defaultInfoStream = infoStream;
	}
}
