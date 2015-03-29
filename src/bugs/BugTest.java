package bugs;

import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import tree.Tree;

/**Tests the Bug interpreter class.
 * @author Nicki Hoffman
 *
 */
public class BugTest {
	Bug b;
	Parser p;

	@Before
	public void setUp() throws Exception {
		b = new Bug("Floyd");	// :)
	}
	
	//------------Tests for my helper methods
	
	@Test
	public void testGetAndSetName() {
		assertEquals("Floyd", b.getBugName());
		b.setBugName("Imogen");
		assertEquals("Imogen", b.getBugName());
	}
	
	@Test
	public void testGetAndSetColor() {
		assertEquals(Color.BLACK, b.getColor());
		b.setColor(Color.GREEN);
		assertEquals(Color.GREEN, b.getColor());
	}
	
	@Test
	public void testGetAndSetX() {
		assertEquals(0.0, b.getX(), Bug.e);
		b.setX(100.0);
		assertEquals(100.0, b.getX(), Bug.e);
	}
	
	@Test
	public void testGetAndSetY() {
		assertEquals(0.0, b.getY(), Bug.e);
		b.setY(100.0);
		assertEquals(100.0, b.getY(), Bug.e);
	}
	
	@Test
	public void testGetAndSetAngle() {
		assertEquals(0.0, b.getAngle(), Bug.e);
		b.setAngle(270.0);
		assertEquals(270.0, b.getAngle(), Bug.e);
	}
	
	@Test
	public void testGetAndSetExitLoop() {
		assertFalse(b.getExitLoop());
		b.setExitLoop(true);
		assertTrue(b.getExitLoop());
	}
	
	//-------------End of my helper method tests
	//-------------Primary method tests below
	
	@Test
	public void testDirection() {
		Bug c = new Bug("Mary");
		Interpreter i = new Interpreter();
		i.bugs.put("Mary", c);
		b.interp = i;
		c.setX(2.0);	// 0 degrees
		assertEquals(0.0, b.direction(c.name), Bug.e);
		c.setY(-2.0);	// 45 degrees
		assertEquals(45.0, b.direction(c.name), Bug.e);
		c.setX(0.0);	// 90 degrees
		assertEquals(90.0, b.direction(c.name), Bug.e);
		c.setX(-2.0);	// 135 degrees
		assertEquals(135.0, b.direction(c.name), Bug.e);
		c.setY(0.0);	// 180 degrees
		assertEquals(180.0, b.direction(c.name), Bug.e);
		c.setY(2.0);	// 225 degrees
		assertEquals(225.0, b.direction(c.name), Bug.e);
		c.setX(0.0);	// 270 degrees
		assertEquals(270.0, b.direction(c.name), Bug.e);
		c.setX(2.0);	// 315 degrees
		assertEquals(315.0, b.direction(c.name), Bug.e);
	}
	
	@Test
	public void testDistance() {
		Bug c = new Bug("Mary");
		Interpreter i = new Interpreter();
		i.bugs.put("Mary", c);
		b.interp = i;
		c.setX(8.0);
		assertEquals(8.0, b.distance(c.name), Bug.e);
		c.setX(-4.0);
		assertEquals(4.0, b.distance(c.name), Bug.e);
		c.setX(0.0);
		c.setY(-3.0);
		assertEquals(3.0, b.distance(c.name), Bug.e);
		c.setY(12.0);
		assertEquals(12.0, b.distance(c.name), Bug.e);
	}

	/**
	 * Test method for store and fetch methods
	 */
	@Test
	public void testStoreAndFetch() {
		b.variables.put("myvar", 0.0);
		b.store("x", 1.5);
		assertEquals(1.5, b.fetch("x"), Bug.e);
		b.store("y", 4.2);
		assertEquals(4.2, b.fetch("y"), Bug.e);
		b.store("angle", 270.0);
		assertEquals(270.0, b.fetch("angle"), Bug.e);
		b.store("myvar", -3.14);
		assertEquals(-3.14, b.fetch("myvar"), Bug.e);
	}

