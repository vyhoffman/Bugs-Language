package bugs;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import bugs.Token.Type;
/**
* This class consists of a number of methods that "recognize" strings
* composed of Tokens that follow the indicated grammar rules for each
* method.
* <p>Each method may have one of three outcomes:
* <ul>
* <li>The method may succeed, returning <code>true</code> and
* consuming the tokens that make up that particular nonterminal.</li>
* <li>The method may fail, returning <code>false</code> and not
* consuming any tokens.</li>
* <li>(Some methods only) The method may determine that an
* unrecoverable error has occurred and throw a
* <code>SyntaxException</code></li>.
* </ul>
* @author Nicki Hoffman
* @author David Matuszek
* @version February 2015
*/
public class Recognizer {
	StreamTokenizer tokenizer = null;
	int lineNumber;
	/**
	* Constructs a Recognizer for the given string.
	* @param text The string to be recognized.
	*/
	public Recognizer(String text) {
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
	/**
	* Tries to recognize an &lt;expression&gt;.
	* <pre>&lt;expression&gt; ::= &lt;arithmetic expression&gt; {  &lt;comparator&gt; &lt;arithmetic expression&gt; }</pre>
	* @return <code>true</code> if an expression is recognized.
	*/
	public boolean isExpression() {
		if (!isArithmeticExpression()) return false;
		while (isComparator()) {
			if(!isArithmeticExpression()) error("Invalid expression");
		}
		return true;
	}
	/**TODO check this
	* Tries to recognize an &lt;arithmetic expression&gt;.
	* <pre>&lt;arithmetic expression&gt; ::= [ &lt;addOperator&gt; ] &lt;term&gt; { &lt;add operator&gt; &lt;term&gt; }</pre>
	* A <code>SyntaxException</code> will be thrown if the add_operator
	* is present but not followed by a valid &lt;expression&gt;.
	* @return <code>true</code> if an arithmetic expression is recognized.
	*/
	public boolean isArithmeticExpression() {
		if (!isTerm()) return false;
		while (isAddOperator()) {
			if (!isTerm()) error("Error in expression after '+' or '-'");
		}
		return true;
	}
	/**
	* Tries to recognize a &lt;term&gt;.
	* <pre>&lt;term&gt; ::= &lt;factor&gt; { &lt;multiply_operator&gt; &lt;term&gt;}</pre>
	* A <code>SyntaxException</code> will be thrown if the multiply_operator
	* is present but not followed by a valid &lt;term&gt;.
	* @return <code>true</code> if a term is recognized.
	*/
	public boolean isTerm() {
		if (!isFactor()) return false;
		while (isMultiplyOperator()) {
			if (!isTerm()) error("No term after '*' or '/'");
		}
		return true;
	}
	/**
	* Tries to recognize a &lt;factor&gt;.
	* <pre>&lt;factor&gt; ::= [ &lt;add operator&gt; ] &lt;unsigned factor&gt;</pre>
	* @return <code>true</code> if a factor is parsed.
	*/
	public boolean isFactor() {
		if(symbol("+") || symbol("-")) {
			if (isUnsignedFactor()) {
				return true;
			}
			error("No factor following unary plus or minus");
			return false; // Can't ever get here
		}
		return isUnsignedFactor();
	}
	/**
	* Tries to recognize an &lt;unsigned factor&gt;.
	* <pre>&lt;factor&gt; ::= &lt;name&gt; "." &lt;name&gt;
	* | &lt;name&gt; "(" &lt;parameter list&gt; ")"
	* | &lt;name&gt;
	* | &lt;number&gt;
	* | "(" &lt;expression&gt; ")"</pre>
	* A <code>SyntaxException</code> will be thrown if the opening
	* parenthesis is present but not followed by a valid
	* &lt;expression&gt; and a closing parenthesis.
	* @return <code>true</code> if a factor is recognized.
	*/
	public boolean isUnsignedFactor() {
		if (isVariable()) {
			if (symbol(".")) { // reference to another Bug
				if (name()) return true;
				error("Incorrect use of dot notation");
			}
			else if (isParameterList()) return true; // function call
			else return true; // just a variable
		}
		if (number()) return true;
		if (symbol("(")) {
			if (!isExpression()) error("Error in parenthesized expression");
			if (!symbol(")")) error("Unclosed parenthetical expression");
			return true;
		}
		return false;
	}
	/**
	* Tries to recognize a &lt;parameter list&gt;.
	* <pre>&ltparameter list&gt; ::= "(" [ &lt;expression&gt; { "," &lt;expression&gt; } ] ")"</pre>
	* @return <code>true</code> if a parameter list is recognized.
	*/
	public boolean isParameterList() {
		if (!symbol("(")) return false;
		if (isExpression()) {
			while (symbol(",")) {
				if (!isExpression()) error("No expression after ','");
			}
		}
		if (!symbol(")")) error("Parameter list doesn't end with ')'");
		return true;
	}
	/**
	* Tries to recognize an &lt;add_operator&gt;.
	* <pre>&lt;add_operator&gt; ::= "+" | "-"</pre>
	* @return <code>true</code> if an addop is recognized.
	*/
	public boolean isAddOperator() {
		return symbol("+") || symbol("-");
	}
	/**
	* Tries to recognize a &lt;multiply_operator&gt;.
	* <pre>&lt;multiply_operator&gt; ::= "*" | "/"</pre>
	* @return <code>true</code> if a multiply_operator is recognized.
	*/
	public boolean isMultiplyOperator() {
		return symbol("*") || symbol("/");
	}
	/**
	* Tries to recognize a &lt;variable&gt;.
	* <pre>&lt;variable&gt; ::= &lt;NAME&gt;</pre>
	* @return <code>true</code> if a variable is recognized.
	*/
	public boolean isVariable() {
		return name();
	}
	
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
		if (!keyword("color")) return false;
		if (!nextTokenMatches(Type.KEYWORD)) error("Not a valid keyword");
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
	    return isAction() || isStatement();
	}
	
