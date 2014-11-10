package org.apache.lucene.index;

/**term 通常代表了文本中的一个词汇, 它是搜索的基本单元。
  term 由两个组成部分, 一个是 field, 一个是 name
  需要注意的是 有时候 term 不仅仅只是单词, 可能是 时间, email, etc.*/
public final class Term implements Comparable<Term>{
	String field;
	BytesRef bytes;

	public Term(field field, BytesRef bytes){
		this.field = field;
		this.bytes = bytes;
	}

	public Term(field field, String text){
		this(field, new BytesRef(text));
	}

	public Term(String field){
		this(field, new BytesRef());
	}

	public final String field(){
		return field;
	}

	public final String text(){
		return toString(bytes);
	}

	public static final String toString(BytesRef termText){
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
			.onMalformedInput(CodingErrorAction.REPORT)
			.onUnmappableCharacter(CodingErrorAction.REPORT);
		try {
			return decoder.decode(ByteBuffer.wrap(termText.bytes, termText.offset, termText.length)).toString();
		} catch (CharacterCodingException e) {
			return termText.toString();
		}
	}

	public BytesRef bytes(){
		return bytes;
	}

}