	/**
	 * Test method for evaluate method's addition case
	 */
	@Test
	public void testAdd() {
		p = new Parser("9 + 16");
		p.isExpression();
		assertEquals(25.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("+(16 + 9) + 7");
		p.isExpression();
		assertEquals(32.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("9 + 7 + +16");
		p.isExpression();
		assertEquals(32.0, b.evaluate(p.stack.pop()), Bug.e);
	}
	
	/**
	 * Test method for evaluate method's subtract case
	 */
	@Test
	public void testSubtract() {
		p = new Parser("9 - 16");
		p.isExpression();
		assertEquals(-7.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("-(16 - 9) - 7");
		p.isExpression();
		assertEquals(-14.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("9 - 7 - -16");
		p.isExpression();
		assertEquals(18.0, b.evaluate(p.stack.pop()), Bug.e);
	}
	
	/**
	 * Test method for evaluate method's multiply case
	 */
	@Test
	public void testMultiply() {
		p = new Parser("10 * 0");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("10 * 1");
		p.isExpression();
		assertEquals(10.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("12 * 13");
		p.isExpression();
		assertEquals(156.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("2 * -3");
		p.isExpression();
		assertEquals(-6.0, b.evaluate(p.stack.pop()), Bug.e);
	}
	
	/**
	 * Test method for evaluate method's division case
	 */
	@Test
	public void testDivide() {
		p = new Parser("0 / 1");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("4 / 1");
		p.isExpression();
		assertEquals(4.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("4 / 2");
		p.isExpression();
		assertEquals(2.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("2 / 4");
		p.isExpression();
		assertEquals(0.5, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("-4 / 2");
		p.isExpression();
		assertEquals(-2.0, b.evaluate(p.stack.pop()), Bug.e);
	}
	
	/**
	 * Test method for evaluate method's less-than case
	 */
	@Test
	public void testLT() {
		p = new Parser("0 < 0");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e); // read: false

		p = new Parser("1 < 2");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e); // read: true
		
		p = new Parser("0 < 0.01");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("0 < 0.001");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e);
	}
	
	/**
	 * Test method for evaluate method's less-than-or-equal-to case
	 */
	@Test
	public void testLEQ() {
		p = new Parser("0 <= 0");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e); // read: true

		p = new Parser("1 <= 2");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("0.001 <= 0");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("2 <= 1");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e); // read: false
	}
	
	/**
	 * test method for evaluate method's equal-to case
	 */
	@Test
	public void testEQ() {
		p = new Parser("0 = 0");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e); // read: true

		p = new Parser("1 = 2");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e); // read: false
		
		p = new Parser("0.001 = 0");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("0 = 0.001");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("2 = 1");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e);
	}
	
	/**
	 * Test method for evaluate method's greater-than-or-equal-to case
	 */
	@Test
	public void testGEQ() {
		p = new Parser("0 >= 0");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e); // read: true

		p = new Parser("1 >= 2");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e); // read: false
		
		p = new Parser("0 >= 0.001");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("2 >= 1");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e);
	}
	
	/**
	 * Test method for evaluate method's greater-than case
	 */
	@Test
	public void testGT() {
		p = new Parser("0 > 0");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e); // read: false

		p = new Parser("1 > 2");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("0 > 0.001");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("2 > 1");
		p.isExpression();
		assertEquals(1.0, b.evaluate(p.stack.pop()), Bug.e); // read: true
	}
	
	/**
	 * Test method for evaluate method's default case (just a number)
	 */
	@Test
	public void testEvaluateDefault() {
		p = new Parser("0");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e);

		p = new Parser("2.45");
		p.isExpression();
		assertEquals(2.45, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("(10)");
		p.isExpression();
		assertEquals(10.0, b.evaluate(p.stack.pop()), Bug.e);
		
		p = new Parser("x");
		p.isExpression();
		assertEquals(0.0, b.evaluate(p.stack.pop()), Bug.e);
	}
	
