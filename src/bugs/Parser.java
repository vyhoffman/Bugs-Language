package bugs;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;

import bugs.Token.Type;

import tree.Tree;

/**
 * Parser for the Bugs language, a project in CIT594, Spring 2015.
 * 
 * @author Nicki Hoffman
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
	 * Tries to build an &lt;action&gt; on the global stack.
	 * <pre>&lt;action&gt; ::= &lt;move action&gt;
	 * | &lt;moveto action&gt;
	 * | &lt;turn action&gt;
	 * | &lt;turnto action&gt;
	 * | &lt;line action&gt;</pre>
	 * @return <code>true</code> if an action is recognized.
	 */
	public boolean isAction() {
		if (isMoveAction() || isMoveToAction() || isTurnAction() ||
				isTurnToAction() || isLineAction()) {
			return true;
		}
	    return false;
	}
	
	/**
	 * Tries to build an &lt;allbugs code&gt; on the global stack.
	 * <pre>&lt;allbugs code&gt; ::= "Allbugs"  "{" &lt;eol&gt;
	 * { &lt;var declaration&gt; }
	 * { &lt;function definition&gt; }
	 * "}" &lt;eol&gt;</pre>
	 * @return <code>true</code> if an allbugs code is recognized.
	 */
	public boolean isAllbugsCode() {
		if (!keyword("Allbugs")) return false;
		if (!symbol("{")) error("Missing '{'");
		stack.pop();							// toss the {
		if (!isEol()) error("Missing EOL");
		pushNewNode("list");					// start the var decl'n list
		while (isVarDeclaration()) makeTree(2, 1);
		pushNewNode("list");					// start the fn decl'n list
		while (isFunctionDefinition()) makeTree(2, 1);
		if (!symbol("}")) error("Missing '}'");
		stack.pop();							// toss the }
		if (!isEol()) error("Missing EOL");
		// Should have 3: Allbugs keyword, 2: var decl'n list, 3: fn decl'n list
		makeTree(3, 2, 1);
	    return true;
	}
	
	/**
	 * Tries to build an &lt;assignment statement&gt; on the global stack.
	 * <pre>&lt;assignment statement&gt; ::= &lt;variable&gt; "=" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if an assignment statement is recognized.
	 */
	public boolean isAssignmentStatement() {
		if (!isVariable()) return false;
		if (!symbol("=")) error("Expected '=' after variable name");
		stack.pop();	// we'll add a node "assign"; pop the "="
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
		pushNewNode("assign");
		makeTree(1, 3, 2);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;block&gt; on the global stack.
	 * <pre>&lt;block&gt; ::= "{" &lt;eol&gt; { &lt;command&gt; }  "}" &lt;eol&gt;</pre>
	 * @return <code>true</code> if a block is recognized.
	 */
	public boolean isBlock() {
		if (!symbol("{")) return false;
		stack.pop();							// throw out the {
		if (!isEol()) error("Missing EOL");
		pushNewNode("block");					// start the block
		while (isCommand()) makeTree(2, 1);		// add command to block tree
		if (!symbol("}")) error("Missing '}' after command");
		stack.pop();							// throw out the }
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to build a &lt;bug definition&gt; on the global stack.
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
		// Bug
		if (!keyword("Bug")) return false;
		// name
		if (!name()) error("Invalid name");
		if (!symbol("{")) error("Missing '{'");
		stack.pop();							// toss the {
		if (!isEol()) error("Missing EOL");
		// variable declarations list
		pushNewNode("list");					// start var declarations list
		while (isVarDeclaration()) makeTree(2, 1); // add declarations to list
		// initialization block
		if (!isInitializationBlock()) {
			pushNewNode("initially");
			pushNewNode("block");	// if no init block, make a dummy one
			makeTree(2, 1);
		}
		// command block
		pushNewNode("block");
		if (!isCommand()) error("Missing/invalid command");
		makeTree(2, 1);							// start the command block
		while (isCommand()) makeTree(2, 1);		// add any more commands to block
		// fn definition list
		pushNewNode("list");					// start fn def'n list
		while (isFunctionDefinition()) makeTree(2, 1); // add fn def'ns to list
		if (!symbol("}")) error("Missing '}'");
		stack.pop();							// toss the }
		if (!isEol()) error("Missing EOL");
		// OK. Should have 6: Bug keyword, 5: name, 4: var declarations, 
		// 				   3: init block, 2: command block, 1: fn defns
		makeTree(6, 5, 4, 3, 2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;color statement&gt; on the global stack.
	 * <pre>&lt;color statement&gt; ::= "color" &lt;KEYWORD&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a color statement is recognized.
	 */
	public boolean isColorStatement() {
		if (!keyword("color")) return false;
		if (!nextTokenMatches(Type.KEYWORD)) error("Not a valid keyword");
		//pushBack(); //these 2 lines are for later
		//if (!Token.COLORS.contains(nextToken().value)) error("Invalid color"); 
		if (!isEol()) error("Missing EOL");
		makeTree(2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;command&gt; on the global stack.
	 * <pre>&lt;command&gt; ::= &lt;action&gt;
	 * | &lt;statement&gt;</pre>
	 * @return <code>true</code> if a command is recognized.
	 */
	public boolean isCommand() {
	    return isAction() || isStatement();
	}
	
	/**
	 * Tries to build a &lt;comparator&gt; on the global stack.
	 * <pre>&lt;comparator&gt; ::= "&lt;" | "&lt;=" | "=" | "!=" | "&gt;=" | "&gt;"</pre>
	 * @return <code>true</code> if a comparator is recognized.
	 */
	public boolean isComparator() {
		if (symbol("<")) {
			if (symbol("=")) {
				stack.pop();
				stack.pop(); // replace separate <, = with combined >=
				pushNewNode("<=");
			}
			return true;
		}
		if (symbol(">")) {
			if (symbol("=")) {
				stack.pop();
				stack.pop(); // replace separate >, = with combined >=
				pushNewNode(">=");
			}
			return true;
		}
		if (symbol("!")) {
			if (!symbol("=")) error("No '=' after '!'");
			stack.pop();
			stack.pop();
			pushNewNode("!="); // replace separate !, = with combined !=
			return true;
		}
		if (symbol("=")) {
			return true; 
		}
	    return false;
	}
	
	/**
	 * Tries to build a &lt;do statement&gt; on the global stack.
	 * <pre>&lt;do statement&gt; ::= "do" &lt;variable&gt; [ &lt;parameter list&gt; ] &lt;eol&gt;</pre>
	 * @return <code>true</code> if a do statement is recognized.
	 */
	public boolean isDoStatement() {
		if (!keyword("do")) return false;
		stack.pop();			// per piazza @267, replacing "do"
		pushNewNode("call");	// with "call"
		if (!isVariable()) error("Missing variable after 'do'");
		if (!isParameterList()) pushNewNode("var");
		if (!isEol()) error("Missing EOL");
		makeTree(3, 2, 1);
	    return true;
	}
	
	/**
	 * Tries to recognize an &lt;EOL&gt;.
	 * <pre>&lt;eol&gt; ::= &lt;EOL&gt; { &lt;EOL&gt; } </pre>
	 * @return <code>true</code> if an EOL is recognized.
	 */
	public boolean isEol() {
	    if (!nextTokenMatches(Type.EOL)) {
	    	return false;
	    }
	    stack.pop();
	    isEol();
	    return true;
	}
	
	/**
	 * Tries to build an &lt;exit if statement&gt; on the global stack.
	 * <pre>&lt;exit if statement&gt; ::= "exit" "if" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if an exit if statement is recognized.
	 */
	public boolean isExitIfStatement() {
		if (!keyword("exit")) return false;
		if (!keyword("if")) error("Missing 'if' after 'exit'");
		stack.pop();			//TODO IS THIS REALLY SUPPOSED TO DROP THE IF?
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
		makeTree(2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;function call&gt; on the global stack.
	 * <pre>&lt;function call&gt; ::= &lt;NAME&gt; &lt;parameter list&gt;</pre>
	 * @return <code>true</code> if a function call is recognized.
	 */
	public boolean isFunctionCall() {
		if (!name()/* && !nextTokenMatches(Type.KEYWORD)*/) return false;
		if (!isParameterList()) error("Missing/invalid parameter list");
		pushNewNode("call");
		makeTree(1, 3, 2);
		return true;
	}
	
	/**
	 * Tries to build a &lt;function definition&gt; on the global stack.
	 * <pre>&lt;function definition&gt; ::= "define" &lt;NAME&gt; [ "using" &lt;variable&gt; { "," &lt;variable&gt; }  ] &lt;block&gt;</pre>
	 * @return <code>true</code> if a function definition is recognized.
	 */
	public boolean isFunctionDefinition() {
		if (!keyword("define")) return false;
		if (!name()) error("Invalid name");
		pushNewNode("var");				// instructions call it a var block, so.
		if (keyword("using")) {
			stack.pop();				// get rid of "using"
			if (!isVariable()) error("Invalid variable name");
			makeTree(2, 1);				// add variable to block
			while (symbol(",")) {
				stack.pop();			// toss the comma
				if (!isVariable()) error("Invalid variable name");
				makeTree(2, 1);			// add variable to block
			}
		}
		if (!isBlock()) error("Missing/invalid block");
		// should have 4: "define", 3: name, 2: var block, 1: command block
		makeTree(4, 3, 2, 1);
	    return true;
	}
	
	/**
	 * Tries to build an &lt;initialization block&gt; on the global stack.
	 * <pre>&lt;initialization block&gt; ::= "initially" &lt;block&gt;</pre>
	 * @return <code>true</code> if an initialization block is recognized.
	 */
	public boolean isInitializationBlock() {
		if (!keyword("initially")) return false;
		if (!isBlock()) error("Missing/invalid block");
		makeTree(2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;line action&gt; on the global stack.
	 * <pre>&lt;line action&gt; ::= "line" &lt;expression&gt; ","&lt;expression&gt; ","&lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a line action is recognized.
	 */
	public boolean isLineAction() {
		if (!keyword("line")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!symbol(",")) error("Missing ','");
		stack.pop();
		if (!isExpression()) error("Missing/invalid expression");
		if (!symbol(",")) error("Missing ','");
		stack.pop();
		if (!isExpression()) error("Missing/invalid expression");
		if (!symbol(",")) error("Missing ','");
		stack.pop();
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
		makeTree(5, 4, 3, 2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;loop statement&gt; on the global stack.
	 * <pre>&lt;loop statement&gt; ::= "loop" &lt;block&gt;</pre>
	 * @return <code>true</code> if a loop statement is recognized.
	 */
	public boolean isLoopStatement() {
		if (!keyword("loop")) return false;
		if (!isBlock()) error("Missing/invalid block");
		makeTree(2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;move action&gt; on the global stack.
	 * <pre>&lt;move action&gt; ::= "move" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a move action is recognized.
	 */
	public boolean isMoveAction() {
		if (!keyword("move")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
		makeTree(2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;moveto action&gt; on the global stack.
	 * <pre>&lt;moveto action&gt; ::= "moveto" &lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a move-to action is recognized.
	 */
	public boolean isMoveToAction() {
		if (!keyword("moveto")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!symbol(",")) error("Missing ',' after expression");
		stack.pop();	// throw out the comma
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
		makeTree(3, 2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;program&gt; on the global stack.
	 * <pre> &lt;program&gt; ::= [ &lt;allbugs code&gt; ]
	 * &lt;bug definition&gt;
	 * { &lt;bug definition&gt; }
	 * &lt;EOF&gt;</pre>
	 * @return <code>true</code> if a program is recognized.
	 */
	public boolean isProgram() {
		pushNewNode("program");
		if (!isAllbugsCode()) {
			pushNewNode("Allbugs");
			pushNewNode("list");
			pushNewNode("list");
			makeTree(3, 2, 1);			// if no Allbugs, make a dummy one
			if (!isBugDefinition()) return false;
		}
		else if (!isBugDefinition()) error("Missing bug definition(s)");
		pushNewNode("list");			// start bug def'n tree
		makeTree(1, 2);
		while (isBugDefinition()) makeTree(2, 1);	// add any more bug defs
		if (!nextTokenMatches(Type.EOF)) error("Invalid program format");
		stack.pop();					// toss the EOF
		// should have 3: program keyword, 2: allbugs, 1: list of bug defs
		makeTree(3, 2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;return statement&gt; on the global stack.
	 * <pre>&lt;return statement&gt; ::= "return" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a return statement is recognized.
	 */
	public boolean isReturnStatement() {
		if (!keyword("return")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
		makeTree(2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;statement&gt; on the global stack.
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
		if (isAssignmentStatement() || isLoopStatement() || 
				isExitIfStatement() || isSwitchStatement() || 
				isReturnStatement() || isDoStatement() || isColorStatement()) {
			return true;
		}
	    return false;
	}
	
	/**
	 * Tries to build a &lt;switch statement&gt; on the global stack.
	 * <pre>&lt;switch statement&gt; ::= "switch" "{" &lt;eol&gt;
	 * { "case" &lt;expression&gt; &lt;eol&gt;
	 * 	   { &lt;command&gt; } }
	 * "}" &lt;eol&gt;</pre>
	 * @return <code>true</code> if a switch statement is recognized.
	 */
	public boolean isSwitchStatement() {
		if (!keyword("switch")) return false;
		if (!symbol("{")) error("Missing '{'");
		stack.pop();							// throw out the {
		if (!isEol()) error("Missing EOL");
		while (keyword("case")) {
			if (!isExpression()) error("Missing/invalid expression");
			if (!isEol()) error("Missing EOL");
			pushNewNode("block");				// start command block
			while (isCommand()) makeTree(2, 1); // adds command to block
			makeTree(3, 2, 1); 					// builds "case" tree
			makeTree(2, 1); 					// add this case to switch tree
		}
		if (!symbol("}")) error("Missing '}'");
		stack.pop();							// throw out the }
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	/**
	 * Tries to build a &lt;turn action&gt; on the global stack.
	 * <pre>&lt;turn action&gt; ::= "turn" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a turn action is recognized.
	 */
	public boolean isTurnAction() {
		if (!keyword("turn")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
		makeTree(2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;turnto action&gt; on the global stack.
	 * <pre>&lt;turnto action&gt; ::= "turnto" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a turn-to action is recognized.
	 */
	public boolean isTurnToAction() {
		if (!keyword("turnto")) return false;
		if (!isExpression()) error("Missing/invalid expression");
		if (!isEol()) error("Missing EOL");
		makeTree(2, 1);
	    return true;
	}
	
	/**
	 * Tries to build a &lt;var declaration&gt; on the global stack.
	 * <pre>&lt;var declaration&gt; ::= "var" &lt;NAME&gt; { "," &lt;NAME&gt; } &lt;eol&gt;</pre>
	 * @return <code>true</code> if a var declaration is recognized.
	 */
	public boolean isVarDeclaration() {
		if (!keyword("var")) return false;
		if (!name()) error("Invalid variable name");
		makeTree(2, 1);
		while (symbol(",")) {
			stack.pop();	// throw out the comma
			if (!name()) error("Invalid variable name");
			makeTree(2, 1);
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