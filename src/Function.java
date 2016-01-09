import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class Function {
	String original;
	String y;
	int c;
	double x;
	private ENode yTree, dyTree, d2yTree, d3yTree;
	private ArrayList<ENode> trees;
	double a, b, step;
	private TreeSet<Double> yRoots, dyRoots, d2yRoots;
	private TreeMap<Double,String> roots;
	private String[] remarks = {"x intercept", "local minimum", "local maximum", "point of inflection", "error"};
	private int newtonCounter;
	private int errorCode;
	private String[] errorMessages = {"valid", "Division by Zero Error", "Domain Error", "Number Format Error", "Illegal Operator", "Bracket Syntax Error", "There's nothing there mate"};
	private String[] functions = {"abs", "acos", "asin", "atan", "cos", "cot", "csc", "ln", "log", "sec", "sin", "sqrt", "sqr", "tan"};
	private String digits = ".0123456789";
	private String vars = "abcdefghijklmnopqrstuvwxyz";
	
	public Function(String def, double a, double b, double step) {
		errorCode = 0;
		this.a = a;
		this.b = b;
		this.step = step;
		original = def;
		y = "" + (original.length() > 0 ? preProcess(original) : "");
		yTree = parse();
		yTree = prune(yTree);
		dyTree = differentiate(yTree);
		dyTree = prune(dyTree);
		d2yTree = differentiate(dyTree);
		d2yTree = prune(d2yTree);
		d3yTree = differentiate(d2yTree);
		d3yTree = prune(d3yTree);
		trees = new ArrayList<ENode>();
		trees.add(yTree);
		trees.add(dyTree);
		trees.add(d2yTree);
		trees.add(d3yTree);
		yRoots = new TreeSet<Double>();
		dyRoots = new TreeSet<Double>();
		d2yRoots = new TreeSet<Double>();
		roots = new TreeMap<Double,String>();
		newtonCounter = 0;
		findRoots(a, b, step);
		analyze();
	}
	
	public Function(String def) {
		this(def, -10, 10, 1);
	}
	
	public String getAnalysis(){
		String output = "x\tf(x)\tf'(x)\tf\"(x)\tFeature\n";
		for(Double d: roots.keySet()){
			output += d + "\t" + roots.get(d) + "\n";
		}
		return output;
	}
	
	private void analyze(){
		/*for(Double d: yRoots){
			double fx = f(0, d);
			double dfx = f(1, d);
			double d2fx = f(2, d);
			String r = remarks[0];//[getRootType(fx, dfx, d2fx)];
			roots.put(d, fx + "\t" + dfx  + "\t" + d2fx + "\t" + r);
		}*/
		for(Double d: yRoots){
			roots.put(d, 0d + "\t" + round(f(1, d), 2)  + "\t" + round(f(2, d), 2) + "\t" + remarks[0]);
		}
		for(Double d: dyRoots){
			roots.put(d, round(f(0, d), 2) + "\t" + 0d  + "\t" + round(f(2, d), 2) + "\t" + remarks[f(2, d) >= 0 ? 1 : 2]);
		}
		for(Double d: d2yRoots){
			roots.put(d, round(f(0, d), 2) + "\t" + round(f(1, d), 2)  + "\t" + 0d + "\t" + remarks[3]);
		}
	}
	
	/*private int getRootType(double fx, double dfx, double d2fx){
		if(approxEqual(0, fx)){
			return 0;
		}
		if(approxEqual(0, dfx)){
			if(d2fx > 0){
				return 1;
			} else if(d2fx < 0){
				return 2;
			} else {
				// Table required for accurate output
				return 1;
			}
		}
		if(approxEqual(0, d2fx)){
			return 3;
		}
		return 4;
	}*/
	
	private ENode parse(){
		ENode output = null;
		c = 0;
		if(y.length() < 1){
			errorCode = 6;
			return new CNode(1);
		}
		output = expression();
		if(c < y.length()){
			errorCode = 4;
			return new CNode(1);
		}
		return output;
	}
	
	private ENode prune(ENode node){
		ENode output = node;
		if(node != null){
			node.setLeft(prune(node.getLeft()));
			node.setRight(prune(node.getRight()));
			switch(node.getType()){
			case 'O':
				return pruneO(node);
			case 'F':
				if(reportHelper(node).equals("CF")){
					return new CNode(evaluate(node));
				}
			}
		}
		return output;
	}
	
	private ENode pruneO(ENode node){
		ENode output = node;
		if(reportHelper(node).equals("COC")){
			return new CNode(evaluate(node));
		}
		switch (((ONode) node).getOp()){
		case '+': 
			if(isZero(node.getLeft())){
				return node.getRight();
			} else if(isZero(node.getRight())){
				return node.getLeft();
			}
		case '-':
			if(isZero(node.getRight())){
				return node.getLeft();
			}
		case '*':
			if(isZero(node.getLeft()) || isZero(node.getRight())){
				return new CNode(0);
			}
			if(node.getLeft().getType() == 'C' && ((CNode) node.getLeft()).getValue() == 1){
				return node.getRight();
			}
			if(node.getRight().getType() == 'C' && ((CNode) node.getRight()).getValue() == 1){
				return node.getLeft();
			}
		case '/':
			if(node.getRight().getType() == 'C' && ((CNode) node.getRight()).getValue() == 1){
				return node.getLeft();
			}
		case '^':
			if(node.getRight().getType() == 'C' && ((CNode) node.getRight()).getValue() == 1){
				return node.getLeft();
			}
			if(node.getRight().getType() == 'C' && ((CNode) node.getRight()).getValue() == 0){
				if(node.getLeft().getType() == 'C' && ((CNode) node.getRight()).getValue() == 0){
					errorCode = 1;
				}
				return new CNode(1);
			}
		}
		return output;
	}
	
	private boolean isZero(ENode node){
		return node.getType() == 'C' && Math.abs(((CNode) node).getValue()) < 1E-10;
	}
	
	private boolean approxEqual(int i, double d){
		return Math.abs(i-d) < 1E-20;
	}
	
	public void findRoots(double a, double b, double step){
		yRoots = createRoots(0, a, b, step);
		dyRoots = createRoots(1, a, b, step);
		d2yRoots = createRoots(2, a, b, step);
	}
	
	private TreeSet<Double> createRoots(int i, double a, double b, double step){
		TreeSet<Double> roots = new TreeSet<Double>();
		newtonCounter = 0;
		if(!reportStructure(i).equals("C")){
			for(double d = a; d < b; d += step){
				//System.out.println(a + ", " + b  + ", " + d  + ", " + (d + step) + ", " + f(i, d));
				if(f(i, d) * f(i, d+step) <= 0){
					roots.add(getRootByNewton(i, d));
				}
			}
		}
		return roots;
	}
	
	private double getRootByNewton(int i, double a){
		if(approxEqual(0, f(i, a)) || newtonCounter == 20){
			return Math.round(a*1E2)/1E2;
		}
		newtonCounter++;
		return getRootByNewton(i, a - f(i, a)/f(i+1, a));
	}
	
	public TreeSet<Double> getRoots(int i){
		switch(i){
		case 0: return yRoots;
		case 1: return dyRoots;
		case 2: return d2yRoots;
		}
		return null;
	}
	
	private ENode duplicate(ENode node){
		if(node == null){
			return null;
		}
		switch(node.getType()){
			case 'C': return new CNode(((CNode) node).getValue()); 
			case 'V': return new VNode(((VNode) node).getVar());
			case 'O': return new ONode(((ONode) node).getOp(), duplicate(node.getLeft()), duplicate(node.getRight()));
			case 'F': return new FNode(((FNode) node).getWhich(), duplicate(node.getLeft()));
		}
		return null;
	}
	
	private ENode differentiate(ENode node){
		if(node == null){
			return null;
		}
		switch(node.getType()){
		case 'C': return new CNode(0);
		case 'V': return new CNode(1);
		case 'O': 
			switch(((ONode) node).getOp()){
			case '+': return sumRule(node);
			case '-': return sumRule(node);
			case '*': return productRule(node);
			case '/': return quotientRule(node);
			case '^': return powerRule(node);
			}
		case 'F':
			ENode dydu = null;
			switch(((FNode) node).getWhich()){
			case 0: dydu = new ONode('/', new FNode(0,  duplicate(node.getLeft())), duplicate(node.getLeft()));break;
			case 1: dydu = new ONode('/', new CNode(-1), new FNode(11, new ONode('-', new CNode(1), new ONode('^', duplicate(node.getLeft()), new CNode(2)))));break;
			case 2: dydu = new ONode('/', new CNode(1), new FNode(11, new ONode('-', new CNode(1), new ONode('^', duplicate(node.getLeft()), new CNode(2)))));break;
			case 3: dydu = new ONode('/', new CNode(1), new ONode('+', new ONode('^', duplicate(node.getLeft()), new CNode(2)), new CNode(1)));break;
			case 4: dydu = new ONode('*', new CNode(-1), new FNode(10, duplicate(node.getLeft())));break;
			case 5: dydu = new ONode('*', new CNode(-1), new ONode('^', new FNode(6, duplicate(node.getLeft())), new CNode(2)));break;
			case 6: dydu = new ONode('^', new FNode(9, duplicate(node.getLeft())), new CNode(2));break;
			case 7: dydu = new ONode('/', new CNode(1), duplicate(node.getLeft()));break;
			case 8: dydu = new ONode('/', new CNode(1), new ONode('*', duplicate(node.getLeft()), new FNode(7, new CNode(10))));break;
			case 9: dydu = new ONode('*', new FNode(9, duplicate(node.getLeft())), new FNode(13, duplicate(node.getLeft())));break;
			case 10: dydu = new FNode(4, duplicate(node.getLeft()));break;
			case 11: dydu = new ONode('/', new CNode(1), new ONode('*', new CNode(2), new FNode(11, duplicate(node.getLeft()))));break;
			case 12: dydu = new ONode('*', new CNode(2), duplicate(node.getLeft()));break;
			case 13: dydu = new ONode('^', new FNode(9, duplicate(node.getLeft())), new CNode(2));break;
			}
			return new ONode('*', dydu, differentiate(node.getLeft()));
		}
		return null;
	}
	
	private ENode sumRule(ENode node){
		return new ONode(((ONode) node).getOp(), differentiate(node.getLeft()), differentiate(node.getRight()));
	}
	
	private ENode productRule(ENode node){
		return new ONode('+', new ONode('*', differentiate(node.getLeft()), duplicate(node.getRight())), new ONode('*', duplicate(node.getLeft()), differentiate(node.getRight())));
	}
	
	private ENode quotientRule(ENode node){
		return new ONode('/', new ONode('-', new ONode('*', differentiate(node.getLeft()), duplicate(node.getRight())), new ONode('*', duplicate(node.getLeft()), differentiate(node.getRight()))), new ONode('^', duplicate(node.getRight()), new CNode(2)));
	}
	
	private ENode powerRule(ENode node){
		if(node.getRight().getType() == 'C'){
			return new ONode('*', new ONode('*', duplicate(node.getRight()), new ONode('^', duplicate(node.getLeft()), new CNode(((CNode) node.getRight()).getValue() - 1))), differentiate(node.getLeft()));
		}
		return null;
	}
	
	private ENode expression(){
		ENode output = term();
		while(hasNextChar() && (y.charAt(c) == '+' || y.charAt(c) == '-')){
			char op = y.charAt(c);
			c++;
			output = new ONode(op, output, term());
		}
		return output;
	}
	
	private ENode term(){
		ENode output = factor();
		while(hasNextChar() && (y.charAt(c) == '*' || y.charAt(c) == '/')){
			char op = y.charAt(c);
			c++;
			output = new ONode(op, output, factor());
		}
		return output;
	}
	
	private ENode factor(){
		ENode output = primitive();
		while(hasNextChar() && y.charAt(c) == '^'){
			c++;
			output = new ONode('^', output, factor());
		}
		return output;
	}
	
	private ENode primitive(){
		ENode output = null;
		if(isConstant(y.charAt(c))){
			return constant();
		}
		if(isVariable(y.charAt(c))){
			return variable();
		}
		
		if(isFunction(y.charAt(c))){
			return function();
		}

		if(y.charAt(c) == '('){
			c++;
			output = expression();
			if(c < y.length() && y.charAt(c) == ')'){
				c++;
			} else {
				errorCode = 5;
				return new CNode("1");
			}
		}
		return output;
	}
	
	private CNode constant(){
		String value = "" + y.charAt(c);
		if(y.charAt(c) == 'P'){
			c++;
			return new CNode(Math.PI);
		}
		if(y.charAt(c) == 'Y'){
			c++;
			return new CNode(Math.E);
		}
		
		int save = c;
		while(hasNextChar() && isDigit(y.charAt(c+1))) c++;
		value = y.substring(save, c+1);
		c++;
		
		try {
			Double.parseDouble(value);
		} catch (NumberFormatException e){
			errorCode = 3;
			return new CNode(1);
		}
		
		return new CNode(value);
	}
	
	private VNode variable(){
		return new VNode(y.charAt(c++));
	}
	
	private FNode function(){
		return new FNode(-65+(int)y.charAt(c++), primitive());
	}

	private String preProcess(String f){
		// gets rid of upper-case to simplify and allow for indicators later on
		StringBuffer sf = new StringBuffer(f.toLowerCase());
		// replaces all built in functions with an upper-case indicator
		for(int i = 0; i < functions.length; i++){
			int index = sf.indexOf(functions[i]);
			if(index > -1){
				sf.replace(index, index+functions[i].length(), String.valueOf((char)(65+i)));
			}
		}
		// Turns the first negative in a statement into a subtraction
		if(sf.charAt(0) == '-'){
			sf.insert(0, 0);
		}
		for(int i = 0; i < sf.length(); i++){
			// removes all spaces
			if(Character.isWhitespace(sf.charAt(i))){
				sf.deleteCharAt(i);
			}
			// replaces all references to e with an upper-case indicator
			if(sf.charAt(i) == 'e'){
				sf.replace(i, i+1, "Y");
			}
			if(i < sf.length() - 1){
				// replaces all references to pi with an upper-case indicator
				if(sf.charAt(i) == 'p' && sf.charAt(i+1) == 'i'){
					sf.replace(i, i+2, "P");
				}
			}
			if(i < sf.length() - 1){
				// inserts an asterisk between any implied multiplication
				// this is any combination of a letter, digit or right bracket on the right,
				// a letter, digit or left bracket on the left, but not two digits
				if((isDigit(sf.charAt(i)) || (isVariable(sf.charAt(i)) || sf.charAt(i) == 'P' || sf.charAt(i) == 'Y')
						|| sf.charAt(i) == ')')
						&& (isDigit(sf.charAt(i+1)) || Character.isLetter(sf.charAt(i+1)) || sf.charAt(i+1) == '(')
						&& !(isDigit(sf.charAt(i)) && isDigit(sf.charAt(i+1)))){
					sf.insert(i+1, '*');
				}
				if(i > 0){
					if(!isDigit(sf.charAt(i-1)) && sf.charAt(i) == '-' && isVariable(sf.charAt(i+1))){
						sf.insert(i, "(0");
						sf.insert(i+4, ")");
						i+=4;
					}
				}
			}
		}
		return sf.toString();
	}
	
	public double f(int which, double x){
		this.x = x;
		double output = evaluate(trees.get(which));
		if(errorCode > 0){
			System.out.println(errorMessages[errorCode]);
			return 0;
		}
		return Math.abs(output) < 1E-10 ? 0 : output;
	}
	
	public int getErrorCode(){
		return errorCode;
	}
	
	public String getErrorMessage(){
		return errorMessages[errorCode];
	}
	
	public double evaluate(ENode node){
		if(node == null){
			return 0;
		}
		if(node.getType() == 'C'){
			return ((CNode) node).getValue();
		}
		if(node.getType() == 'V'){
			return x;
		}
		if(node.getType() == 'F'){
			double param = evaluate(node.getLeft());
			switch(((FNode) node).getWhich()){
				case(0): return Math.abs(param);
				case(1): return (-1 <= param && param <= 1 ? Math.acos(param) : (errorCode = 2));
				case(2): return (-1 <= param && param <= 1 ? Math.asin(param) : (errorCode = 2));
				case(3): return (-1 <= param && param <= 1 ? Math.atan(param) : (errorCode = 2));
				case(4): return Math.cos(param);
				case(5): return ((param % Math.PI) != 0 ? 1/Math.tan(param) : (errorCode = 2));
				case(6): return ((param % Math.PI) != 0 ? 1/Math.sin(param) : (errorCode = 2));
				case(7): return (param >= 0 ? Math.log(param) : (errorCode = 2));
				case(8): return (param >= 0 ? Math.log10(param) : (errorCode = 2));
				case(9): return ((param - Math.PI/2) % Math.PI  != 0 ? 1/Math.cos(param) : (errorCode = 2));
				case(10): return Math.sin(param);
				case(11): return (param >= 0 ? Math.sqrt(param): (errorCode = 2));
				case(12): return Math.pow(param, 2);
				case(13): return ((param - Math.PI/2) % Math.PI  != 0 ? Math.tan(param) : (errorCode = 2));
			}
		}
		if(node.getType() == 'O'){
			double param1 = evaluate(node.getLeft()), param2 = evaluate(node.getRight());
			switch(((ONode) node).getOp()){
				case('+'): return param1 + param2;
				case('-'): return param1 - param2;
				case('*'): return param1 * param2;
				case('/'): return (param2 != 0 ? param1 / param2 : (errorCode = 1));
				case('^'): return Math.pow(param1, param2);
			}
			return ((CNode) node).getValue();
		}
		return 0;
	}

	public String reportStructure(int which) {
		return reportHelper(trees.get(which));
	}
	
	private String reportHelper(ENode node){
		if(node == null){
			return "";
		}
		return reportHelper(node.getLeft()) + node.getType() + reportHelper(node.getRight());
	}
	
	private boolean isConstant(char c){
		return isDigit(c) || c == 'P' || c == 'Y';
	}
	
	private boolean isDigit(char c){
		return digits.indexOf(c) > -1;
	}
	
	private boolean isVariable(char c){
		return vars.indexOf(c) > -1;
	}
	
	private boolean isFunction(char c){
		return vars.toUpperCase().indexOf(c) > -1;
	}
	
	private boolean hasNextChar(){
		return c < y.length()-1;
	}
	
	private double round(double d, int i){
		return Math.round(d*Math.pow(10, i))/Math.pow(10, i);
	}
	
	public String toString(){
		return y;
	}
}