//	/**
//	 * Test method for evaluate method's call case
//	 */
//	@Test
//	public void testCall() {
//		fail("Not yet implemented");
//	}
	
//	/**
//	 * Test method for interpret method's program case
//	 */
//	@Test
//	public void testProgram() {
//		fail("Not yet implemented");
//	}
//	
//	/**
//	 * Test method for interpret method's Allbugs case
//	 */
//	@Test
//	public void testAllbugs() {
//		fail("Not yet implemented");
//	}
	
	/**
	 * Test method for interpret method's bug definition case
	 */
	@Test
	public void testBug() {
		//changed tests for interpreter part 2 since completed interpret() for
		//bug def'n case
		p = new Parser("Bug Eratosthenes {\n turn 42 \n }\n");
		p.isBugDefinition();
		Tree<Token> prog = p.stack.peek().getChild(3); //block of cmds that==bug's program
		b.interpret(p.stack.pop());
		b.mainProgram.equals(prog);
		assertEquals("Eratosthenes", b.getBugName());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);
		assertEquals(0.0, b.getAngle(), Bug.e);
		assertEquals(Color.BLACK, b.getColor());
		assertEquals(0, b.variables.size());
		assertEquals(0, b.functions.size());
		
		p = new Parser("Bug Aristophanes {\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n moveto 41, 42 \n define fn1 {\n}\n }\n");
		p.isBugDefinition();
		prog = p.stack.peek().getChild(3);
		b.interpret(p.stack.pop());
		b.mainProgram.equals(prog);
		assertEquals("Aristophanes", b.getBugName());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);
		assertEquals(0.0, b.getAngle(), Bug.e);
		assertEquals(Color.BLACK, b.getColor());
		assertEquals(1, b.variables.size());
		assertTrue(b.variables.containsKey("myvar1"));
		assertEquals(1, b.functions.size());
		
		p = new Parser("Bug Thales {\n var myvar1 \n var myvar2 "+
				"\n initially {\n turnto 0 \n }\n turn 42 \n moveto 42, 43 \n "+
				"color darkGray \n exit if 1 \n color purple \n"+
				"define fn1 {\n}\n define fn2 {\n}\n }\n");
		p.isBugDefinition();
		prog = p.stack.peek().getChild(3);
		b.interpret(p.stack.pop());
		b.mainProgram.equals(prog);
		assertEquals("Thales", b.getBugName());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);
		assertEquals(0.0, b.getAngle(), Bug.e);
		assertEquals(Color.BLACK, b.getColor());
		assertEquals(2, b.variables.size());
		assertTrue(b.variables.containsKey("myvar2"));
		assertEquals(2, b.functions.size());
	}
	
