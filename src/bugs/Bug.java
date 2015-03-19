package bugs;

import java.awt.Color;
import java.util.HashMap;

import tree.Tree;

/**Bug interpreter class
 * @author Nicki Hoffman
 *
 */
public class Bug {
	// 
	HashMap<String, Tree<Token>> functions = new HashMap<String, Tree<Token>>();
	HashMap<String, Double> variables = new HashMap<String, Double>();
	String name;
	Color color = Color.BLACK;
	double x = 0.0;
	double y = 0.0;
	double angle = 0.0;
	boolean exitLoop = false;
	
	/**Stores the parameter value under the key variable in the list
	 * of variables (or updates the appropriate instance variable if it is
	 * one of x, y, or angle).
	 * @param <code>variable</code> name of the variable to store
	 * @param <code>value</code> value of the variable to store
	 */
	void store(String variable, double value) {
		if (variable.equals("x")) x = value;
		else if (variable.equals("y")) y = value;
		else if (variable.equals("angle")) angle = value;
		else variables.put(variable, value);
	}
	
	/**Returns the value of the named variable, if variable is defined,
	 * otherwise throws a RuntimeException.
	 * @param <code>variable</code> to look up
	 * @return double value of the variable
	 * @throws <code>RuntimeException</code>
	 */
	double fetch(String variable) throws RuntimeException {
		if (variable.equals("x")) return x;
		else if (variable.equals("y")) return y;
		else if (variable.equals("angle")) return angle;
		else if (!variables.containsKey(variable)) 
			throw new RuntimeException("No such variable!");
		return variables.get(variable);
	}
	
	/**Evaluates the expression, switch case, or function call specified.
	 * @param <code>tree</code> to evaluate
	 * @return double result of evaluation
	 */
	public double evaluate(Tree<Token> tree) {
		switch(tree.getValue().value) {
		case "+":
			// TODO test
			if (tree.getNumberOfChildren() == 1) {
				return evaluate(tree.getChild(0));
			} else {
				return evaluate(tree.getChild(0)) + evaluate(tree.getChild(1));
			}
		case "-":
			// TODO test
			if (tree.getNumberOfChildren() == 1) {
				return evaluate(tree.getChild(0));
			} else {
				return evaluate(tree.getChild(0)) - evaluate(tree.getChild(1));
			}
		case "*":
			// TODO test
			return evaluate(tree.getChild(0)) * evaluate(tree.getChild(1));
		case "/":
			// TODO test
			return evaluate(tree.getChild(0)) / evaluate(tree.getChild(1));
		case "<":
			// TODO test
			if (evaluate(tree.getChild(0)) - evaluate(tree.getChild(1)) < -0.001) {
				return 1.0;
			}
			return 0.0;
		case "<=":
			// TODO test
			if (evaluate(tree.getChild(0)) - evaluate(tree.getChild(1)) <= 0.001) {
				return 1.0;
			}
			return 0.0;
		case "=":
			// TODO test
			double d = evaluate(tree.getChild(0)) - evaluate(tree.getChild(1));
			if (d < 0.001 && d > -0.001) return 1.0;
			return 0.0;
		case ">=":
			// TODO test
			if (evaluate(tree.getChild(0)) - evaluate(tree.getChild(1)) >= -0.001) {
				return 1.0;
			}
			return 0.0;
		case ">":
			// TODO test
			if (evaluate(tree.getChild(0)) - evaluate(tree.getChild(1)) > 0.001) {
				return 1.0;
			}
			return 0.0;
		case "case":
			// TODO test
			double result = evaluate(tree.getChild(0));
			if (result < -0.001 || result > 0.001) interpret(tree.getChild(1));
			return result;
		case "call":
			// Not this week
			break;
		default:
			// TODO if it's a leaf/just a number
			if (tree.getNumberOfChildren() == 0) {
				return Double.parseDouble(tree.getValue().value);
			}
		}
		return 0;
	}
	
