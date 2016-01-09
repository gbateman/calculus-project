
public class FNode extends ENode{
	private int which;
	
	public FNode(int which, ENode left){
		super('F', left, null);
		this.which = which;
	}

	public int getWhich() {
		return which;
	}

	public void setWhich(int which) {
		this.which = which;
	}
}
