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
	 * Test method for {@link bugs.Recognizer#symbol(java.lang.String)}.
	 * TODO except that it doesn't? it tests what, SYMBOL?
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
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isUnsignedFactor()}.
	 */
	@Test
	public void testIsUnsignedFactor() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAction()}.
	 */
	@Test
	public void testIsAction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAllbugsCode()}.
	 */
	@Test
	public void testIsAllbugsCode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isAssignmentStatement()}.
	 */
	@Test
	public void testIsAssignmentStatement() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBlock()}.
	 */
	@Test
	public void testIsBlock() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isBugDefinition()}.
	 */
	@Test
	public void testIsBugDefinition() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isColorStatement()}.
	 */
	@Test
	public void testIsColorStatement() {
        Recognizer ra = new Recognizer("< <= abc > >= 25 != = #");
        Recognizer rb = new Recognizer("color darkGray \n");
        Recognizer rc = new Recognizer("color do \n");
        Recognizer rd = new Recognizer("notcolor do \n");
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
        assertFalse(r.isColorStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isColorStatement()}.
	 */
	@Test(expected=SyntaxException.class)
	public void testIsColorStatementE2() {
        Recognizer r = new Recognizer("color color color");
        assertFalse(r.isColorStatement());
	}

	/**
	 * Test method for {@link bugs.Recognizer#isCommand()}.
	 */
	@Test
	public void testIsCommand() {
		fail("Not yet implemented"); // TODO
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
		fail("Not yet implemented"); // TODO
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
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionCall()}.
	 */
	@Test
	public void testIsFunctionCall() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isFunctionDefinition()}.
	 */
	@Test
	public void testIsFunctionDefinition() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isInitializationBlock()}.
	 */
	@Test
	public void testIsInitializationBlock() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLineAction()}.
	 */
	@Test
	public void testIsLineAction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isLoopStatement()}.
	 */
	@Test
	public void testIsLoopStatement() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveAction()}.
	 */
	@Test
	public void testIsMoveAction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isMoveToAction()}.
	 */
	@Test
	public void testIsMoveToAction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isProgram()}.
	 */
	@Test
	public void testIsProgram() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isReturnStatement()}.
	 */
	@Test
	public void testIsReturnStatement() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isStatement()}.
	 */
	@Test
	public void testIsStatement() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isSwitchStatement()}.
	 */
	@Test
	public void testIsSwitchStatement() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isTurnAction()}.
	 */
	@Test
	public void testIsTurnAction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isTurnToAction()}.
	 */
	@Test
	public void testIsTurnToAction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link bugs.Recognizer#isVarDeclaration()}.
	 */
	@Test
	public void testIsVarDeclaration() {
		fail("Not yet implemented"); // TODO
	}

}