//	/**
//	 * Test method for interpret method's list case
//	 */
//	@Test
//	public void testList() {
//		fail("Not yet implemented"); // how to test since only created 
//	}								 // under other things like bug def'ns?
	
	/**
	 * Test method for interpret method's block case
	 */
	@Test
	public void testBlock() {
		p = new Parser("{\n }\n");
		p.isBlock();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);
		assertEquals(0.0, b.getAngle(), Bug.e);
		assertEquals(Color.BLACK, b.getColor());
		assertEquals(0, b.variables.size());
		assertEquals(0, b.functions.size());
		
		p = new Parser("{\n moveto 10, -10 \n }\n");
		p.isBlock();
		b.interpret(p.stack.pop());
		assertEquals(10.0, b.getX(), Bug.e);
		assertEquals(-10.0, b.getY(), Bug.e);
		
		p = new Parser("{\n turnto -10 \n color orange \n }\n");
		p.isBlock();
		b.interpret(p.stack.pop());
		assertEquals(350.0, b.getAngle(), Bug.e);
		assertEquals(Color.ORANGE, b.getColor());
		
		p = new Parser("{\n exit if 1 \n color blue \n }\n");
		p.isBlock();
		b.interpret(p.stack.pop());
		assertEquals(Color.ORANGE, b.getColor());
	}
	
	/**
	 * Test method for interpret method's var case
	 */
	@Test
	public void testVar() {
		p = new Parser("var myvar1 \n");
		p.isVarDeclaration();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.fetch("myvar1"), Bug.e);
		
		p = new Parser("var myvar1, myvar2, myvar3, myvar3 \n");
		p.isVarDeclaration();
		b.interpret(p.stack.pop());
		assertEquals(3, b.variables.size());
		assertEquals(0.0, b.fetch("myvar1"), Bug.e);
		assertEquals(0.0, b.fetch("myvar2"), Bug.e);
		assertEquals(0.0, b.fetch("myvar3"), Bug.e);
	}
	
	/**
	 * Test method for interpret method's initially block case
	 */
	@Test
	public void testInitially() {
		p = new Parser("initially {\n }\n");
		p.isInitializationBlock();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);
		assertEquals(0.0, b.getAngle(), Bug.e);
		assertEquals(Color.BLACK, b.getColor());
		assertEquals(0, b.variables.size());
		assertEquals(0, b.functions.size());
		
		p = new Parser("initially {\n moveto 10, -10 \n }\n");
		p.isInitializationBlock();
		b.interpret(p.stack.pop());
		assertEquals(10.0, b.getX(), Bug.e);
		assertEquals(-10.0, b.getY(), Bug.e);
		
		p = new Parser("initially {\n turnto -10 \n color orange \n }\n");
		p.isInitializationBlock();
		b.interpret(p.stack.pop());
		assertEquals(350.0, b.getAngle(), Bug.e);
		assertEquals(Color.ORANGE, b.getColor());
		
		p = new Parser("initially {\n exit if 1 \n color blue \n }\n");
		p.isInitializationBlock();
		b.interpret(p.stack.pop());
		assertEquals(Color.ORANGE, b.getColor());
	}
	
	/**
	 * Test method for interpret method's move case
	 */
	@Test
	public void testMove() {
		b = new Bug("Eratosthenes");
		b.setAngle(0.0);
		p = new Parser("move 2 \n");
		p.isMoveAction();
		b.interpret(p.stack.pop());
		assertEquals(2.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);

		b.setAngle(180.0);
		p = new Parser("move 2 \n");
		p.isMoveAction();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);

		b.setAngle(90.0);
		p = new Parser("move 2 \n");
		p.isMoveAction();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(-2.0, b.getY(), Bug.e);

		b.setAngle(270.0);
		p = new Parser("move 2 \n");
		p.isMoveAction();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);

		b.setAngle(225.0);
		p = new Parser("move 2 \n");
		p.isMoveAction();
		b.interpret(p.stack.pop());
		assertEquals(-Math.sqrt(2.0), b.getX(), Bug.e);
		assertEquals(Math.sqrt(2.0), b.getY(), Bug.e);
	}
	
	/**
	 * Test method for interpret method's moveto case
	 */
	@Test
	public void testMoveto() {
		p = new Parser("moveto 4, 5 \n");
		p.isMoveToAction();
		b.interpret(p.stack.pop());
		assertEquals(4.0, b.getX(), Bug.e);
		assertEquals(5.0, b.getY(), Bug.e);
		
		p = new Parser("moveto -4, -5 \n");
		p.isMoveToAction();
		b.interpret(p.stack.pop());
		assertEquals(-4.0, b.getX(), Bug.e);
		assertEquals(-5.0, b.getY(), Bug.e);
		
		p = new Parser("moveto (4 * 1), (5 / 2) \n");
		p.isMoveToAction();
		b.interpret(p.stack.pop());
		assertEquals(4.0, b.getX(), Bug.e);
		assertEquals(2.5, b.getY(), Bug.e);
	}
	
	/**
	 * Test method for interpret method's turn case
	 */
	@Test
	public void testTurn() {
		p = new Parser("turn 1 \n");
		p.isTurnAction();
		b.interpret(p.stack.pop());
		assertEquals(1.0, b.getAngle(), Bug.e);
		
		p = new Parser("turn -1 \n");
		p.isTurnAction();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.getAngle(), Bug.e);
		
		p = new Parser("turn (3 + 1) \n");
		p.isTurnAction();
		b.interpret(p.stack.pop());
		assertEquals(4.0, b.getAngle(), Bug.e);
		
		p = new Parser("turn 400 \n");
		p.isTurnAction();
		b.interpret(p.stack.pop());
		assertEquals(44.0, b.getAngle(), Bug.e);
		
		p = new Parser("turn -721 \n");
		p.isTurnAction();
		b.interpret(p.stack.pop());
		assertEquals(43.0, b.getAngle(), Bug.e);
	}
	
	/**
	 * Test method for interpret method's turnto case
	 */
	@Test
	public void testTurnto() {
		p = new Parser("turnto 1 \n");
		p.isTurnToAction();
		b.interpret(p.stack.pop());
		assertEquals(1.0, b.getAngle(), Bug.e);
		
		p = new Parser("turnto -1 \n");
		p.isTurnToAction();
		b.interpret(p.stack.pop());
		assertEquals(359.0, b.getAngle(), Bug.e);
		
		p = new Parser("turnto (3 + 1) \n");
		p.isTurnToAction();
		b.interpret(p.stack.pop());
		assertEquals(4.0, b.getAngle(), Bug.e);
		
		p = new Parser("turnto 400 \n");
		p.isTurnToAction();
		b.interpret(p.stack.pop());
		assertEquals(40.0, b.getAngle(), Bug.e);
		
		p = new Parser("turnto -721 \n");
		p.isTurnToAction();
		b.interpret(p.stack.pop());
		assertEquals(359.0, b.getAngle(), Bug.e);
	}
	
