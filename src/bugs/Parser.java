package bugs;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;

import bugs.Token.Type;

import tree.Tree;

/**
 * Parser for numeric expressions. Used as starter code for
 * the Bugs language parser in CIT594, Spring 2015.
 * 
 * @author Dave Matuszek
 * @version February 2015
 */
public class Parser {
    /** The tokenizer used by this Parser. */
    StreamTokenizer tokenizer = null;
    /** The number of the line of source code currently being processed. */
    private int lineNumber = 1;

    /**
     * The stack used for holding Trees as they are created.
     */
    public Stack<Tree<Token>> stack = new Stack<>();

    /**
     * Constructs a Parser for the given string.
     * @param text The string to be parsed.
     */
    public Parser(String text) {
        Reader reader = new StringReader(text);
        tokenizer = new StreamTokenizer(reader);
        tokenizer.parseNumbers();
        tokenizer.eolIsSignificant(true);
        tokenizer.slashStarComments(true);
        tokenizer.slashSlashComments(true);
        tokenizer.lowerCaseMode(false);
        tokenizer.ordinaryChars(33, 47);
        tokenizer.ordinaryChars(58, 64);
        tokenizer.ordinaryChars(91, 96);
        tokenizer.ordinaryChars(123, 126);
        tokenizer.quoteChar('\"');
        lineNumber = 1;
    }

    //****---------Nicki's code----------
    
	/**
	 * Tries to recognize an &lt;action&gt;.
	 * <pre>&lt;action&gt; ::= &lt;move action&gt;
	 * | &lt;moveto action&gt;
	 * | &lt;turn action&gt;
	 * | &lt;turnto action&gt;
	 * | &lt;line action&gt;</pre>
	 * @return <code>true</code> if an action is recognized.
	 */
	public boolean isAction() {
		//TODO write parse portion
		if (isMoveAction() || isMoveToAction() || isTurnAction() ||
				isTurnToAction() || isLineAction()) {
			return true;
		}
	    return false;
	}
	
