package org.apache.lucene.index;

/**term 通常表示一个词汇(包含词汇内容 和 所在field),
 *但是词汇不一定就是字符串(可能是时间,数字)*/
public final class Term implements Compareable<Term>{
	String    field;
	BytesRef  bytes;

	public Term(String field, BytesRef bytes){
		this.field = field;
		this.bytes = bytes;
	}

	public Term(String field, String text){
		this(field, new BytesRef(text));
	}

	public Term(String field){
		this(field, new BytesRef());
	}

	public final String text(){
		return toString(bytes);
	}

	/**解析字节数组数据*/
	public static final String toString(BytesRef termText) {
		// the term might not be text, but usually is. so we make a best effort
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
			.onMalformedInput(CodingErrorAction.REPORT)
			.onUnmappableCharacter(CodingErrorAction.REPORT);
		try {
			return decoder.decode(ByteBuffer.wrap(termText.bytes, termText.offset, termText.length)).toString();
		} catch (CharacterCodingException e) {
			return termText.toString();
		}
	}

	public final BytesRef Bytes(){
		return bytes;
	}

	@Override
	public final boolean equals(Object obj){
		if(this == obj)			//相同引用
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())	//是否类型相同
			return false;
		Term other = (Term)obj;			//类型转换
		if(field == null){
			if(other.field != null)
				return false;
		}else if(!field.equals(other.field))
			return false;
		if(bytes = null){
			return false;
		}else if(!bytes.equals(other.bytes))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((bytes == null) ? 0 : bytes.hashCode());
		return result;
	}

	@Override
	public final int compareTo(Term other){
		if(field.equals(other.field))
			return bytes.compareTo(other.bytes);
		else
			return field.compareTo(other.field);
	}

	@Override
	public final String toString(){
		return field + ":" + text();
	}
}
