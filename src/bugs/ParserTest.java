package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import tree.Tree;


/**Tests the , Parser class (a parser for the Bugs language used in CIT 594,
 * spring 2015).
 * project
 * @author Nicki Hoffman
 * @author Dave Matuszek
 *
 */
public class ParserTest {
    Parser parser;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParser() {
        parser = new Parser("");
        parser = new Parser("2 + 2");
    }
    
    //***---------Nicki's tests------------
    
    @Test
    public void testIsAction() {
    	Tree<Token> expected;
    	
		use("move 42 \n");
		assertTrue(parser.isAction());
		expected = tree("move", "42.0");
		assertStackTopEquals(expected);
		
		use("moveto 42, 43 \n");
		assertTrue(parser.isAction());
		expected = tree("moveto", "42.0", "43.0");
		assertStackTopEquals(expected);
		
		use("turn 42 \n");
		assertTrue(parser.isAction());
		expected = tree("turn", "42.0");
		assertStackTopEquals(expected);
		
		use("turnto 42 \n");
		assertTrue(parser.isAction());
		expected = tree("turnto", "42.0");
		assertStackTopEquals(expected);
		
		use("line 42, 43, 44, 45 \n");
		assertTrue(parser.isAction());
		expected = tree("line", "42.0", "43.0", "44.0", "45.0");
		assertStackTopEquals(expected);
		
		use("");
		assertFalse(parser.isAction());
		use("loop 42 \n");
		assertFalse(parser.isAction());
    }
        
    @Test
    public void testIsAllbugsCode() {
        use("Allbugs {\n var myvar1 \n var myvar2 \n "+
        		"define fn1 {\n}\n define fn2 {\n}\n }\n");
        assertTrue(parser.isAllbugsCode());
        assertStackTopEquals(tree("Allbugs", 
        					 tree("list", tree("var", "myvar1"), 
        							 	  tree("var", "myvar2")), 
        					 tree("list", tree("define", "fn1", tree("var"), tree("block")),
        							 	  tree("define", "fn2", tree("var"), tree("block")))));
        
        use("Allbugs {\n var myvar1 \n "+
        		"define fn1 {\n}\n }\n");
        assertTrue(parser.isAllbugsCode());
        assertStackTopEquals(tree("Allbugs", 
        					 tree("list", tree("var", "myvar1")), 
        					 tree("list", tree("define", "fn1", tree("var"), tree("block")))));
        
        use("Allbugs {\n }\n");
        assertTrue(parser.isAllbugsCode());
        assertStackTopEquals(tree("Allbugs", tree("list"), tree("list")));
        
        use("notAllbugs {\n }\n");
        assertFalse(parser.isAllbugsCode());
    }
        
    @Test
    public void testIsAssignmentStatement() {
        use("myVar = 42 \n");
        assertTrue(parser.isAssignmentStatement());
        assertStackTopEquals(tree("assign", "myVar", "42.0"));
        
        use("1myVar = 42 \n");
        assertFalse(parser.isAssignmentStatement());
    }
        
    @Test
    public void testIsBlock() {
		use("{\n }\n");
		assertTrue(parser.isBlock());
		assertStackTopEquals(tree("block"));
		
		use("{\n move 42 \n }\n");
		assertTrue(parser.isBlock());
		assertStackTopEquals(tree("block", tree("move", "42.0")));
		
		use("{\n move 42 \n turn 24 \n }\n");
		assertTrue(parser.isBlock());
		assertStackTopEquals(tree("block", tree("move", "42.0"), tree("turn", "24.0")));
		
		use("!{\n }\n");
		assertFalse(parser.isBlock());
    }
        
    @Test
    public void testIsBugDefinition() {
		use("Bug mybug {\n var myvar1 \n var myvar2 "+
				"\n initially {\n}\n turn 42 \n move 42 \n color darkGray \n "+
				"define fn1 {\n}\n define fn2 {\n}\n }\n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", 
				"mybug", 
				tree("list", tree("var", "myvar1"), tree("var", "myvar2")), 
				tree("initially", tree("block")),
				tree("block", tree("turn", "42.0"), tree("move", "42.0"), tree("color", "darkGray")),
				tree("list", tree("define", "fn1", tree("var"), tree("block")), tree("define", "fn2", tree("var"), tree("block")))));
		
