
/**语汇对象状态类, 用于封装一个语汇对象的内部状态，避免重新计算*/
public abstract class TermState implements Cloneable{

	public abstract void copyFrom(TermState state);		//复制其他状态
	public TermState clone();				//克隆当前状态
	
}

/**描述语汇单元顺序信息*/
public class OrdTermState{
	public long ord;
}

@tree
TermState
	OrdTermState
