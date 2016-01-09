
public class CNode extends ENode{
	private double value;
	private String strValue;
	
	public CNode(double value){
		super('C', null, null);
		this.value = value;
	}
	
	public CNode(String strValue){
		this(Double.parseDouble(strValue));
		this.strValue = strValue;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}
}
