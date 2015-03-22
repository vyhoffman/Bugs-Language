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
	Color color;
	double x, y, angle;
	boolean exitLoop;
	static double e = 0.001;
	
	/**Bug constructor
	 * @param name Bug's name
	 */
	Bug(String name) {
		this.name = name;
		color = Color.BLACK;
		x = 0.0;
		y = 0.0;
		angle = 0.0;
		exitLoop = false;
	}
	
	/**Returns the Bug's name.
	 * @return String value of Bug's name field
	 */
	String getName() {
		return name;
	}
	
	/**Sets the Bug's name to the string provided. For testing only.
	 * @param name to set
	 */
	void setName(String name) {
		this.name = name;
	}
	
	/**Returns the Bug's current color.
	 * @return java.awt.Color value of Bug's color field
	 */
	Color getColor() {
		return color;
	}
	
	/**Sets the Bug's color to the Color provided. For testing only.
	 * @param color to set
	 */
	void setColor(Color color) {
		this.color = color;
	}
	
	/**Returns Bug's current x coordinate.
	 * @return double value of Bug's x field
	 */
	double getX() {
		return x;
	}
	
	/**Sets the Bug's x coordinate to the value provided. For testing only.
	 * @param x value to set
	 */
	void setX(double x) {
		this.x = x;
	}
	
	/**Returns Bug's current y coordinate.
	 * @return double value of Bug's y field
	 */
	double getY() {
		return y;
	}
	
	/**Sets the Bug's y coordinate to the value provided. For testing only.
	 * @param y value to set
	 */
	void setY(double y) {
		this.y = y;
	}
	
	/**Returns Bug's current angle.
	 * @return double value of Bug's angle field
	 */
	double getAngle() {
		return angle;
	}
	
	/**Sets Bug's angle field to the value provided. For testing only.
	 * @param angle to set
	 */
	void setAngle(double angle) {
		this.angle = angle;
	}
	
	/**Returns true if Bug is currently set to exit a loop, otherwise false.
	 * @return boolean value of Bug's exitLoop field
	 */
	boolean getExitLoop() {
		return exitLoop;
	}
	
	/**Sets Bug's exitLoop value to the boolean provided. For testing only.
	 * @param exitLoop value to set
	 */
	void setExitLoop(boolean exitLoop) {
		this.exitLoop = exitLoop;
	}
	
	//-----------End of helper/testing methods
	//-----------Primary functions below
	
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
			if (tree.getNumberOfChildren() == 1) {
				return evaluate(tree.getChild(0));
			} else {
				return evaluate(tree.getChild(0)) + evaluate(tree.getChild(1));
			}
		case "-":
			if (tree.getNumberOfChildren() == 1) {
				return -evaluate(tree.getChild(0));
			} else {
				return evaluate(tree.getChild(0)) - evaluate(tree.getChild(1));
			}
		case "*":
			return evaluate(tree.getChild(0)) * evaluate(tree.getChild(1));
		case "/":
			return evaluate(tree.getChild(0)) / evaluate(tree.getChild(1));
		case "<":
			if (evaluate(tree.getChild(0)) - evaluate(tree.getChild(1)) < -e) {
				return 1.0;
			}
			return 0.0;
		case "<=":
			if (evaluate(tree.getChild(0)) - evaluate(tree.getChild(1)) <= e) {
				return 1.0;
			}
			return 0.0;
		case "=":
			double d = evaluate(tree.getChild(0)) - evaluate(tree.getChild(1));
			if (Math.abs(d) <= e) return 1.0;
			return 0.0;
		case ">=":
			if (evaluate(tree.getChild(0)) - evaluate(tree.getChild(1)) >= -e) {
				return 1.0;
			}
			return 0.0;
		case ">":
			if (evaluate(tree.getChild(0)) - evaluate(tree.getChild(1)) > e) {
				return 1.0;
			}
			return 0.0;
		case "case":
			double result = evaluate(tree.getChild(0));
			if (Math.abs(result) > e) interpret(tree.getChild(1));
			return result;
		case "call":
			// Not this week
			break;
		default:
			// if it's a leaf/just a number
			if (tree.getNumberOfChildren() == 0) {
				try {
					return Double.parseDouble(tree.getValue().value);					
				} catch (Exception e) {
					return fetch(tree.getValue().value);
				}
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
			// Save the name of the Bug in an instance variable.
			name = tree.getChild(0).getValue().value; // ???
			interpret(tree.getChild(1)); // Interpret the list of var declarations.
			interpret(tree.getChild(2)); // Interpret the initialization block.
			interpret(tree.getChild(3)); // Interpret the block (of commands).
			// For this assignment, ignore the list of function declarations.
			break;
		case "list":
			for (int i = 0; i < tree.getNumberOfChildren(); i++) {
				if (exitLoop) break;
				interpret(tree.getChild(i));
			}
			break;
		case "block":
			for (int i = 0; i < tree.getNumberOfChildren(); i++) {
				if (exitLoop) break;
				interpret(tree.getChild(i));
			}
			break;
		case "var":
			for (int i = 0; i < tree.getNumberOfChildren(); i++) {
				store(tree.getChild(i).getValue().value, 0.0);
			}
			break;
		case "initially":
			interpret(tree.getChild(0)); // interpret(block) --added block case
			break;
		case "move":
			double tempD = evaluate(tree.getChild(0));
			double dx = Math.cos(angle * Math.PI / 180) * tempD;
			double dy = -(Math.sin(angle * Math.PI / 180) * tempD);
			store("x", x + dx);
			store("y", y + dy);
			// sin(270) = -1; 270 is down. This would be perfect if we were
			// using Cartesian coordinates, but with CG default (down == higher 
			// value), have to subtract sin from y instead of adding.
			break;
		case "moveto":
			store("x", evaluate(tree.getChild(0)));
			store("y", evaluate(tree.getChild(1)));
			break;
		case "turn":
			double newAngle = (angle + evaluate(tree.getChild(0))) % 360.0;
			store("angle", newAngle >= 0.0 ? newAngle : newAngle + 360.0);
			break;
		case "turnto":
			double newA = evaluate(tree.getChild(0)) % 360.0;
			store("angle", newA >= 0.0 ? newA : newA + 360.0);
			break;
		case "return":
			// Not this week
			break;
		case "line":
			// Not this week
			break;
		case "assign":
			String tempS = tree.getChild(0).getValue().value;
			if (!variables.containsKey(tempS) && !"x".equals(tempS) && 
					!"y".equals(tempS) && !"angle".equals(tempS)) 
				throw new RuntimeException("No such variable declared!");
			store(tempS, evaluate(tree.getChild(1)));
			break;
		case "loop":
			while (!exitLoop) {
				interpret(tree.getChild(0));
			}
			exitLoop = false;
			break;
		case "exit":
			// an exit-if statement - set exit true if condition true
			if (Math.abs(evaluate(tree.getChild(0))) > e) exitLoop = true;
			break;
		case "switch":
			for (int i = 0; i < tree.getNumberOfChildren(); i++) {
				if (Math.abs(evaluate(tree.getChild(i))) > e) break;
			}
			break;
		case "color":
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
			else if (colorName.equals("brown")) color = new Color(64, 32, 0);
			else if (colorName.equals("purple")) color = new Color(80, 0, 64);
			else throw new RuntimeException("Invalid color!");
			break;
		case "function":
			functions.put(tree.getChild(0).getValue().value, tree);
		}
	}
	
	
}