		use("Bug mybug {\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n }\n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", 
				"mybug", 
				tree("list", tree("var", "myvar1")), 
				tree("initially", tree("block")),
				tree("block", tree("turn", "42.0"), tree("move", "42.0")),
				tree("list", tree("define", "fn1", tree("var"), tree("block")))));
		
		use("Bug mybug {\n turn 42 \n }\n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", 
									"mybug", 
									tree("list"), 
									tree("initially", tree("block")),
									tree("block", tree("turn", "42.0")),
									tree("list")));
		
		use("noBug mybug {\n turn 42 \n }\n");
		assertFalse(parser.isBugDefinition());
    }
        
    @Test
    public void testIsColorStatement() {
    	Tree<Token> expected;
    	
        use("< <= abc > >= 25 != = #");
        assertFalse(parser.isColorStatement());
        
        use("color darkGray \n");
        assertTrue(parser.isColorStatement());
        expected = tree("color", "darkGray");
        assertStackTopEquals(expected);
        
        use("color do \n"); //is ok for this part
        		// but will need to fail later bc not a color (have code ready)
        assertTrue(parser.isColorStatement());
        expected = tree("color", "do");
        
        use("notcolor darkGray \n");
        assertFalse(parser.isColorStatement());
    }
        
    @Test
    public void testIsCommand() {
		use("move 42 \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("move", "42.0"));
		use("moveto 42, 42 \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("moveto", "42.0", "42.0"));
		use("turn 42 \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("turn", "42.0"));
		use("turnto 42 \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("turnto", "42.0"));
		use("line 42, 42, 42, 42 \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("line", "42.0", "42.0", "42.0", "42.0"));
        
		use("myvar = 42 \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("assign", "myvar", "42.0"));
		use("loop {\n }\n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("loop", tree("block")));
		use("exit if 42 \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("exit", "42.0"));
		use("switch {\n }\n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("switch"));
		use("return 42 \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("return", "42.0"));
		use("do varname \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("call", "varname", tree("var")));
		use("color darkGray \n");
        assertTrue(parser.isCommand());
        assertStackTopEquals(tree("color", "darkGray"));
        
        use("");
        assertFalse(parser.isCommand());
    }
        
    @Test
    public void testIsComparator() {
    	use("< <= abc > >= 25 != = #");
    	assertTrue(parser.isComparator());
    	assertStackTopEquals(tree("<"));
    	assertTrue(parser.isComparator());
    	assertStackTopEquals(tree("<="));
    	assertFalse(parser.isComparator());
    	assertStackTopEquals(tree("<="));
    	parser.nextToken();
    	assertTrue(parser.isComparator());
    	assertStackTopEquals(tree(">"));
    	assertTrue(parser.isComparator());
    	assertStackTopEquals(tree(">="));
    	assertFalse(parser.isComparator());
    	assertStackTopEquals(tree(">="));
    	parser.nextToken();
    	assertTrue(parser.isComparator());
    	assertStackTopEquals(tree("!="));
    	assertTrue(parser.isComparator());
    	assertStackTopEquals(tree("="));
    	assertFalse(parser.isComparator());
    	assertStackTopEquals(tree("="));
    }
        
    @Test
    public void testIsDoStatement() {
    	Tree<Token> expected;
    	
		use("do something \n");
		assertTrue(parser.isDoStatement());
		expected = tree("call", "something", tree("var"));
		assertStackTopEquals(expected);
		
		use("do something (1, 2, 3) \n");
		assertTrue(parser.isDoStatement());
		expected = tree("call", "something", tree("var", "1.0", "2.0", "3.0"));
		assertStackTopEquals(expected);
		
		use("");
		assertFalse(parser.isDoStatement());
		use("(xyz + 3)");
		assertFalse(parser.isDoStatement());
    }
        
    @Test
    public void testIsEol() {
    	use("25\n30\n\n35\n\n\n\n40");
		assertFalse(parser.isEol());
		parser.nextToken();
		assertTrue(parser.isEol());
		assertFalse(parser.isEol());
		parser.nextToken();
		assertTrue(parser.isEol());
		assertFalse(parser.isEol());
		parser.nextToken();
		assertTrue(parser.isEol());
		assertFalse(parser.isEol());
		assertTrue(parser.stack.empty());
    }
        
    @Test
    public void testIsExitIfStatement() {
		use("exit if 42 \n");
		assertTrue(parser.isExitIfStatement());
		assertStackTopEquals(tree("exit", "42.0"));
		
		use("noexit if 42 \n");
		assertFalse(parser.isExitIfStatement());
    }
        
    @Test
    public void testIsFunctionCall() {
    	Tree<Token> expected;
    	
		use("moveto(10)");
		assertTrue(parser.isFunctionCall());
		expected = tree("call", "moveto", tree("var", "10.0"));
		assertStackTopEquals(expected);
		
		use("dosomething()");
		assertTrue(parser.isFunctionCall());
		expected = tree("call", "dosomething", tree("var"));
		assertStackTopEquals(expected);
		
		use("do(10 + 10, 20 * 20, 3 - 3)");
		assertTrue(parser.isFunctionCall());
		expected = tree("call", "do", tree("var", tree("+", "10.0", "10.0"),
				tree("*", "20.0", "20.0"), tree("-", "3.0", "3.0")));
		assertStackTopEquals(expected);
		
		use("()");
		assertFalse(parser.isFunctionCall());
		
		use("10()");
		assertFalse(parser.isFunctionCall());
    }
        
    @Test
    public void testIsFunctionDefinition() {
		use("define fn {\n}\n");
		assertTrue(parser.isFunctionDefinition());
		assertStackTopEquals(tree("define", "fn", tree("var"), tree("block")));
		
		use("define fn using myvar {\n}\n");
		assertTrue(parser.isFunctionDefinition());
		assertStackTopEquals(tree("define", "fn", tree("var", "myvar"), tree("block")));
		
		use("define fn using myvar1, myvar2 {\n}\n");
		assertTrue(parser.isFunctionDefinition());
		assertStackTopEquals(tree("define", "fn", tree("var", "myvar1", "myvar2"), tree("block")));
		
		use("define fn using myvar1, myvar2, myvar3 {\n}\n");
		assertTrue(parser.isFunctionDefinition());
		assertStackTopEquals(tree("define", "fn", tree("var", "myvar1", "myvar2", "myvar3"), tree("block")));
		
		use("nodefine fn {\n}\n");
		assertFalse(parser.isFunctionDefinition());
    }
        
    @Test
    public void testIsInitializationBlock() {
		use("initially {\n }\n");
		assertTrue(parser.isInitializationBlock());
		assertStackTopEquals(tree("initially", tree("block")));
		
		use("notinitially {\n }\n");
		assertFalse(parser.isInitializationBlock());
    }
        
    @Test
    public void testIsLineAction() {
		use("line 42, 43, 44, 45\n");
		assertTrue(parser.isLineAction());
		assertStackTopEquals(tree("line", "42.0", "43.0", "44.0", "45.0"));
		
		use("noline 42, 42, 42, 42\n");
		assertFalse(parser.isLineAction());
    }
        
    @Test
    public void testIsLoopStatement() {
		use("loop {\n}\n");
		assertTrue(parser.isLoopStatement());
		assertStackTopEquals(tree("loop", tree("block")));
		
		use("noloop {\n}\n");
		assertFalse(parser.isLoopStatement());
    }
        
    @Test
    public void testIsMoveAction() {
		use("move 42 \n");
		assertTrue(parser.isMoveAction());
		assertStackTopEquals(tree("move", "42.0"));
		
		use("nomove 42 \n");
		assertFalse(parser.isMoveAction());
    }
        
    @Test
    public void testIsMoveToAction() {
		use("moveto 42, 43 \n");
		assertTrue(parser.isMoveToAction());
		assertStackTopEquals(tree("moveto", "42.0", "43.0"));
		
		
		use("nomoveto 42, 43 \n");
		assertFalse(parser.isMoveToAction());
    }
        
    @Test
    public void testIsProgram() {
    	Tree<Token> expected; 
    	Tree<Token> bug1 = tree("Bug", 
    							"mybug1", 
    							tree("list"), 
    							tree("initially", tree("block")), 
    							tree("block", tree("turn", "42.0")), 
    							tree("list"));
    	Tree<Token> bug2 = tree("Bug", 
								"mybug2", 
								tree("list"), 
								tree("initially", tree("block")), 
								tree("block", tree("turn", "42.0")), 
								tree("list"));
    	Tree<Token> bug3 = tree("Bug", 
								"mybug3", 
								tree("list"), 
								tree("initially", tree("block")), 
								tree("block", tree("turn", "42.0")), 
								tree("list"));
    	Tree<Token> emptyAB = tree("Allbugs", tree("list"), tree("list"));
    	
		use("Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n"+
				"Bug mybug2 {\n turn 42 \n }\n"+
				"Bug mybug3 {\n turn 42 \n }\n");
		assertTrue(parser.isProgram());
		expected = tree("program", emptyAB, tree("list", bug1, bug2, bug3));
		assertStackTopEquals(expected);
		
		use("Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n"+
				"Bug mybug2 {\n turn 42 \n }\n");
		assertTrue(parser.isProgram());
		expected = tree("program", emptyAB, tree("list", bug1, bug2));
		assertStackTopEquals(expected);
		
		use("Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n");
		assertTrue(parser.isProgram());
		expected = tree("program", emptyAB, tree("list", bug1));
		assertStackTopEquals(expected);
		
		use("Bug mybug1 {\n turn 42 \n }\n");
		assertTrue(parser.isProgram());
		expected = tree("program", emptyAB, tree("list", bug1));
		assertStackTopEquals(expected);
		
		use("noBug mybug1 {\n turn 42 \n }\n");
		assertFalse(parser.isProgram());
		
		use("noAllbugs {\n}\n");
		assertFalse(parser.isProgram());
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsProgramEOFException() {
    	//the rule has been fixed so this breaks it now (EOF required)
		use("Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n"+
				"noBug mybug2 {\n turn 42 \n }\n");
		parser.isProgram();
    }
        
    @Test
    public void testIsReturnStatement() {
		use("return 42 \n");
		assertTrue(parser.isReturnStatement());
		assertStackTopEquals(tree("return", "42.0"));
		
		use("noreturn 42 \n");
		assertFalse(parser.isReturnStatement());
    }
        
    @Test
    public void testIsStatement() {
		use("myvar = 42 \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("assign", "myvar", "42.0"));
		
		use("loop {\n }\n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("loop", tree("block")));
		
		use("exit if 42 \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("exit", "42.0"));
		
		use("switch {\n }\n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("switch"));
		
		use("return 42 \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("return", "42.0"));
		
		use("do varname \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("call", "varname", tree("var")));
		
		use("color darkGray \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("color", "darkGray"));
		
		use("move 42 \n");
		assertFalse(parser.isStatement());
    }
        
    @Test
    public void testIsSwitchStatement() {
    	Tree<Token> expected;
		use("switch {\n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch");
		assertStackTopEquals(expected);
		
		use("switch {\n case 1 \n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", "1.0", tree("block")));
		assertStackTopEquals(expected);
		
		use("switch {\n case 1 \n move 42 \n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", "1.0", 
										tree("block", tree("move", "42.0"))));
		assertStackTopEquals(expected);
		
		use("switch {\n case 1 \n move 42 \n turn 24 \n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", "1.0", 
										tree("block", tree("move", "42.0"), 
													  tree("turn", "24.0"))));
		assertStackTopEquals(expected);

		use("switch {\n case 1 \n move 42 \n case two \n turn 24 \n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", "1.0", tree("block", tree("move", "42.0"))), 
								  tree("case", "two", tree("block", tree("turn", "24.0"))));
		assertStackTopEquals(expected);

		
		use("noswitch {\n }\n");
		assertFalse(parser.isSwitchStatement());
    }
        
    @Test
    public void testIsTurnAction() {
		use("turn 42 \n");
		assertTrue(parser.isTurnAction());
		assertStackTopEquals(tree("turn", "42.0"));
		
		use("noturn 42 \n");
		assertFalse(parser.isTurnAction());
    }
        
    @Test
    public void testIsTurnToAction() {
		use("turnto 42 \n");
		assertTrue(parser.isTurnToAction());
		assertStackTopEquals(tree("turnto", "42.0"));
		
		use("noturnto 42 \n");
		assertFalse(parser.isTurnToAction());
    }

    @Test
    public void testIsVarDeclaration() {
		use("var var1 \n");
		assertTrue(parser.isVarDeclaration());
		assertStackTopEquals(tree("var", "var1"));
		
		use("var var1, var2, var3 \n");
		assertTrue(parser.isVarDeclaration());
		assertStackTopEquals(tree("var", "var1", "var2", "var3"));
		
		use("novar var1 \n");
		assertFalse(parser.isVarDeclaration());
    }
    
    //***---------End of Nicki's tests-----------

    @Test
    public void testIsExpression() {
        Tree<Token> expected;
        
        use("250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(createNode("250.0"));
        
        use("hello");
        assertTrue(parser.isExpression());
        assertStackTopEquals(createNode("hello"));

        use("(xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", "xyz", "3.0"));

        use("a + b + c");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", tree("+", "a", "b"), "c"));

        use("a * b * c");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("*", tree("*", "a", "b"), "c"));

        use("3 * 12.5 - 7");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", tree("*", "3.0", "12.5"), createNode("7.0")));

        use("12 * 5 - 3 * 4 / 6 + 8");
        assertTrue(parser.isExpression());
        expected = tree("+",
                      tree("-",
                         tree("*", "12.0", "5.0"),
                         tree("/",
                            tree("*", "3.0", "4.0"),
                            "6.0"
                           )
                        ),
                      "8.0"
                     );
        assertStackTopEquals(expected);
                     
        use("12 * ((5 - 3) * 4) / 6 + (8)");
        assertTrue(parser.isExpression());
        expected = tree("+",
                      tree("/",
                         tree("*",
                            "12.0",
                            tree("*",
                               tree("-","5.0","3.0"),
                               "4.0")),
                         "6.0"),
                      "8.0");
        assertStackTopEquals(expected);
        
        use("");
        assertFalse(parser.isExpression());
        
        use("#");
        assertFalse(parser.isExpression());

        try {
            use("17 +");
            assertFalse(parser.isExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
        try {
            use("22 *");
            assertFalse(parser.isExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
    }

    @Test
    public void testUnaryOperator() {       
        use("-250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", "250.0"));
        
        use("+250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", "250.0"));
        
        use("- hello");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", "hello"));

        use("-(xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", tree("+", "xyz", "3.0")));

        use("(-xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", tree("-", "xyz"), "3.0"));

        use("+(-xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+",
                                        tree("+",
                                                   tree("-", "xyz"), "3.0")));
    }

    @Test
    public void testIsTerm() {        
        use("12");
        assertTrue(parser.isTerm());
        assertStackTopEquals(createNode("12.0"));
        
        use("12.5");
        assertTrue(parser.isTerm());
        assertStackTopEquals(createNode("12.5"));

        use("3*12");
        assertTrue(parser.isTerm());
        assertStackTopEquals(tree("*", "3.0", "12.0"));

        use("x * y * z");
        assertTrue(parser.isTerm());
        assertStackTopEquals(tree("*", tree("*", "x", "y"), "z"));
        
        use("20 * 3 / 4");
        assertTrue(parser.isTerm());
        assertEquals(tree("/", tree("*", "20.0", "3.0"), createNode("4.0")),
                     stackTop());

        use("20 * 3 / 4 + 5");
        assertTrue(parser.isTerm());
        assertEquals(tree("/", tree("*", "20.0", "3.0"), "4.0"),
                     stackTop());
        followedBy(parser, "+ 5");
        
        use("");
        assertFalse(parser.isTerm());
        followedBy(parser, "");
        
        use("#");
        assertFalse(parser.isTerm());followedBy(parser, "#");

    }

    @Test
    public void testIsFactor() {
        use("12");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("12.0"));

        use("hello");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("hello"));
        
        use("(xyz + 3)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("+", "xyz", "3.0"));
        
        use("12 * 5");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("12.0"));
        followedBy(parser, "* 5.0");
        
        use("17 +");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("17.0"));
        followedBy(parser, "+");

        use("");
        assertFalse(parser.isFactor());
        followedBy(parser, "");
        
        use("#");
        assertFalse(parser.isFactor());
        followedBy(parser, "#");
    }

    @Test
    public void testIsFactor2() {
        use("hello.world");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree(".", "hello", "world"));
        
        use("foo(bar)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                        tree("var", "bar")));
        
        use("foo(bar, baz)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                        tree("var", "bar", "baz")));
        
        use("foo(2*(3+4))");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                 tree("var",
                                     tree("*", "2.0",
                                         tree("+", "3.0", "4.0")))));
    }

    @Test
    public void testIsAddOperator() {
        use("+ - + $");
        assertTrue(parser.isAddOperator());
        assertTrue(parser.isAddOperator());
        assertTrue(parser.isAddOperator());
        assertFalse(parser.isAddOperator());
        followedBy(parser, "$");
    }

    @Test
    public void testIsMultiplyOperator() {
        use("* / $");
        assertTrue(parser.isMultiplyOperator());
        assertTrue(parser.isMultiplyOperator());
        assertFalse(parser.isMultiplyOperator());
        followedBy(parser, "$");
    }

    @Test
    public void testNextToken() {
        use("12 12.5 bogus switch + \n");
        assertEquals(new Token(Token.Type.NUMBER, "12.0"), parser.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "12.5"), parser.nextToken());
        assertEquals(new Token(Token.Type.NAME, "bogus"), parser.nextToken());
        assertEquals(new Token(Token.Type.KEYWORD, "switch"), parser.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "+"), parser.nextToken());
        assertEquals(new Token(Token.Type.EOL, "\n"), parser.nextToken());
        assertEquals(new Token(Token.Type.EOF, "EOF"), parser.nextToken());
    }
    
//  ----- "Helper" methods
    
    /**
     * Sets the <code>parser</code> instance to use the given string.
     * 
     * @param s The string to be parsed.
     */
    private void use(String s) {
        parser = new Parser(s);
    }
    
    /**
     * Returns the current top of the stack.
     *
     * @return The top of the stack.
     */
    private Object stackTop() {
        return parser.stack.peek();
    }
    
    /**
     * Tests whether the top element in the stack is correct.
     *
     * @return <code>true</code> if the top element of the stack is as expected.
     */
    private void assertStackTopEquals(Tree<Token> expected) {
        assertEquals(expected, stackTop());
    }
    
    /**
     * This method is given a String containing some or all of the
     * tokens that should yet be returned by the Tokenizer, and tests
     * whether the Tokenizer in fact has those Tokens. To succeed,
     * everything in the given String must still be in the Tokenizer,
     * but there may be additional (untested) Tokens to be returned.
     * This method is primarily to test whether Tokens are pushed
     * back appropriately.
     * @param parser TODO
     * @param expectedTokens The Tokens we expect to get from the Tokenizer.
     */
    private void followedBy(Parser parser, String expectedTokens) {
        int expectedType;
        int actualType;
        StreamTokenizer actual = parser.tokenizer;

        Reader reader = new StringReader(expectedTokens);
        StreamTokenizer expected = new StreamTokenizer(reader);

        try {
            while (true) {
                expectedType = expected.nextToken();
                if (expectedType == StreamTokenizer.TT_EOF) break;
                actualType = actual.nextToken();
                assertEquals(typeName(expectedType), typeName(actualType));
                if (actualType == StreamTokenizer.TT_WORD) {
                    assertEquals(expected.sval, actual.sval);
                }
                else if (actualType == StreamTokenizer.TT_NUMBER) {
                    assertEquals(expected.nval, actual.nval, 0.001);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String typeName(int type) {
        switch(type) {
            case StreamTokenizer.TT_EOF: return "EOF";
            case StreamTokenizer.TT_EOL: return "EOL";
            case StreamTokenizer.TT_WORD: return "WORD";
            case StreamTokenizer.TT_NUMBER: return "NUMBER";
            default: return "'" + (char)type + "'";
        }
    }
    
    /**
     * Returns a Tree node consisting of a single leaf; the
     * node will contain a Token with a String as its value. <br>
     * Given a Tree, return the same Tree.<br>
     * Given a Token, return a Tree with the Token as its value.<br>
     * Given a String, make it into a Token, return a Tree
     * with the Token as its value.
     * 
     * @param value A Tree, Token, or String from which to
              construct the Tree node.
     * @return A Tree leaf node containing a Token whose value
     *         is the parameter.
     */
    private Tree<Token> createNode(Object value) {
        if (value instanceof Tree) {
            return (Tree) value;
        }
        if (value instanceof Token) {
            return new Tree<Token>((Token) value);
        }
        else if (value instanceof String) {
            return new Tree<Token>(new Token((String) value));
        }
        assert false: "Illegal argument: tree(" + value + ")";
        return null; 
    }
    
    /**
     * Builds a Tree that can be compared with the one the
     * Parser produces. Any String or Token arguments will be
     * converted to Tree nodes containing Tokens.
     * 
     * @param op The String value to use in the Token in the root.
     * @param children The objects to be made into children.
     * @return The resultant Tree.
     */
    private Tree<Token> tree(String op, Object... children) {
        Tree<Token> tree = new Tree<Token>(new Token(op));
        for (int i = 0; i < children.length; i++) {
            tree.addChild(createNode(children[i]));
        }
        return tree;
    }
}
