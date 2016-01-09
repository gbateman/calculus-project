
public class ENode {
	private char type;
	private ENode left, right;
	
	public ENode(char type, ENode left, ENode right){
		this.type = type;
		this.left = left;
		this.right = right;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public ENode getLeft() {
		return left;
	}

	public void setLeft(ENode left) {
		this.left = left;
	}

	public ENode getRight() {
		return right;
	}

	public void setRight(ENode right) {
		this.right = right;
	}
}