	/**
	 * Tries to recognize an &lt;allbugs code&gt;.
	 * <pre>&lt;allbugs code&gt; ::= "Allbugs"  "{" &lt;eol&gt;
	 * { &lt;var declaration&gt; }
	 * { &lt;function definition&gt; }
	 * "}" &lt;eol&gt;</pre>
	 * @return <code>true</code> if an allbugs code is recognized.
	 */
	public boolean isAllbugsCode() {
		//TODO write parse portion
		if (!keyword("Allbugs")) return false;
		if (!symbol("{")) error("Missing '{'");
		if (!isEol()) error("Missing EOL");
		while (isVarDeclaration());
		while (isFunctionDefinition());
		if (!symbol("}")) error("Missing '}'");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize an &lt;assignment statement&gt;.
	 * <pre>&lt;assignment statement&gt; ::= &lt;variable&gt; "=" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if an assignment statement is recognized.
	 */
	public boolean isAssignmentStatement() {
		//TODO write parse portion
		if (!isVariable()) return false;
		if (!symbol("=")) error("Expected '=' after variable name");
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;block&gt;.
	 * <pre>&lt;block&gt; ::= "{" &lt;eol&gt; { &lt;command&gt; }  "}" &lt;eol&gt;</pre>
	 * @return <code>true</code> if a block is recognized.
	 */
	public boolean isBlock() {
		//TODO write parse portion
		if (!symbol("{")) return false;
		if (!isEol()) error("Missing EOL");
		while (isCommand());
		if (!symbol("}")) error("Missing '}' after command");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;bug definition&gt;.
	 * <pre>&lt;bug definition&gt; ::= "Bug" &lt;name&gt; "{" &lt;eol&gt;
	 * { &lt;var declaration&gt; }
	 * [ &lt;initialization block&gt; ]
	 * &lt;command&gt;
	 * { &lt;command&gt; }
	 * { &lt;function definition&gt; }
	 * "}" &lt;eol&gt;</pre>
	 * @return <code>true</code> if a bug definition is recognized.
	 */
	public boolean isBugDefinition() {
		//TODO write parse portion
		if (!keyword("Bug")) return false;
		if (!name()) error("Invalid name");
		if (!symbol("{")) error("Missing '{'");
		if (!isEol()) error("Missing EOL");
		while (isVarDeclaration());
		if (isInitializationBlock());
		if (!isCommand()) error("Missing/invalid command");
		while (isCommand());
		while (isFunctionDefinition());
		if (!symbol("}")) error("Missing '}'");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;color statement&gt;.
	 * <pre>&lt;color statement&gt; ::= "color" &lt;KEYWORD&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a color statement is recognized.
	 */
	public boolean isColorStatement() {
		//TODO write parse portion
		if (!keyword("color")) return false;
		if (!nextTokenMatches(Type.KEYWORD)) error("Not a valid keyword");
		//pushBack(); //these 2 lines are for later
		//if (!Token.COLORS.contains(nextToken().value)) error("Invalid color"); 
		if (!nextTokenMatches(Type.EOL)) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;command&gt;.
	 * <pre>&lt;command&gt; ::= &lt;action&gt;
	 * | &lt;statement&gt;</pre>
	 * @return <code>true</code> if a command is recognized.
	 */
	public boolean isCommand() {
		//TODO write parse portion
	    return isAction() || isStatement();
	}
	
	/**
	 * Tries to recognize a &lt;comparator&gt;.
	 * <pre>&lt;comparator&gt; ::= "&lt;" | "&lt;=" | "=" | "!=" | "&gt;=" | "&gt;"</pre>
	 * @return <code>true</code> if a comparator is recognized.
	 */
	public boolean isComparator() {
		//TODO write parse portion
		if (symbol("<") || symbol(">")) {
			if (symbol("="));
			return true;
		}
		if (symbol("!")) {
			if (!symbol("=")) error("No '=' after '!'");
			return true;	// this allows WEIRD spacing (!   = okay?)
		}
		if (symbol("=")) {
			return true; 
		}
	    return false;
	}
	
	/**
	 * Tries to recognize a &lt;do statement&gt;.
	 * <pre>&lt;do statement&gt; ::= "do" &lt;variable&gt; [ &lt;parameter list&gt; ] &lt;eol&gt;</pre>
	 * @return <code>true</code> if a do statement is recognized.
	 */
	public boolean isDoStatement() {
		//TODO write parse portion
		if (!keyword("do")) return false;
		if (!isVariable()) error("Missing variable after 'do'");
		if (isParameterList());
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize an &lt;EOL&gt;.
	 * <pre>&lt;eol&gt; ::= &lt;EOL&gt; { &lt;EOL&gt; } </pre>
	 * @return <code>true</code> if an EOL is recognized.
	 */
	public boolean isEol() {
		//TODO write parse portion
	    if (!nextTokenMatches(Type.EOL)) {
	    	return false;
	    }
	    isEol();	// this is hacky. I should stick to iteration here.
	    return true;
	}
	
	/**
	 * Tries to recognize an &lt;exit if statement&gt;.
	 * <pre>&lt;exit if statement&gt; ::= "exit" "if" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if an exit if statement is recognized.
	 */
	public boolean isExitIfStatement() {
		//TODO write parse portion
		if (!keyword("exit")) return false;
		if (!keyword("if")) error("Missing 'if' after 'exit'");
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;function call&gt;.
	 * <pre>&lt;function call&gt; ::= &lt;NAME&gt; &lt;parameter list&gt;</pre>
	 * @return <code>true</code> if a function call is recognized.
	 */
	public boolean isFunctionCall() {
		//TODO write parse portion
		if (!name() && !nextTokenMatches(Type.KEYWORD)) return false;
		if (!isParameterList()) error("Missing/invalid parameter list");
		return true;
	}
	
	/**
	 * Tries to recognize a &lt;function definition&gt;.
	 * <pre>&lt;function definition&gt; ::= "define" &lt;NAME&gt; [ "using" &lt;variable&gt; { "," &lt;variable&gt; }  ] &lt;block&gt;</pre>
	 * @return <code>true</code> if a function definition is recognized.
	 */
	public boolean isFunctionDefinition() {
		//TODO write parse portion
		if (!keyword("define")) return false;
		if (!name()) error("Invalid name");
		if (keyword("using")) {
			if (!isVariable()) error("Invalid variable name");
			while (symbol(",")) {
				if (!isVariable()) error("Invalid variable name");
			}
		}
		if (!isBlock()) error("Missing/invalid block");
	    return true;
	}
	
	/**
	 * Tries to recognize an &lt;initialization block&gt;.
	 * <pre>&lt;initialization block&gt; ::= "initially" &lt;block&gt;</pre>
	 * @return <code>true</code> if an initialization block is recognized.
	 */
	public boolean isInitializationBlock() {
		//TODO write parse portion
		if (!keyword("initially")) return false;
		if (!isBlock()) error("Missing/invalid block");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;line action&gt;.
	 * <pre>&lt;line action&gt; ::= "line" &lt;expression&gt; ","&lt;expression&gt; ","&lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a line action is recognized.
	 */
	public boolean isLineAction() {
		//TODO write parse portion
		if (!keyword("line")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!symbol(",")) error("Missing ','");
		if (!isExpression()) error("Missing/invalid expression");
		if (!symbol(",")) error("Missing ','");
		if (!isExpression()) error("Missing/invalid expression");
		if (!symbol(",")) error("Missing ','");
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;loop statement&gt;.
	 * <pre>&lt;loop statement&gt; ::= "loop" &lt;block&gt;</pre>
	 * @return <code>true</code> if a loop statement is recognized.
	 */
	public boolean isLoopStatement() {
		//TODO write parse portion
		if (!keyword("loop")) return false;
		if (!isBlock()) error("Missing/invalid block");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;move action&gt;.
	 * <pre>&lt;move action&gt; ::= "move" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a move action is recognized.
	 */
	public boolean isMoveAction() {
		//TODO write parse portion
		if (!keyword("move")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;moveto action&gt;.
	 * <pre>&lt;moveto action&gt; ::= "moveto" &lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a move-to action is recognized.
	 */
	public boolean isMoveToAction() {
		//TODO write parse portion
		if (!keyword("moveto")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!symbol(",")) error("Missing ',' after expression");
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;program&gt;.
	 * <pre>&lt;program&gt; ::= [ &lt;allbugs code&gt; ]
	 * &lt;bug definition&gt;
	 * { &lt;bug definition&gt; }</pre>
	 * @return <code>true</code> if a program is recognized.
	 */
	public boolean isProgram() {
		//TODO write parse portion
		if (!isAllbugsCode()) {
			if (!isBugDefinition()) return false;
		}
		else if (!isBugDefinition()) error("Missing bug definition(s)");
		while (isBugDefinition());
		if (!nextTokenMatches(Type.EOF)) error("Invalid program format");
		//^^changed my mind about that for this hw, think it's allowed
		//doesn't say program ends in EOF, or anything particular, anyway
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;return statement&gt;.
	 * <pre>&lt;return statement&gt; ::= "return" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a return statement is recognized.
	 */
	public boolean isReturnStatement() {
		//TODO write parse portion
		if (!keyword("return")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;statement&gt;.
	 * <pre>&lt;statement&gt; ::= &lt;assignment statement&gt;
	 * | &lt;loop statement&gt;
	 * | &lt;exit if statement&gt;
	 * | &lt;switch statement&gt;
	 * | &lt;return statement&gt;
	 * | &lt;do statement&gt;
	 * | &lt;color statement&gt;</pre>
	 * @return <code>true</code> if a statement is recognized.
	 */
	public boolean isStatement() {
		//TODO write parse portion
		if (isAssignmentStatement() || isLoopStatement() || 
				isExitIfStatement() || isSwitchStatement() || 
				isReturnStatement() || isDoStatement() || isColorStatement()) {
			return true;
		}
	    return false;
	}
	
	/**
	 * Tries to recognize a &lt;switch statement&gt;.
	 * <pre>&lt;switch statement&gt; ::= "switch" "{" &lt;eol&gt;
	 * { "case" &lt;expression&gt; &lt;eol&gt;
	 * 	   { &lt;command&gt; } }
	 * "}" &lt;eol&gt;</pre>
	 * @return <code>true</code> if a switch statement is recognized.
	 */
	public boolean isSwitchStatement() {
		//TODO write parse portion
		if (!keyword("switch")) return false;
		if (!symbol("{")) error("Missing '{'");
		if (!isEol()) error("Missing EOL");
		while (keyword("case")) {
			if (!isExpression()) error("Missing/invalid expression");
			if (!isEol()) error("Missing EOL");
			while (isCommand());
		}
		if (!symbol("}")) error("Missing '}'");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;turn action&gt;.
	 * <pre>&lt;turn action&gt; ::= "turn" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a turn action is recognized.
	 */
	public boolean isTurnAction() {
		//TODO write parse portion
		if (!keyword("turn")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;turnto action&gt;.
	 * <pre>&lt;turnto action&gt; ::= "turnto" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a turn-to action is recognized.
	 */
	public boolean isTurnToAction() {
		//TODO write parse portion
		if (!keyword("turnto")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;var declaration&gt;.
	 * <pre>&lt;var declaration&gt; ::= "var" &lt;NAME&gt; { "," &lt;NAME&gt; } &lt;eol&gt;</pre>
	 * @return <code>true</code> if a var declaration is recognized.
	 */
	public boolean isVarDeclaration() {
		//TODO write parse portion
		if (!keyword("var")) return false;
		if (!name()) error("Invalid variable name");
		while (symbol(",")) {
			if (!name()) error("Invalid variable name");
		}
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	//***-----------End of Nicki's code-------------
    
    /**
     * Tries to build an &lt;expression&gt; on the global stack.
     * <pre>&lt;expression&gt; ::= &lt;arithmetic expression&gt; {  &lt;comparator&gt; &lt;arithmetic expression&gt; }
</pre>
     * A <code>SyntaxException</code> will be thrown if the add_operator
     * is present but not followed by a valid &lt;expression&gt;.
     * @return <code>true</code> if an expression is parsed.
     */
    public boolean isExpression() {
        if (!isArithmeticExpression()) return false;
        while (isComparator()) {
            if (!isArithmeticExpression()) error("Illegal expression after comparator");
            makeTree(2, 3, 1);
        }
        return true;
    }

    /**
     * Tries to build an &lt;expression&gt; on the global stack.
     * <pre>&lt;expression&gt; ::= &lt;term&gt; { &lt;add_operator&gt; &lt;expression&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if the add_operator
     * is present but not followed by a valid &lt;expression&gt;.
     * @return <code>true</code> if an expression is recognized.
     */
    public boolean isArithmeticExpression() {
        if (!isTerm())
            return false;
        while (isAddOperator()) {
            if (!isTerm()) error("Error in expression after '+' or '-'");
            makeTree(2, 3, 1);
        }
        return true;
    }

    /**
     * Tries to build a &lt;term&gt; on the global stack.
     * <pre>&lt;term&gt; ::= &lt;factor&gt; { &lt;multiply_operator&gt; &lt;term&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if the multiply_operator
     * is present but not followed by a valid &lt;term&gt;.
     * @return <code>true</code> if a term is parsed.
     */

    public boolean isTerm() {
        if (!isFactor()) {
            return false;
        }
        while (isMultiplyOperator()) {
            if (!isFactor()) {
                error("No term after '*' or '/'");
            }
            makeTree(2, 3, 1);
        }
        return true;
    }

    /**
     * Tries to build a &lt;factor&gt; on the global stack.
     * <pre>&lt;factor&gt; ::= [ &lt;unsigned factor&gt; ] &lt;name&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is parsed.
     */
    public boolean isFactor() {
        if(symbol("+") || symbol("-")) {
            if (isUnsignedFactor()) {
                makeTree(2, 1);
                return true;
            }
            error("No factor following unary plus or minus");
            return false; // Can't ever get here
        }
        return isUnsignedFactor();
    }

    /**
     * Tries to build an &lt;unsigned factor&gt; on the global stack.
     * <pre>&lt;unsigned factor&gt; ::= &lt;variable&gt; . &lt;variable&gt;
     *                    | &lt;function call&gt;
     *                    | &lt;variable&gt;
     *                    | &lt;number&gt;
     *                    | "(" &lt;expression&gt; ")"</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is parsed.
     */
    public boolean isUnsignedFactor() {
        if (name()) {
            if (symbol(".")) {
                // reference to another Bug
                if (name()) {
                    makeTree(2, 3, 1);
                }
                else error("Incorrect use of dot notation");
            }
            else if (isParameterList()) {
                // function call
                pushNewNode("call");
                makeTree(1, 3, 2);
            }
            else {
                // just a variable; leave it on the stack
            }
        }
        else if (number()) {
            // leave the number on the stack
        }
        else if (symbol("(")) {
            stack.pop();
            if (!isExpression()) {
                error("Error in parenthesized expression");
            }
            if (!symbol(")")) {
                error("Unclosed parenthetical expression");
            }
            stack.pop();
        }
        else {
            return false;
        }
       return true;
    }
    
    /**
     * Tries to recognize a &lt;parameter list&gt;.
     * <pre>&ltparameter list&gt; ::= "(" [ &lt;expression&gt; { "," &lt;expression&gt; } ] ")"
     * @return <code>true</code> if a parameter list is recognized.
     */
    public boolean isParameterList() {
        if (!symbol("(")) return false;
        stack.pop(); // remove open paren
        pushNewNode("var");
        if (isExpression()) {
            makeTree(2, 1);
            while (symbol(",")) {
                stack.pop(); // remove comma
                if (!isExpression()) error("No expression after ','");
                makeTree(2, 1);
            }
        }
        if (!symbol(")")) error("Parameter list doesn't end with ')'");
        stack.pop(); // remove close paren
        return true;
    }

    /**
     * Tries to recognize an &lt;add_operator&gt; and put it on the global stack.
     * <pre>&lt;add_operator&gt; ::= "+" | "-"</pre>
     * @return <code>true</code> if an addop is recognized.
     */
    public boolean isAddOperator() {
        return symbol("+") || symbol("-");
    }

    /**
     * Tries to recognize a &lt;multiply_operator&gt; and put it on the global stack.
     * <pre>&lt;multiply_operator&gt; ::= "*" | "/"</pre>
     * @return <code>true</code> if a multiply_operator is recognized.
     */
    public boolean isMultiplyOperator() {
        return symbol("*") || symbol("/");
    }
    
    /**
     * Tries to parse a &lt;variable&gt;; same as &lt;isName&gt;.
     * <pre>&lt;variable&gt; ::= &lt;NAME&gt;</pre>
     * @return <code>true</code> if a variable is parsed.
     */
    public boolean isVariable() {
        return name();
    }

    //------------------------- Private "helper" methods
    
    /**
     * Creates a new Tree consisting of a single node containing a
     * Token with the correct type and the given <code>value</code>,
     * and pushes it onto the global stack. 
     *
     * @param value The value of the token to be pushed onto the global stack.
     */
    private void pushNewNode(String value) {
        stack.push(new Tree<>(new Token(Token.typeOf(value), value)));
    }

    /**
     * Tests whether the next token is a number. If it is, the token
     * is moved to the stack, otherwise it is not.
     * 
     * @return <code>true</code> if the next token is a number.
     */
    private boolean number() {
        return nextTokenMatches(Token.Type.NUMBER);
    }

    /**
     * Tests whether the next token is a name. If it is, the token
     * is moved to the stack, otherwise it is not.
     * 
     * @return <code>true</code> if the next token is a name.
     */
    private boolean name() {
        return nextTokenMatches(Token.Type.NAME);
    }

    /**
     * Tests whether the next token is the expected name. If it is, the token
     * is moved to the stack, otherwise it is not.
     * 
     * @param expectedName The String value of the expected next token.
     * @return <code>true</code> if the next token is a name with the expected value.
     */
    private boolean name(String expectedName) {
        return nextTokenMatches(Token.Type.NAME, expectedName);
    }

    /**
     * Tests whether the next token is the expected keyword. If it is, the token
     * is moved to the stack, otherwise it is not.
     *
     * @param expectedKeyword The String value of the expected next token.
     * @return <code>true</code> if the next token is a keyword with the expected value.
     */
    private boolean keyword(String expectedKeyword) {
        return nextTokenMatches(Token.Type.KEYWORD, expectedKeyword);
    }

    /**
     * Tests whether the next token is the expected symbol. If it is,
     * the token is moved to the stack, otherwise it is not.
     * 
     * @param expectedSymbol The single-character String that is expected
     *        as the next symbol.
     * @return <code>true</code> if the next token is the expected symbol.
     */
    private boolean symbol(String expectedSymbol) {
        return nextTokenMatches(Token.Type.SYMBOL, expectedSymbol);
    }

    /**
     * If the next Token has the expected type, it is used as the
     * value of a new (childless) Tree node, and that node
     * is then pushed onto the stack. If the next Token does not
     * have the expected type, this method effectively does nothing.
     * 
     * @param type The expected type of the next token.
     * @return <code>true</code> if the next token has the expected type.
     */
    private boolean nextTokenMatches(Token.Type type) {
        Token t = nextToken();
        if (t.type == type) {
            stack.push(new Tree<>(t));
            return true;
        }
        pushBack();
        return false;
    }

    /**
     * If the next Token has the expected type and value, it is used as
     * the value of a new (childless) Tree node, and that node
     * is then pushed onto the stack; otherwise, this method does
     * nothing.
     * 
     * @param type The expected type of the next token.
     * @param value The expected value of the next token; must
     *              not be <code>null</code>.
     * @return <code>true</code> if the next token has the expected type.
     */
    private boolean nextTokenMatches(Token.Type type, String value) {
        Token t = nextToken();
        if (type == t.type && value.equals(t.value)) {
            stack.push(new Tree<>(t));
            return true;
        }
        pushBack();
        return false;
    }

    /**
     * Returns the next Token. Increments the global variable
     * <code>lineNumber</code> when an EOL is returned.
     * 
     * @return The next Token.
     */
    Token nextToken() {
        int code;
        try { code = tokenizer.nextToken(); }
        catch (IOException e) { throw new Error(e); } // Should never happen
        switch (code) {
            case StreamTokenizer.TT_WORD:
                if (Token.KEYWORDS.contains(tokenizer.sval)) {
                    return new Token(Token.Type.KEYWORD, tokenizer.sval);
                }
                return new Token(Token.Type.NAME, tokenizer.sval);
            case StreamTokenizer.TT_NUMBER:
                return new Token(Token.Type.NUMBER, tokenizer.nval + "");
            case StreamTokenizer.TT_EOL:
                lineNumber++;
                return new Token(Token.Type.EOL, "\n");
            case StreamTokenizer.TT_EOF:
                return new Token(Token.Type.EOF, "EOF");
            default:
                return new Token(Token.Type.SYMBOL, ((char) code) + "");
        }
    }

    /**
     * Returns the most recent Token to the tokenizer. Decrements the global
     * variable <code>lineNumber</code> if an EOL is pushed back.
     */
    void pushBack() {
        tokenizer.pushBack();
        if (tokenizer.ttype == StreamTokenizer.TT_EOL) lineNumber--;
    }

    /**
     * Assembles some number of elements from the top of the global stack
     * into a new Tree, and replaces those elements with the new Tree.<p>
     * <b>Caution:</b> The arguments must be consecutive integers 1..N,
     * in any order, but with no gaps; for example, makeTree(2,4,1,5)
     * would cause problems (3 was omitted).
     * 
     * @param rootIndex Which stack element (counting from 1) to use as
     * the root of the new Tree.
     * @param childIndices Which stack elements to use as the children
     * of the root.
     */    
    void makeTree(int rootIndex, int... childIndices) {
        // Get root from stack
        Tree<Token> root = getStackItem(rootIndex);
        // Get other trees from stack and add them as children of root
        for (int i = 0; i < childIndices.length; i++) {
            root.addChild(getStackItem(childIndices[i]));
        }
        // Pop root and all children from stack
        for (int i = 0; i <= childIndices.length; i++) {
            stack.pop();
        }
        // Put the root back on the stack
        stack.push(root);
    }
    
    /**
     * Returns the n-th item from the top of the global stack (counting the
     * top element as 1).
     * 
     * @param n Which stack element to return.
     * @return The n-th element in the global stack.
     */
    private Tree<Token> getStackItem(int n) {
        return stack.get(stack.size() - n);
    }

    /**
     * Utility routine to throw a <code>SyntaxException</code> with the
     * given message.
     * @param message The text to put in the <code>SyntaxException</code>.
     */
    private void error(String message) {
        throw new SyntaxException("Line " + lineNumber + ": " + message);
    }
}