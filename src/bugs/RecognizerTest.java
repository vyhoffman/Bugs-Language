/**
 * 
 */
package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

/**Tests the Recognizer class.
 * @author Nicki Hoffman
 *
 */
public class RecognizerTest {
	
    Recognizer r0, r1, r2, r3, r4, r5, r6, r7, r8;
    
    /**
     * Constructor for RecognizerTest.
     */
    public RecognizerTest() {
        r0 = new Recognizer("2 + 2");
        r1 = new Recognizer("");
    }
    
    
    /**
     * Resets variables r1 through r7 to statements, r8 to non-statement.
     */
    public void getStatements() {
		r1 = new Recognizer("myvar = 42 \n");
		r2 = new Recognizer("loop {\n }\n");
		r3 = new Recognizer("exit if 42 \n");
		r4 = new Recognizer("switch {\n }\n");
		r5 = new Recognizer("return 42 \n");
		r6 = new Recognizer("do varname \n");
		r7 = new Recognizer("color darkGray \n");
		r8 = new Recognizer("move 42 \n");
    }
    
    
    /**
     * Resets instance variables r1 through r5 to actions, r6 to non-action.
     */
    public void getActions() {
		r1 = new Recognizer("move 42 \n");
		r2 = new Recognizer("moveto 42, 42 \n");
		r3 = new Recognizer("turn 42 \n");
		r4 = new Recognizer("turnto 42 \n");
		r5 = new Recognizer("line 42, 42, 42, 42 \n");
		r6 = new Recognizer("loop 42 \n");
    }


    /**
     * Setup method for RecognizerTest.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        r0 = new Recognizer("");
        r1 = new Recognizer("250");
        r2 = new Recognizer("hello");
        r3 = new Recognizer("(xyz + 3)");
        r4 = new Recognizer("12 * 5 - 3 * 4 / 6 + 8");
        r5 = new Recognizer("12 * ((5 - 3) * 4) / 6 + (8)");
        r6 = new Recognizer("17 +");
        r7 = new Recognizer("22 *");
        r8 = new Recognizer("#");
    }

	/**
	 * Test method for {@link bugs.Recognizer#Recognizer(java.lang.String)}.
	 */
    @Test
    public void testRecognizer() {
        r0 = new Recognizer("");
        r1 = new Recognizer("2 + 2");
    }