	/**
	 * Tries to recognize a &lt;comparator&gt;.
	 * <pre>&lt;comparator&gt; ::= "&lt;" | "&lt;=" | "=" | "!=" | "&gt;=" | "&gt;"</pre>
	 * @return <code>true</code> if a comparator is recognized.
	 */
	public boolean isComparator() {
		if (symbol("<")) {
			if (symbol("="));
			return true;
		}
		if (symbol(">")) {
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
		if (!isAllbugsCode()) {
			if (!isBugDefinition()) return false;
		}
		else if (!isBugDefinition()) error("Missing bug definition(s)");
		while (isBugDefinition());
		if (!nextTokenMatches(Type.EOF)) error("Invalid program format");
	    return true;
	}
	
	/**
	 * Tries to recognize a &lt;return statement&gt;.
	 * <pre>&lt;return statement&gt; ::= "return" &lt;expression&gt; &lt;eol&gt;</pre>
	 * @return <code>true</code> if a return statement is recognized.
	 */
	public boolean isReturnStatement() {
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
		if (!keyword("var")) return false;
		if (!name()) error("Invalid variable name");
		while (symbol(",")) {
			if (!name()) error("Invalid variable name");
		}
		if (!isEol()) error("Missing EOL");
	    return true;
	}
	
	
	//----- Private "helper" methods
	/**
	* Tests whether the next token is a number. If it is, the token
	* is consumed, otherwise it is not.
	*
	* @return <code>true</code> if the next token is a number.
	*/
	private boolean number() {
		return nextTokenMatches(Token.Type.NUMBER);
	}
	/**
	* Tests whether the next token is a name. If it is, the token
	* is consumed, otherwise it is not.
	*
	* @return <code>true</code> if the next token is a name.
	*/
	private boolean name() {
		return nextTokenMatches(Token.Type.NAME);
	}
	/**
	* Tests whether the next token is the expected name. If it is, the token
	* is consumed, otherwise it is not.
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
	* the token is consumed, otherwise it is not.
	*
	* @param expectedSymbol The String value of the token we expect
	* to encounter next.
	* @return <code>true</code> if the next token is the expected symbol.
	*/
	boolean symbol(String expectedSymbol) {
		return nextTokenMatches(Token.Type.SYMBOL, expectedSymbol);
	}
	/**
	* Tests whether the next token has the expected type. If it does,
	* the token is consumed, otherwise it is not. This method would
	* normally be used only when the token's value is not relevant.
	*
	* @param type The expected type of the next token.
	* @return <code>true</code> if the next token has the expected type.
	*/
	boolean nextTokenMatches(Token.Type type) {
		Token t = nextToken();
		if (t.type == type) return true;
		pushBack();
		return false;
	}
	/**
	* Tests whether the next token has the expected type and value.
	* If it does, the token is consumed, otherwise it is not. This
	* method would normally be used when the token's value is
	* important.
	*
	* @param type The expected type of the next token.
	* @param value The expected value of the next token; must
	* not be <code>null</code>.
	* @return <code>true</code> if the next token has the expected type.
	*/
	boolean nextTokenMatches(Token.Type type, String value) {
		Token t = nextToken();
		if (type == t.type && value.equals(t.value)) return true;
		pushBack();
		return false;
	}
	/**
	* Returns the next Token.
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
				return new Token(Token.Type.EOL, "\n");
			case StreamTokenizer.TT_EOF:
				return new Token(Token.Type.EOF, "EOF");
			default:
				return new Token(Token.Type.SYMBOL, ((char) code) + "");
		}
	}
	/**
	* Returns the most recent Token to the tokenizer.
	*/
	void pushBack() {
		tokenizer.pushBack();
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