//	/**
//	 * Test method for interpret method's return statement case
//	 */
//	@Test
//	public void testReturn() {
//		fail("Not yet implemented");
//	}
//	
//	/**
//	 * Test method for interpret method's line action case
//	 */
//	@Test
//	public void testLine() {
//		fail("Not yet implemented");
//	}
	
	/**
	 * Test method for interpret method's assignment statement case
	 */
	@Test
	public void testAssign() {
		b.variables.put("myvar", 0.0);
		
		p = new Parser("x = 15 \n");
		p.isAssignmentStatement();
		b.interpret(p.stack.pop());
		assertEquals(15.0, b.getX(), Bug.e);
		
		p = new Parser("y = -(10 + 2) \n");
		p.isAssignmentStatement();
		b.interpret(p.stack.pop());
		assertEquals(-12.0, b.getY(), Bug.e);
		
		p = new Parser("angle = -2 * -3 \n");
		p.isAssignmentStatement();
		b.interpret(p.stack.pop());
		assertEquals(6.0, b.getAngle(), Bug.e);
		
		p = new Parser("myvar = 13 \n");
		p.isAssignmentStatement();
		b.interpret(p.stack.pop());
		assertEquals(13.0, b.fetch("myvar"), Bug.e);
	}
	
	/**
	 * Test method for interpret method's loop statement case
	 */
	@Test
	public void testLoop() {
		p = new Parser("loop {\n exit if 1 \n }\n");
		p.isLoopStatement();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);
		assertEquals(0.0, b.getAngle(), Bug.e);
		assertEquals(Color.BLACK, b.getColor());
		assertEquals(0, b.variables.size());
		assertEquals(0, b.functions.size());
		
		p = new Parser("loop {\n move 10 \n exit if x > 20 \n }\n");
		p.isLoopStatement();
		b.interpret(p.stack.pop());
		assertEquals(30.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);
		
		p = new Parser("loop {\n turn -10 \n color orange \n move 1 \n"+ 
					   "exit if x > 100 \n exit if angle < 330 \n }\n");
		p.isLoopStatement();
		b.interpret(p.stack.pop());
		assertEquals(320.0, b.getAngle(), Bug.e);
		assertEquals(Color.ORANGE, b.getColor());
		assertTrue(b.getX() > 2.0);
		
		p = new Parser("loop {\n exit if 1 \n color blue \n }\n");
		p.isLoopStatement();
		b.interpret(p.stack.pop());
		assertEquals(Color.ORANGE, b.getColor());
	}
	
	/**
	 * Test method for interpret method's exit if statement case
	 */
	@Test
	public void testExit() {
		p = new Parser("exit if 1 \n");
		p.isExitIfStatement();
		b.interpret(p.stack.pop());
		assertTrue(b.getExitLoop());
		
		p = new Parser("exit if 0 \n"); // this method only makes value true
		p.isExitIfStatement();			// doesn't ever set it false;
		b.interpret(p.stack.pop());		// is that right?
		assertTrue(b.getExitLoop());
		
		b.setExitLoop(false);
		p = new Parser("exit if 0 \n");
		p.isExitIfStatement();
		b.interpret(p.stack.pop());
		assertFalse(b.getExitLoop());
		
		p = new Parser("exit if 1 - 1 \n");
		p.isExitIfStatement();
		b.interpret(p.stack.pop());
		assertFalse(b.getExitLoop());
		
		p = new Parser("exit if -1 \n");
		p.isExitIfStatement();
		b.interpret(p.stack.pop());
		assertTrue(b.getExitLoop());
	}
	
	/**
	 * Test method for interpret method's switch statement case
	 * (and the evaluate method's case "case", since it falls under switch)
	 */
	@Test
	public void testSwitchAndCase() {
		p = new Parser("switch {\n }\n");
		p.isSwitchStatement();
		b.interpret(p.stack.pop());
		assertEquals(0.0, b.getX(), Bug.e);
		assertEquals(0.0, b.getY(), Bug.e);
		assertEquals(0.0, b.getAngle(), Bug.e);
		assertEquals(Color.BLACK, b.getColor());
		assertEquals(0, b.variables.size());
		assertEquals(0, b.functions.size());
		
		p = new Parser("switch {\n case x = y \n x = 1 \n }\n");
		p.isSwitchStatement();
		b.interpret(p.stack.pop());
		assertEquals(1.0, b.getX(), Bug.e);
		
		p = new Parser("switch {\n case x = -2 \n x = 2 \n " +
					   "case x * y \n x = 3 \n case x * -x \n x = 2 \n }\n");
		p.isSwitchStatement();
		b.interpret(p.stack.pop());
		assertEquals(2.0, b.getX(), Bug.e);
	}
	
	/**
	 * Test method for interpret method's color statement case
	 */
	@Test
	public void testColor() {
		p = new Parser("color blue \n");
		p.isColorStatement();
		b.interpret(p.stack.pop());
		assertEquals(Color.BLUE, b.getColor());
		
		p = new Parser("color none \n");
		p.isColorStatement();
		b.interpret(p.stack.pop());
		assertEquals(null, b.getColor());
		
		p = new Parser("color brown \n");
		p.isColorStatement();
		b.interpret(p.stack.pop());
		assertEquals(new Color(64, 32, 0), b.getColor());
		
		p = new Parser("color purple \n");
		p.isColorStatement();
		b.interpret(p.stack.pop());
		assertEquals(new Color(80, 0, 64), b.getColor());
	}
	
	/**
	 * Test method for interpret method's function definition case
	 */
	@Test
	public void testFunction() {
		p = new Parser("define fn1 {\n }\n");
		p.isFunctionDefinition();
		b.interpret(p.stack.get(0));
		assertEquals(p.stack.pop(), b.functions.get("fn1"));
		
		p = new Parser("define fn2 using myvar {\n move myvar \n }\n");
		p.isFunctionDefinition();
		b.interpret(p.stack.get(0));
		assertEquals(p.stack.pop(), b.functions.get("fn2"));
		assertTrue(b.functions.containsKey("fn1"));
	}

}
