package bugs;

import java.awt.Color;
import java.util.HashMap;
import java.util.Stack;

import tree.Tree;

/**Bug object class
 * @author Nicki Hoffman
 *
 */
public class Bug extends Thread {
	// 
	Tree<Token> mainProgram;
	HashMap<String, Tree<Token>> functions = new HashMap<String, Tree<Token>>();
	HashMap<String, Double> variables = new HashMap<String, Double>();
	String name;
	Color color;
	double x, y, angle;
	boolean exitLoop;
	double returnVal;
	static double e = 0.001;
	Stack<HashMap<String, Double>> scopes = new Stack<HashMap<String, Double>>();
	Interpreter interp;
	boolean blocked; // I know, I'm a terrible person for not making more of
					// these private. I am TIRED of writing tedious methods.
	
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
		scopes.push(variables); // bottom of stack of variable maps
		this.interp = new Interpreter();
		blocked = false;
	}
	
	/**Bug constructor that takes a Bug definition to interpret and a reference
	 * to the controlling Interpreter.
	 * @param b Tree<Token> representing a Bug
	 * @param i Interpreter that's controlling all Bugs
	 */
	Bug(Tree<Token> b, Interpreter i) {
		this("");
		interp = i;
		blocked = true;
		interpret(b);
	}
	
	@Override
	public void run() {
		//loop through program
		int i = 0;
		while (i < mainProgram.getNumberOfChildren()) {
			//get permission
			interp.getActionPermit(this);
			while (!blocked && i < mainProgram.getNumberOfChildren()) {
				interpret(mainProgram.getChild(i));
				i++;
			}
			//interp.completeAction(this); //no, do this at the end of interpreting
		}
		interp.killBug(this);
		//indicate done TODO
	}
	
	/**Returns the Bug's name.
	 * @return String value of Bug's name field
	 */
	public String getBugName() {
		return name;
	}
	
	/**Sets the Bug's name to the string provided. For testing only.
	 * @param name to set
	 */
	public void setBugName(String name) {
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
	
	boolean findVariable(String var) {
		if ("x".equals(var) || "y".equals(var) || "angle".equals(var)) 
												return true;
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(var)) return true;
		}
		if (interp.variables.containsKey(var)) return true;
		return false;
	} //TODO test
	
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
		else {
			boolean found = false;
			for (int i = scopes.size() - 1; i >= 0; i--) {
				if (scopes.get(i).containsKey(variable)) {
					scopes.get(i).put(variable, value);
					found = true;
					break;
				}
			}
			if (!found && interp.variables.containsKey(variable)) {
				interp.variables.put(variable, value);
			} else if (!found) {
				variables.put(variable, value);
			} //am counting on caller to check if it requires pre-declared
		}
	} //TODO test
	
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
		else {
			for (int i = scopes.size() - 1; i >= 0; i--) {
				if (scopes.get(i).containsKey(variable)) 
							return scopes.get(i).get(variable);
			}
			if (interp.variables.containsKey(variable)) 
							return interp.variables.get(variable);					
		}
		throw new RuntimeException("Undeclared variable!");
	} //TODO test
	
	double fetchFrom(String bug, String v) {
		if (!interp.bugs.containsKey(bug)) 
			throw new RuntimeException("That Bug is not alive\n");
		else if ("x".equals(v) || "y".equals(v) || "angle".equals(v)) {
			return interp.bugs.get(bug).fetch(v);
		}
		else if (declaredBy(bug, v)) {
			return interp.bugs.get(bug).scopes.get(0).get(v);
		}
		else throw new RuntimeException("Access to that variable denied\n");
	}
	
	boolean declaredBy(String b, String v) {
		return interp.bugs.get(b).scopes.get(0).containsKey(v);
	}
	
	/**Returns distance from this Bug to another Bug.
	 * @param bug to find distance to
	 * @return distance to other bug
	 */
	double distance(String bug) {
		double otherX = fetchFrom(bug, "x");
		double otherY = fetchFrom(bug, "y");
		return Math.sqrt(Math.pow((x - otherX), 2) + Math.pow((y - otherY), 2));
	}
	
	/**Returns angle this Bug would have if directly facing another Bug.
	 * @param bug to find direction to
	 * @return angle this Bug must have to directly face bug
	 */
	double direction(String bug) {
		double rise = fetchFrom(bug, "y") - y;
		double run = fetchFrom(bug, "x") - x;
		if (run == 0) {
			// don't want a divide by 0 error
			if (rise >= 0) return 270.0; //remember it's dumb cg style
			return 90.0;
		}
		double arctan = (Math.atan(rise/run) * 180 / Math.PI);
		if (run < 0) return 180 - arctan; // quadrant 2 or 3; 90 - 270 deg
		else if (rise <= 0) return -arctan; // quad. 1; 0 - 90 deg.
		return 360 - arctan; // quad. 4; 270 - 360 deg.
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
			returnVal = 0.0;
			HashMap<String, Double> locals = new HashMap<String, Double>();
			scopes.push(locals);
			//get the values
			Tree<Token> t = tree.getChild(1); //var node
			Double[] varvalues = new Double[t.getNumberOfChildren()];
			for (int i = 0; i < t.getNumberOfChildren(); i++) {
				varvalues[i] = evaluate(t.getChild(i));
			}
			//have to find the function to get the names
			String fn = tree.getChild(0).getValue().value; // fn name
			if ("distance".equals(fn)) {
				return distance(tree.getChild(1).getValue().value);
			}
			else if ("direction".equals(fn)) {
				return direction(tree.getChild(1).getValue().value);
			}
			else if (functions.containsKey(fn)) t = functions.get(fn);
			else if (interp.functions.containsKey(fn)) t = interp.functions.get(fn);
			//TODO throw exception if not found?
			//then have to put everything in locals
			for (int i = 0; i < t.getNumberOfChildren(); i++) {
				locals.put(t.getChild(i).getValue().value, varvalues[i]);
			}
			interpret(tree.getChild(2));
			// TODO check
			return returnVal;
		default:
			// if it's a leaf/just a number
			if (tree.getNumberOfChildren() == 0) {
				try {
					return Double.parseDouble(tree.getValue().value);					
				} catch (Exception e) {
					String name = tree.getValue().value;
					if (name.contains(".")) {
						String var = name.substring(name.indexOf('.') + 1);
						name = name.substring(0, name.indexOf('.'));
						return fetchFrom(name, var);
					}
					return fetch(name);
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
		double x1, y1;
		switch (tree.getValue().value) {
//		case "program":
//			// TODO
//			
//			break;
//		case "Allbugs":
//			// TODO check
//			Tree<Token> t = tree.getChild(0); // var declaration list
//			for (int i = 0; i < t.getNumberOfChildren(); i++) {
//				interp.variables.put(t.getChild(i).getValue().value, 0.0);
//			}
//			t = tree.getChild(1); // fn declaration list
//			for (int i = 0; i < t.getNumberOfChildren(); i++) {
//				Tree<Token> fn = t.getChild(i);
//				interp.functions.put(fn.getChild(0).getValue().value, fn);
//			}
//			break;
		case "Bug":
			// Save the name of the Bug in an instance variable.
			name = tree.getChild(0).getValue().value; // ???
			interpret(tree.getChild(1)); // Interpret the list of var declarations.
			interpret(tree.getChild(2)); // Interpret the initialization block.
			mainProgram = tree.getChild(3); // Save the block (of commands).
			interpret(tree.getChild(4)); // Interpret the list of fn declarations
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
			// TODO update
			x1 = x;
			y1 = y;
			double tempD = evaluate(tree.getChild(0));
			double dx = Math.cos(angle * Math.PI / 180) * tempD;
			double dy = -(Math.sin(angle * Math.PI / 180) * tempD);
			store("x", x + dx);
			store("y", y + dy);
			// sin(270) = -1; 270 is down. This would be perfect if we were
			// using Cartesian coordinates, but with CG default (down == higher 
			// value), have to subtract sin from y instead of adding.
			interp.lines.add(new Command(x1, y1, x, y, color));
			interp.completeAction(this);
			break;
		case "moveto":
			// TODO update
			x1 = x;
			y1 = y;
			store("x", evaluate(tree.getChild(0)));
			store("y", evaluate(tree.getChild(1)));
			interp.lines.add(new Command(x1, y1, x, y, color));
			interp.completeAction(this);
			break;
		case "turn":
			// TODO update
			double newAngle = (angle + evaluate(tree.getChild(0))) % 360.0;
			store("angle", newAngle >= 0.0 ? newAngle : newAngle + 360.0);
			// anything to change for redrawing??
			interp.completeAction(this);
			break;
		case "turnto":
			// TODO update
			double newA = evaluate(tree.getChild(0)) % 360.0;
			store("angle", newA >= 0.0 ? newA : newA + 360.0);
			// anything to change for redrawing??
			interp.completeAction(this);
			break;
		case "return":
			// TODO check
			returnVal = evaluate(tree.getChild(0));
			scopes.pop();
			break;
		case "line":
			// TODO
			x1 = evaluate(tree.getChild(0));
			y1 = evaluate(tree.getChild(1));
			double x2 = evaluate(tree.getChild(2));
			double y2 = evaluate(tree.getChild(3));
			interp.lines.add(new Command(x1, y1, x2, y2, color));
			interp.completeAction(this);
			break;
		case "assign":
			String tempS = tree.getChild(0).getValue().value;
			if (!findVariable(tempS)) 
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