	/**Interprets the tree specified.
	 * @param <code>tree</code> to interpret
	 * @throws <code>RuntimeException</code> if a color statement tries
	 * to assign an invalid color or an assignment statement tries to
	 * assign to an undeclared variable.
	 */
	public void interpret(Tree<Token> tree) throws RuntimeException {
		switch (tree.getValue().value) {
		case "program":
			// Not this week
			break;
		case "Allbugs":
			// Not this week
			break;
		case "Bug":
			// TODO test
			// Save the name of the Bug in an instance variable.
			name = tree.getChild(0).getValue().value; // ???
			interpret(tree.getChild(1)); // Interpret the list of var declarations.
			interpret(tree.getChild(2)); // Interpret the initialization block.
			interpret(tree.getChild(3)); // Interpret the block (of commands).
			// For this assignment, ignore the list of function declarations.
			break;
		case "list":
			// TODO test
			for (int i = 0; i < tree.getNumberOfChildren(); i++) {
				if (exitLoop) break;
				interpret(tree.getChild(i));
			}
			break;
		case "var":
			// TODO test
			for (int i = 0; i < tree.getNumberOfChildren(); i++) {
				store(tree.getChild(i).getValue().value, 0.0);
			}
			break;
		case "initially":
			// TODO test
			//interpret(tree.getChild(0)); // interpret(block) DNE
			Tree<Token> tempT = tree.getChild(0);
			for (int i = 0; i < tempT.getNumberOfChildren(); i++) {
				interpret(tempT.getChild(i));
			}
			break;
		case "move":
			// TODO test
			double tempD = evaluate(tree.getChild(0));
			y -= Math.sin(angle) * tempD; // sin(270) = -1; 270 is down. WHY
			x += Math.cos(angle) * tempD;
			break;
		case "moveto":
			// TODO test
			x = evaluate(tree.getChild(0));
			y = evaluate(tree.getChild(1));
			break;
		case "turn":
			// TODO test
			angle = (angle + evaluate(tree.getChild(0))) % 360.0;
			if (angle < 0.0) angle += 360.0;
			break;
		case "turnto":
			// TODO test
			angle = evaluate(tree.getChild(0)) % 360.0;
			if (angle < 0.0) angle += 360.0;
			break;
		case "return":
			// Not this week
			break;
		case "line":
			// Not this week
			break;
		case "assign":
			// TODO test
			String tempS = tree.getChild(0).getValue().value;
			if (!variables.containsKey(tempS)) 
				throw new RuntimeException("No such variable declared!");
			store(tempS, evaluate(tree.getChild(1)));
			break;
		case "loop":
			// TODO test
			while (!exitLoop) {
				Tree<Token> tempT2 = tree.getChild(0);
				for (int i = 0; i < tempT2.getNumberOfChildren(); i++) {
					interpret(tempT2.getChild(i));
				}
			}
			exitLoop = false;
			break;
		case "exit":
			// TODO test
			exitLoop = true; // shouldn't this be if (evaluate(whichever child))??
			break;
		case "switch":
			// TODO test
			double b;
			for (int i = 0; i < tree.getNumberOfChildren(); i++) {
				b = evaluate(tree.getChild(i));
				if (b < -0.001 || b > 0.001) break;
			}
			break;
		case "color":
			// TODO test
			String colorName = tree.getChild(0).getValue().value;
			if (colorName.equals("none")) color = null;
			else if (colorName.equals("black")) color = Color.BLACK;
			else if (colorName.equals("blue")) color = Color.BLUE;
			else if (colorName.equals("cyan")) color = Color.CYAN;
			else if (colorName.equals("darkGray")) color = Color.DARK_GRAY;
			else if (colorName.equals("gray")) color = Color.GRAY;
			else if (colorName.equals("green")) color = Color.GREEN;
			else if (colorName.equals("lightGray")) color = Color.LIGHT_GRAY;
			else if (colorName.equals("magenta")) color = Color.MAGENTA;
			else if (colorName.equals("orange")) color = Color.ORANGE;
			else if (colorName.equals("pink")) color = Color.PINK;
			else if (colorName.equals("red")) color = Color.RED;
			else if (colorName.equals("white")) color = Color.WHITE;
			else if (colorName.equals("yellow")) color = Color.YELLOW;
			else throw new RuntimeException("Invalid color!");
			break;
		case "function":
			// TODO test
			functions.put(tree.getChild(0).getValue().value, tree);
		}
	}
	
	
}
