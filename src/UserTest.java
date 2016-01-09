import javax.swing.JOptionPane;

public class UserTest {

	public static void main(String[] args){
		String def = JOptionPane.showInputDialog("Enter a function:");
		double x = Integer.valueOf(JOptionPane.showInputDialog("Enter a value to be evaulated at:"));
		double a = -10, b = 10, step = 0.1;
		
		Function f = new Function(def,a,b,step);
		String parsing = String.format("PARS: f(x) = %-17s", def);

		System.out.println();
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.printf("%-30s", parsing);
		System.out.printf("RESULT: %-25s", f.getErrorMessage());
		if (f.getErrorCode() == 0)
			System.out.printf("TREE: %-25s", f.reportStructure(0));
		System.out.println();

		if (f.getErrorCode() == 0) {
			String domain = String.format("%-6.2f", x).trim();
			String evaluation = String.format("EVAL: f(%s)  = %-20.5f", domain, f.f(0, x));
			System.out.printf("%-30s", evaluation.trim());
			System.out.printf("RESULT: %-25s", f.getErrorMessage());
			System.out.printf("TREE: %s\n", f.reportStructure(0));

			evaluation = String.format("EVAL: f'(%s) = %-20.5f", domain, f.f(1, x));
			System.out.printf("%-30s", evaluation.trim());
			System.out.printf("RESULT: %-25s", f.getErrorMessage());
			System.out.printf("TREE: %s\n", f.reportStructure(1));
			evaluation = String.format("EVAL: f''(%s)= %-20.5f", domain, f.f(2, x));
			System.out.printf("%-30s", evaluation.trim());
			System.out.printf("RESULT: %-25s", f.getErrorMessage());
			System.out.printf("TREE: %s\n", f.reportStructure(2));
		}
		System.out.println();
		System.out.println(">Summary of Roots:");
		System.out.println("  f:"+f.getRoots(0));
		System.out.println(" f':"+f.getRoots(1));
		System.out.println("f'':"+f.getRoots(2));
		System.out.println();
		System.out.printf(">Analysis over the closed interval [%5.2f,%5.2f]:\n",a,b);
		System.out.println(f.getAnalysis());
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}
}
