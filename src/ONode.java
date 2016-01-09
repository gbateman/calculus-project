
public class ONode extends ENode{
	private char op;
	
	public ONode(char op, ENode left, ENode right){
		super('O', left, right);
		this.op = op;
	}

	public char getOp() {
		return op;
	}

	public void setOp(char op) {
		this.op = op;
	}	
}
