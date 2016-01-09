
public class VNode extends ENode{
	private char var;
	
	public VNode(char var){
		super('V', null, null);
		this.var = var;
	}

	public char getVar() {
		return var;
	}

	public void setVar(char var) {
		this.var = var;
	}
}