	/**
	 * Test method for {@link bugs.Recognizer#isArithmeticExpression()}.
	 */
    @Test
    public void testIsArithmeticExpression() {
        assertTrue(r1.isArithmeticExpression());
        assertTrue(r2.isArithmeticExpression());
        assertTrue(r3.isArithmeticExpression());
        assertTrue(r4.isArithmeticExpression());
        assertTrue(r5.isArithmeticExpression());

        assertFalse(r0.isArithmeticExpression());
        assertFalse(r8.isArithmeticExpression());

        try {
            assertFalse(r6.isArithmeticExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
        try {
            assertFalse(r7.isArithmeticExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
    }

	/**
	 * Test method for {@link bugs.Recognizer#isArithmeticExpression()}.
	 */
    @Test
    public void testIsArithmeticExpressionWithUnaryMinus() {
        assertTrue(new Recognizer("-5").isArithmeticExpression());
        assertTrue(new Recognizer("12+(-5*10)").isArithmeticExpression());
        assertTrue(new Recognizer("+5").isArithmeticExpression());
        assertTrue(new Recognizer("12+(+5*10)").isArithmeticExpression());
    }

	/**
	 * Test method for {@link bugs.Recognizer#isTerm()}.
	 */
    @Test
    public void testIsTerm() {
        assertFalse(r0.isTerm()); // ""
        
        assertTrue(r1.isTerm()); // "250"
        
        assertTrue(r2.isTerm()); // "hello"
        
        assertTrue(r3.isTerm()); // "(xyz + 3)"
        followedBy(r3, "");
        
        assertTrue(r4.isTerm());  // "12 * 5 - 3 * 4 / 6 + 8"
        assertEquals(new Token(Token.Type.SYMBOL, "-"), r4.nextToken());
        assertTrue(r4.isTerm());
        followedBy(r4, "+ 8");

        assertTrue(r5.isTerm());  // "12 * ((5 - 3) * 4) / 6 + (8)"
        assertEquals(new Token(Token.Type.SYMBOL, "+"), r5.nextToken());
        assertTrue(r5.isTerm());
        followedBy(r5, "");
    }

	/**
	 * Test method for {@link bugs.Recognizer#isFactor()}.
	 */
    @Test
    public void testIsFactor() {
        assertTrue(r1.isFactor());
        assertTrue(r2.isFactor());
        assertTrue(r3.isFactor());
        assertTrue(r4.isFactor()); followedBy(r4, "* 5 - 3 * 4 / 6 + 8");
        assertTrue(r5.isFactor()); followedBy(r5, "* ((5");
        assertTrue(r6.isFactor()); followedBy(r6, "+");
        assertTrue(r7.isFactor()); followedBy(r7, "*");

        assertFalse(r0.isFactor());
        assertFalse(r8.isFactor()); followedBy(r8, "#");

        Recognizer r = new Recognizer("foo()");
        assertTrue(r.isFactor());
        r = new Recognizer("bar(5, abc, 2+3)+");
        assertTrue(r.isFactor()); followedBy(r, "+");

        r = new Recognizer("foo.bar$");
        assertTrue(r.isFactor()); followedBy(r, "$");
        
        r = new Recognizer("123.123");
        assertEquals(new Token(Token.Type.NUMBER, "123.123"), r.nextToken());
        
        r = new Recognizer("5");
        assertEquals(new Token(Token.Type.NUMBER, "5.0"), r.nextToken());
    }
    
	/**
	 * Test method for {@link bugs.Recognizer#isParameterList()}.
	 */
    @Test
    public void testIsParameterList() {
        Recognizer r = new Recognizer("() $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("(5) $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("(bar, x+3) $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
    }

	/**
	 * Test method for {@link bugs.Recognizer#isAddOperator()}.
	 */
    @Test
    public void testIsAddOperator() {
        Recognizer r = new Recognizer("+ - $");
        assertTrue(r.isAddOperator());
        assertTrue(r.isAddOperator());
        assertFalse(r.isAddOperator());
        followedBy(r, "$");
    }

	/**
	 * Test method for {@link bugs.Recognizer#isMultiplyOperator()}.
	 */
    @Test
    public void testIsMultiplyOperator() {
        Recognizer r = new Recognizer("* / $");
        assertTrue(r.isMultiplyOperator());
        assertTrue(r.isMultiplyOperator());
        assertFalse(r.isMultiplyOperator());
        followedBy(r, "$");
    }

	/**
	 * Test method for {@link bugs.Recognizer#isVariable()}.
	 */
    @Test
    public void testIsVariable() {
        Recognizer r = new Recognizer("foo 23 bar +");
        assertTrue(r.isVariable());
        
        assertFalse(r.isVariable());
        assertTrue(r.isFactor());
        
        assertTrue(r.isVariable());
        
        assertFalse(r.isVariable());
        assertTrue(r.isAddOperator());
    }

	/**
	 * Test method for symbol.
	 */
    @Test
    public void testSymbol() {
        Recognizer r = new Recognizer("++");
        assertEquals(new Token(Token.Type.SYMBOL, "+"), r.nextToken());
    }

	/**
	 * Test method for {@link bugs.Recognizer#nextTokenMatches(Token.Type)}.
	 */
    @Test
    public void testNextTokenMatchesType() {
        Recognizer r = new Recognizer("++abc");
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
        assertFalse(r.nextTokenMatches(Token.Type.NAME));
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
        assertTrue(r.nextTokenMatches(Token.Type.NAME));
    }

	/**
	 * Test method for {@link bugs.Recognizer#nextTokenMatches(Token.Type, java.lang.String)}.
	 */
    @Test
    public void testNextTokenMatchesTypeString() {
        Recognizer r = new Recognizer("+abc+");
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
        assertTrue(r.nextTokenMatches(Token.Type.NAME, "abc"));
        assertFalse(r.nextTokenMatches(Token.Type.SYMBOL, "*"));
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
    }

	/**
	 * Test method for {@link bugs.Recognizer#nextToken()}.
	 */
    @Test
    public void testNextToken() {
        // NAME, KEYWORD, NUMBER, SYMBOL, EOL, EOF };
        Recognizer r = new Recognizer("abc move 25 *\n");
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        assertEquals(new Token(Token.Type.KEYWORD, "move"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "*"), r.nextToken());
        assertEquals(new Token(Token.Type.EOL, "\n"), r.nextToken());
        assertEquals(new Token(Token.Type.EOF, "EOF"), r.nextToken());
        
        r = new Recognizer("foo.bar 123.456");
        assertEquals(new Token(Token.Type.NAME, "foo"), r.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "."), r.nextToken());
        assertEquals(new Token(Token.Type.NAME, "bar"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "123.456"), r.nextToken());
    }

	/**
	 * Test method for {@link bugs.Recognizer#pushBack()}.
	 */
    @Test
    public void testPushBack() {
        Recognizer r = new Recognizer("abc 25");
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        r.pushBack();
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
    }
    
//  ----- "Helper" methods

    /**
     * This method is given a String containing some or all of the
     * tokens that should yet be returned by the Tokenizer, and tests
     * whether the Tokenizer in fact has those Tokens. To succeed,
     * everything in the given String must still be in the Tokenizer,
     * but there may be additional (untested) Tokens to be returned.
     * This method is primarily to test whether rejected Tokens are
     * pushed back appropriately.
     * 
     * @param recognizer The Recognizer whose Tokenizer is to be tested.
     * @param expectedTokens The Tokens we expect to get from the Tokenizer.
     */
    private void followedBy(Recognizer recognizer, String expectedTokens) {
        int expectedType;
        int actualType;
        StreamTokenizer actual = recognizer.tokenizer;

        Reader reader = new StringReader(expectedTokens);
        StreamTokenizer expected = new StreamTokenizer(reader);
        expected.ordinaryChar('-');
        expected.ordinaryChar('/');

        try {
            while (true) {
                expectedType = expected.nextToken();
                if (expectedType == StreamTokenizer.TT_EOF) break;
                actualType = actual.nextToken();
                assertEquals(expectedType, actualType);
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

// -------- end Helper methods
    
	/**
	 * Test method for {@link bugs.Recognizer#isExpression()}.
	 */
	@Test
	public void testIsExpression() {
		assertTrue(r1.isExpression());	// "250"
		assertTrue(r2.isExpression());	// "hello"
		assertTrue(r3.isExpression());	// "(xyz + 3)"
		assertTrue(r4.isExpression());	// "12 * 5 - 3 * 4 / 6 + 8"
		assertTrue(r5.isExpression());	// "12 * ((5 - 3) * 4) / 6 + (8)"
		assertFalse(r0.isExpression());	// ""
		assertFalse(r8.isExpression());	// "#"
		
		Recognizer r6 = new Recognizer("-function()");
		assertTrue(r6.isExpression());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isExpression()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsExpressionE1() {
		assertFalse(r6.isExpression());	// "17 +"
	}

	/**
	 * Test method for {@link bugs.Recognizer#isExpression()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsExpressionE2() {
		assertFalse(r7.isExpression());	// "22 *"
	}

//	/**
//	 * Test method for {@link bugs.Recognizer#isUnsignedFactor()}.
//	 */
//	@Test
//	public void testIsUnsignedFactor() {
//		fail("Not yet implemented"); // TODO
//	} // this was created by eclipse bc was apparently not in original...?

	/**
	 * Test method for {@link bugs.Recognizer#isAction()}.
	 */
	@Test
	public void testIsAction() {
		getActions();
		
		assertTrue(r1.isAction());
		assertTrue(r2.isAction());
		assertTrue(r3.isAction());
		assertTrue(r4.isAction());
		assertTrue(r5.isAction());
		
		assertFalse(r6.isAction());
		assertFalse(r0.isAction());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAllbugsCode()}.
	 */
	@Test
	public void testIsAllbugsCode() {
        Recognizer r1 = new Recognizer("Allbugs {\n var myvar1 \n var myvar2 \n "+
        		"define fn1 {\n}\n define fn2 {\n}\n }\n");
        Recognizer r2 = new Recognizer("Allbugs {\n var myvar1 \n "+
        		"define fn1 {\n}\n }\n");
        Recognizer r3 = new Recognizer("Allbugs {\n }\n");
        Recognizer r4 = new Recognizer("notAllbugs {\n }\n");
        assertTrue(r1.isAllbugsCode());
        assertTrue(r2.isAllbugsCode());
        assertTrue(r3.isAllbugsCode());
        assertFalse(r4.isAllbugsCode());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAllbugsCode()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAllbugsCodeE1() {
        Recognizer r = new Recognizer("Allbugs !\n var myvar1 \n "+
        		"define fn1 {\n}\n }\n");
        r.isAllbugsCode();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAllbugsCode()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAllbugsCodeE2() {
        Recognizer r = new Recognizer("Allbugs {nonewlineforyou var myvar1 \n "+
        		"define fn1 {\n}\n }\n");
        r.isAllbugsCode();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAllbugsCode()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAllbugsCodeE3() {
        Recognizer r = new Recognizer("Allbugs {\n novar myvar1 \n "+
        		"define fn1 {\n}\n }\n");
        r.isAllbugsCode();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAllbugsCode()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAllbugsCodeE4() {
        Recognizer r = new Recognizer("Allbugs {\n var myvar1 \n "+
        		"nodefine fn1 {\n}\n }\n");
        r.isAllbugsCode();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAllbugsCode()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAllbugsCodeE5() {
        Recognizer r = new Recognizer("Allbugs {\n var myvar1 \n var myvar2 \n"+
        		"define fn1 {\n}\n !\n");
        r.isAllbugsCode();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAllbugsCode()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAllbugsCodeE6() {
        Recognizer r = new Recognizer("Allbugs {\n var myvar1 \n "+
        		"define fn1 {\n}\n } nonewlineforyou");
        r.isAllbugsCode();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAssignmentStatement()}.
	 */
	@Test
	public void testIsAssignmentStatement() {
        Recognizer r1 = new Recognizer("myVar = 42 \n");
        Recognizer r2 = new Recognizer("1myVar = 42 \n");
        assertTrue(r1.isAssignmentStatement());
        assertFalse(r2.isAssignmentStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAssignmentStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAssignmentStatementE1() {
        Recognizer r = new Recognizer("myVar != 42 \n");
        r.isAssignmentStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAssignmentStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAssignmentStatementE2() {
        Recognizer r = new Recognizer("myVar = 42notanexpression \n");
        r.isAssignmentStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAssignmentStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsAssignmentStatementE3() {
        Recognizer r = new Recognizer("myVar = 42 nonewlineforyou");
        r.isAssignmentStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBlock()}.
	 */
	@Test
	public void testIsBlock() {
		Recognizer r1 = new Recognizer("{\n }\n");
		Recognizer r2 = new Recognizer("{\n move 42 \n }\n");
		Recognizer r3 = new Recognizer("{\n move 42 \n turn 24 \n }\n");
		Recognizer r4 = new Recognizer("!{\n }\n");
		assertTrue(r1.isBlock());
		assertTrue(r2.isBlock());
		assertTrue(r3.isBlock());
		assertFalse(r4.isBlock());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBlock()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBlockE1() {
		Recognizer r = new Recognizer("{ nonewlineforyou }\n");
		r.isBlock();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBlock()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBlockE2() {
		Recognizer r = new Recognizer("{\n thisissonotastatement }\n");
		r.isBlock();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBlock()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBlockE3() {
		Recognizer r = new Recognizer("{\n !\n");
		r.isBlock();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBlock()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBlockE4() {
		Recognizer r = new Recognizer("{\n } nonewlineforyou");
		r.isBlock();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test
	public void testIsBugDefinition() {
		Recognizer r1 = new Recognizer("Bug mybug {\n var myvar1 \n var myvar2 "+
				"\n initially {\n}\n turn 42 \n move 42 \n color darkGray \n "+
				"define fn1 {\n}\n define fn2 {\n}\n }\n");
		Recognizer r2 = new Recognizer("Bug mybug {\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n }\n");
		Recognizer r3 = new Recognizer("Bug mybug {\n turn 42 \n }\n");
		Recognizer r4 = new Recognizer("noBug mybug {\n turn 42 \n }\n");
		assertTrue(r1.isBugDefinition());
		assertTrue(r2.isBugDefinition());
		assertTrue(r3.isBugDefinition());
		assertFalse(r4.isBugDefinition());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE1() {
		Recognizer r = new Recognizer("Bug 1badvarname {\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE2() {
		Recognizer r = new Recognizer("Bug mybug !\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE3() {
		Recognizer r = new Recognizer("Bug mybug {nolineforyou var myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE4() {
		Recognizer r = new Recognizer("Bug mybug {\n novar myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE5() {
		Recognizer r = new Recognizer("Bug mybug {\n var myvar1 \n "+
				"notinitially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE6() {
		Recognizer r = new Recognizer("Bug mybug {\n var myvar1 \n "+
				"initially {\n}\n noturn 42 \n move 42 \n define fn1 {\n}\n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE7() {
		Recognizer r = new Recognizer("Bug mybug {\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n nomove 42 \n define fn1 {\n}\n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE8() {
		Recognizer r = new Recognizer("Bug mybug {\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n nodefine fn1 {\n}\n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE9() {
		Recognizer r = new Recognizer("Bug mybug {\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n !\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE10() {
		Recognizer r = new Recognizer("Bug mybug {\n var myvar1 \n "+
				"initially {\n}\n turn 42 \n move 42 \n define fn1 {\n}\n }nolineforyou");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsBugDefinitionE11() {
		Recognizer r = new Recognizer("Bug mybug {\n var myvar1 \n "+
				"initially {\n}\n initially {\n}\n turn 42 \n }\n");
		r.isBugDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isColorStatement()}.
	 */
	@Test
	public void testIsColorStatement() {
        Recognizer ra = new Recognizer("< <= abc > >= 25 != = #");
        Recognizer rb = new Recognizer("color darkGray \n");
        Recognizer rc = new Recognizer("color do \n"); //is ok for this part
        		// but will need to fail later bc not a color (have code ready)
        Recognizer rd = new Recognizer("notcolor darkGray \n");
        assertFalse(ra.isColorStatement());
        assertTrue(rb.isColorStatement());
        assertTrue(rc.isColorStatement());
        assertFalse(rd.isColorStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isColorStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsColorStatementE1() {
        Recognizer r = new Recognizer("color notakeyword \n");
        r.isColorStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isColorStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsColorStatementE2() {
        Recognizer r = new Recognizer("color color color");
        r.isColorStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isCommand()}.
	 */
	@Test
	public void testIsCommand() {
        getActions();
        assertTrue(r1.isCommand());
        assertTrue(r2.isCommand());
        assertTrue(r3.isCommand());
        assertTrue(r4.isCommand());
        assertTrue(r5.isCommand());
        
        getStatements();
        assertTrue(r1.isCommand());
        assertTrue(r2.isCommand());
        assertTrue(r3.isCommand());
        assertTrue(r4.isCommand());
        assertTrue(r5.isCommand());
        assertTrue(r6.isCommand());
        assertTrue(r7.isCommand());
        
        assertFalse(r0.isCommand()); // this is still "" from setup
	}

	/**
	 * Test method for {@link bugs.Recognizer#isComparator()}.
	 */
	@Test
	public void testIsComparator() {
        Recognizer r = new Recognizer("< <= abc > >= 25 != = #");
        // it seems odd but !== or =!= would be 'okay' (just 2 comparators each)
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertFalse(r.isComparator());
		r.nextToken();
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertFalse(r.isComparator());
		r.nextToken();
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertFalse(r.isComparator());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isDoStatement()}.
	 */
	@Test
	public void testIsDoStatement() {
		Recognizer r2 = new Recognizer("do something \n");
		Recognizer r5 = new Recognizer("do something (1, 2, 3) \n");
		assertTrue(r2.isDoStatement());
		assertTrue(r5.isDoStatement());
		
		assertFalse(r0.isDoStatement());
		assertFalse(r3.isDoStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isDoStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsDoStatementE1() {
		Recognizer r1 = new Recognizer("do 12 \n");
		r1.isDoStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isDoStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsDoStatementE2() {
		Recognizer r3 = new Recognizer("do something");
		r3.isDoStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isDoStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsDoStatementE3() {
		Recognizer r4 = new Recognizer("do something (invalid list) \n");
		r4.isDoStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isEol()}.
	 */
	@Test
	public void testIsEol() {
		Recognizer r = new Recognizer("25\n30\n\n35\n\n\n\n40");
		assertFalse(r.isEol());
		r.nextToken();
		assertTrue(r.isEol());
		assertFalse(r.isEol());
		r.nextToken();
		assertTrue(r.isEol());
		assertFalse(r.isEol());
		r.nextToken();
		assertTrue(r.isEol());
		assertFalse(r.isEol());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isExitIfStatement()}.
	 */
	@Test
	public void testIsExitIfStatement() {
		Recognizer r1 = new Recognizer("exit if 42 \n");
		Recognizer r2 = new Recognizer("noexit if 42 \n");
		assertTrue(r1.isExitIfStatement());
		assertFalse(r2.isExitIfStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isExitIfStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsExitIfStatementE1() {
		Recognizer r = new Recognizer("exit unless 42 \n");
		r.isExitIfStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isExitIfStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsExitIfStatementE2() {
		Recognizer r = new Recognizer("exit if 1notanexpression \n");
		r.isExitIfStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isExitIfStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsExitIfStatementE3() {
		Recognizer r = new Recognizer("exit if 42 nonewlineforyou");
		r.isExitIfStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionCall()}.
	 */
	@Test
	public void testIsFunctionCall() {
		Recognizer r1 = new Recognizer("moveto(10)");
		Recognizer r2 = new Recognizer("dosomething()");
		Recognizer r3 = new Recognizer("do(10 + 10, 20 * 20, 3 - 3)");
		Recognizer r4 = new Recognizer("()");
		Recognizer r5 = new Recognizer("10()");
		assertTrue(r1.isFunctionCall());
		assertTrue(r2.isFunctionCall());
		assertTrue(r3.isFunctionCall());
		assertFalse(r4.isFunctionCall());
		assertFalse(r5.isFunctionCall());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionCall()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionCallE1() {
		Recognizer r = new Recognizer("return(10+)");
		r.isFunctionCall();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionCall()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionCallE2() {
		Recognizer r = new Recognizer("return(");
		r.isFunctionCall();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionCall()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionCallE3() {
		Recognizer r = new Recognizer("return");
		r.isFunctionCall();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionCall()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionCallE4() {
		Recognizer r = new Recognizer("return(10,2,)");
		r.isFunctionCall();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionDefinition()}.
	 */
	@Test
	public void testIsFunctionDefinition() {
		Recognizer r1 = new Recognizer("define fn {\n}\n");
		Recognizer r2 = new Recognizer("define fn using myvar {\n}\n");
		Recognizer r3 = new Recognizer("define fn using myvar1, myvar2 {\n}\n");
		Recognizer r4 = new Recognizer("define fn using myvar1, myvar2, myvar3 {\n}\n");
		Recognizer r5 = new Recognizer("nodefine fn {\n}\n");
		assertTrue(r1.isFunctionDefinition());
		assertTrue(r2.isFunctionDefinition());
		assertTrue(r3.isFunctionDefinition());
		assertTrue(r4.isFunctionDefinition());
		assertFalse(r5.isFunctionDefinition());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionDefinitionE1() {
		Recognizer r = new Recognizer("define 42");
		r.isFunctionDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionDefinitionE2() {
		Recognizer r = new Recognizer("define newFunction usando");
		r.isFunctionDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionDefinitionE3() {
		Recognizer r = new Recognizer("define newFunction using 42");
		r.isFunctionDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionDefinitionE4() {
		Recognizer r = new Recognizer("define newFunction using newvar!");
		r.isFunctionDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionDefinitionE5() {
		Recognizer r = new Recognizer("define newFunction using newvar, 42");
		r.isFunctionDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionDefinition()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsFunctionDefinitionE6() {
		Recognizer r = new Recognizer("define newFunction using newvar, newvar2 sonotablock");
		r.isFunctionDefinition();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isInitializationBlock()}.
	 */
	@Test
	public void testIsInitializationBlock() {
		Recognizer r1 = new Recognizer("initially {\n }\n");
		Recognizer r2 = new Recognizer("notinitially {\n }\n");
		assertTrue(r1.isInitializationBlock());
		assertFalse(r2.isInitializationBlock());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isInitializationBlock()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsInitializationBlockE1() {
		Recognizer r = new Recognizer("initially notablock");
		r.isInitializationBlock();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test
	public void testIsLineAction() {
		Recognizer r1 = new Recognizer("line 42, 42, 42, 42\n");
		Recognizer r2 = new Recognizer("noline 42, 42, 42, 42\n");
		assertTrue(r1.isLineAction());
		assertFalse(r2.isLineAction());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLineActionE1() {
		Recognizer r = new Recognizer("line 42notanexpression, 42, 42, 42\n");
		r.isLineAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLineActionE2() {
		Recognizer r = new Recognizer("line 42! 42, 42, 42\n");
		r.isLineAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLineActionE3() {
		Recognizer r = new Recognizer("line 42, 42notanexpression, 42, 42\n");
		r.isLineAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLineActionE4() {
		Recognizer r = new Recognizer("line 42, 42! 42, 42\n");
		r.isLineAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLineActionE5() {
		Recognizer r = new Recognizer("line 42, 42, 42notanexpression, 42\n");
		r.isLineAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLineActionE6() {
		Recognizer r = new Recognizer("line 42, 42, 42! 42\n");
		r.isLineAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLineActionE7() {
		Recognizer r = new Recognizer("line 42, 42, 42, 42notanexpression\n");
		r.isLineAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLineActionE8() {
		Recognizer r = new Recognizer("line 42, 42, 42, 42 nonewlineforyou");
		r.isLineAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLoopStatement()}.
	 */
	@Test
	public void testIsLoopStatement() {
		Recognizer r1 = new Recognizer("loop {\n}\n");
		Recognizer r2 = new Recognizer("noloop {\n}\n");
		assertTrue(r1.isLoopStatement());
		assertFalse(r2.isLoopStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLoopStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsLoopStatementE1() {
		Recognizer r = new Recognizer("loop !{\n}\n");
		r.isLoopStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveAction()}.
	 */
	@Test
	public void testIsMoveAction() {
		Recognizer r1 = new Recognizer("move 42 \n");
		Recognizer r2 = new Recognizer("nomove 42 \n");
		assertTrue(r1.isMoveAction());
		assertFalse(r2.isMoveAction());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsMoveActionE1() {
		Recognizer r = new Recognizer("move 42notanexpression \n");
		r.isMoveAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsMoveActionE2() {
		Recognizer r = new Recognizer("move 42 nonewlineforyou");
		r.isMoveAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveToAction()}.
	 */
	@Test
	public void testIsMoveToAction() {
		Recognizer r1 = new Recognizer("moveto 42, 42 \n");
		Recognizer r2 = new Recognizer("nomoveto 42, 42 \n");
		assertTrue(r1.isMoveToAction());
		assertFalse(r2.isMoveToAction());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveToAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsMoveToActionE1() {
		Recognizer r = new Recognizer("moveto 42notanexpression, 42 \n");
		r.isMoveToAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveToAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsMoveToActionE2() {
		Recognizer r = new Recognizer("moveto 42! 42 \n");
		r.isMoveToAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveToAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsMoveToActionE3() {
		Recognizer r = new Recognizer("moveto 42, 42notanexpression \n");
		r.isMoveToAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveToAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsMoveToActionE4() {
		Recognizer r = new Recognizer("moveto 42, 42 nonewlineforyou");
		r.isMoveToAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isProgram()}.
	 */
	@Test
	public void testIsProgram() {
		Recognizer r1 = new Recognizer("Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n"+
				"Bug mybug2 {\n turn 42 \n }\n"+
				"Bug mybug3 {\n turn 42 \n }\n");
		Recognizer r2 = new Recognizer("Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n"+
				"Bug mybug2 {\n turn 42 \n }\n");
		Recognizer r3 = new Recognizer("Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n");
		Recognizer r4 = new Recognizer("Bug mybug1 {\n turn 42 \n }\n");
		Recognizer r5 = new Recognizer("noBug mybug1 {\n turn 42 \n }\n");
		Recognizer r6 = new Recognizer("noAllbugs {\n}\n");
		assertTrue(r1.isProgram());
		assertTrue(r2.isProgram());
		assertTrue(r3.isProgram());
		assertTrue(r4.isProgram());
		
		assertFalse(r5.isProgram());
		assertFalse(r6.isProgram());
		
		//I thought this should be disallowed, but it doesn't break the rule, technically
		Recognizer r7 = new Recognizer("Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n"+
				"noBug mybug2 {\n turn 42 \n }\n");
		assertTrue(r7.isProgram());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isProgram()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsProgramE1() {
		Recognizer r = new Recognizer("Allbugs {\n}\n "+
				"Allbugs {\n}\n "+
				"Bug mybug1 {\n turn 42 \n }\n"+
				"Bug mybug2 {\n turn 42 \n }\n");
		r.isProgram();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isProgram()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsProgramE2() {
		Recognizer r = new Recognizer("Allbugs {\n}\n "+
				"noBug mybug1 {\n turn 42 \n }\n"+
				"Bug mybug2 {\n turn 42 \n }\n");
		r.isProgram();
	}

//	/**
//	 * Test method for {@link bugs.Recognizer#isProgram()}.
//	 */
//	@Test(expected=SyntaxException.class)
//	public void testIsProgramE3() {
//		Recognizer r = new Recognizer("Allbugs {\n}\n "+
//				"Bug mybug1 {\n turn 42 \n }\n"+
//				"noBug mybug2 {\n turn 42 \n }\n");
//		r.isProgram();
//	} // changed my mind, think this is allowed at least for now

	/**
	 * Test method for {@link bugs.Recognizer#isReturnStatement()}.
	 */
	@Test
	public void testIsReturnStatement() {
		Recognizer r1 = new Recognizer("return 42 \n");
		Recognizer r2 = new Recognizer("noreturn 42 \n");
		assertTrue(r1.isReturnStatement());
		assertFalse(r2.isReturnStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isReturnStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsReturnStatementE1() {
		Recognizer r = new Recognizer("return 1notanexpression \n");
		r.isReturnStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isReturnStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsReturnStatementE2() {
		Recognizer r = new Recognizer("return 42 nolineforyou");
		r.isReturnStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isStatement()}.
	 */
	@Test
	public void testIsStatement() {
		getStatements();
		
		assertTrue(r1.isStatement());
		assertTrue(r2.isStatement());
		assertTrue(r3.isStatement());
		assertTrue(r4.isStatement());
		assertTrue(r5.isStatement());
		assertTrue(r6.isStatement());
		assertTrue(r7.isStatement());
		
		assertFalse(r8.isStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test
	public void testIsSwitchStatement() {
		Recognizer r1 = new Recognizer("switch {\n }\n");
		Recognizer r2 = new Recognizer("switch {\n case 1 \n }\n");
		Recognizer r3 = new Recognizer("switch {\n case 1 \n move 42 \n }\n");
		Recognizer r4 = new Recognizer("switch {\n case 1 \n move 42 \n turn 24 \n }\n");
		Recognizer r5 = new Recognizer("switch {\n case 1 \n move 42 \n case two \n turn 24 \n }\n");
		Recognizer r6 = new Recognizer("noswitch {\n }\n");
		assertTrue(r1.isSwitchStatement());
		assertTrue(r2.isSwitchStatement());
		assertTrue(r3.isSwitchStatement());
		assertTrue(r4.isSwitchStatement());
		assertTrue(r5.isSwitchStatement());
		
		assertFalse(r6.isSwitchStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsSwitchStatementE1() {
		Recognizer r = new Recognizer("switch !\n case 1 \n move 42 \n }\n");
		r.isSwitchStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsSwitchStatementE2() {
		Recognizer r = new Recognizer("switch {nonewlineforyou case 1 \n move 42 \n }\n");
		r.isSwitchStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsSwitchStatementE3() {
		Recognizer r = new Recognizer("switch {\n nocase 1 \n move 42 \n }\n");
		r.isSwitchStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsSwitchStatementE4() {
		Recognizer r = new Recognizer("switch {\n case 1notanexpression \n move 42 \n }\n");
		r.isSwitchStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsSwitchStatementE5() {
		Recognizer r = new Recognizer("switch {\n case 1 nonewlineforyou move 42 \n }\n");
		r.isSwitchStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsSwitchStatementE6() {
		Recognizer r = new Recognizer("switch {\n case 1 \n nomove }\n");
		r.isSwitchStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsSwitchStatementE7() {
		Recognizer r = new Recognizer("switch {\n case 1 \n move 42 \n !\n");
		r.isSwitchStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsSwitchStatementE8() {
		Recognizer r = new Recognizer("switch {\n case 1 \n move 42 \n }nonewlineforyou");
		r.isSwitchStatement();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isTurnAction()}.
	 */
	@Test
	public void testIsTurnAction() {
		Recognizer r1 = new Recognizer("turn 42 \n");
		Recognizer r2 = new Recognizer("noturn 42 \n");
		assertTrue(r1.isTurnAction());
		assertFalse(r2.isTurnAction());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isTurnAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsTurnActionE1() {
		Recognizer r = new Recognizer("turn 42notanexpression \n");
		r.isTurnAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isTurnAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsTurnActionE2() {
		Recognizer r = new Recognizer("turn 42 nonewlineforyou");
		r.isTurnAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isTurnToAction()}.
	 */
	@Test
	public void testIsTurnToAction() {
		Recognizer r1 = new Recognizer("turnto 42 \n");
		Recognizer r2 = new Recognizer("noturnto 42 \n");
		assertTrue(r1.isTurnToAction());
		assertFalse(r2.isTurnToAction());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isTurnToAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsTurnToActionE1() {
		Recognizer r = new Recognizer("turnto 42notanexpression \n");
		r.isTurnToAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isTurnToAction()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsTurnToActionE2() {
		Recognizer r = new Recognizer("turnto 42 nonewlineforyou");
		r.isTurnToAction();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isVarDeclaration()}.
	 */
	@Test
	public void testIsVarDeclaration() {
		Recognizer r1 = new Recognizer("var var1 \n");
		Recognizer r2 = new Recognizer("var var1, var2, var3 \n");
		Recognizer r3 = new Recognizer("novar var1 \n");
		assertTrue(r1.isVarDeclaration());
		assertTrue(r2.isVarDeclaration());
		assertFalse(r3.isVarDeclaration());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isVarDeclaration()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsVarDeclarationE1() {
		Recognizer r = new Recognizer("var 1notavarname, var2 \n");
		r.isVarDeclaration();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isVarDeclaration()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsVarDeclarationE2() {
		Recognizer r = new Recognizer("var var1! var2 \n");
		r.isVarDeclaration();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isVarDeclaration()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsVarDeclarationE3() {
		Recognizer r = new Recognizer("var var1, 2notavarname \n");
		r.isVarDeclaration();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isVarDeclaration()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsVarDeclarationE4() {
		Recognizer r = new Recognizer("var var1, var2, \n");
		r.isVarDeclaration();
	}

	/**
	 * Test method for {@link bugs.Recognizer#isVarDeclaration()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsVarDeclarationE5() {
		Recognizer r = new Recognizer("var var1, var2 nonewlineforyou");
		r.isVarDeclaration();
	}

}
