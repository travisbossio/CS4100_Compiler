/* 
 * Travis Bossio CS4100 Homework 5, 6 and 7, Spring 2023
 *
 *	CFG For Language Used in Syntactic
 *
 *	<program> = $UNIT <prog-identifier> $SEMICOLON <block>$PERIOD
 *  <block> = {<variable-dec-sec>}* <block-body>
 *	<block-body> = $BEGIN <statement> {$SEMICOLON <statement>}*
 *	<variable-dec-sec> = $VAR <variable-declaration>
 *	<variable-declaration> = {<identifier>  {$COMMA  <identifier>}* $COLON  <simple type>  $SEMICOLON}+
 *	<prog-identifier> = <identifier>
 *	<statement> = [ <variable>  $ASSIGN  (<simple expression> | <string literal>) | 
 *		<block-body> | $IF  <relexpression>  $THEN  <statement>[$ELSE  <statement>] | 
 *		$DOWHILE  <relexpression> <statement> | $REPEAT  <statement>  $UNTIL <relexpression> | 
 *		$FOR  <variable>  $ASSIGN  <simple expression> $TO <simple expression> $DO <statement> | 
 *		$WRITELN  $LPAR (<simple expression> | <identifier> |<string constant> ) $RPAR |
 *		$READLN  $LPAR <identifier> $RPAR]+
 *	<variable> = <identifier>
 *	<relexpression> = <simple expression>  <relop>  <simple expression>
 *  <relop> = $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ
 *	<simple expression> = [<sign>] <term> {<addop>  <term>}*
 *	<addop> = $PLUS | $MINUS
 *	<sign> = $PLUS | $MINUS
 *	<term> = <factor> {<mulop> <factor> }*
 *	<mulop> = $MULTIPLY | $DIVIDE
 *	<factor> = <unsigned constant> | <variable> | $LPAR <simple expression> $RPAR
 *  <simple type> = $INTEGER | $FLOAT | $STRING
 *	<unsigned constant> = <unsigned number>
 *	<unsigned number> = $FLOAT | $INTEGER
 *	<identifier> = $IDENTIFIER
 *	<string constant> = $STRINGC
 */

package ADT;

/**
 * @author abrouill edited by Travis Bossio
 */
public class Syntactic {

	private String filein; // The full file path to input file
	private SymbolTable symbolList; // Symbol table storing ident/const
	private QuadTable quadList; // Quad table storing quads during code generation
	private Lexical lex; // Lexical analyzer
	private Lexical.token token; // Next Token retrieved
	private boolean traceOn; // Controls tracing mode
	private int level = 0; // Controls indent for trace mode
	private boolean anyErrors; // Set TRUE if an error happens
	private boolean globalError = false;
	private Interpreter interp;

	private final int symbolSize = 250;
	private final int quadSize = 1000;

	private int Minus1Index;
	private int Plus1Index;
	private final char CONS_USE = 'c';
	private final char VAR_USE = 'v';
	private final String TEMP_STR = "@";
	private int tempCount = 0;
	private final int TEMP_VAL = 0;

	// public constructor for initializing syntactic variables
	public Syntactic(String filename, boolean traceOn) {
		filein = filename;
		this.traceOn = traceOn;
		symbolList = new SymbolTable(symbolSize);
		Minus1Index = symbolList.AddSymbol("-1", CONS_USE, -1);
		Plus1Index = symbolList.AddSymbol("1", CONS_USE, 1);

		quadList = new QuadTable(quadSize);
		interp = new Interpreter();

		lex = new Lexical(filein, symbolList, true);
		lex.setPrintToken(traceOn);
		anyErrors = false;
	}

	// The interface to the syntax analyzer, initiates parsing
	// Uses variable RECUR to get return values throughout the non-terminal methods
	public void parse() {

		String filenameBase = filein.substring(0, filein.length() - 4);
		System.out.println("File Name Base: " + filenameBase);

		// prime the pump to get the first token to process
		token = lex.GetNextToken();

		// call PROGRAM
		Program();

		// Done with recursion, so add the final STOP quad
		quadList.AddQuad(interp.opcodeFor("STOP"), 0, 0, 0);

		// print SymbolTable before execute
		symbolList.PrintSymbolTable(filenameBase + "ST-before.txt");
		// print quadtable
		quadList.PrintQuadTable(filenameBase + "QUADS.txt");
		// interpret
		if (!globalError) {
			interp.InterpretQuads(quadList, symbolList, traceOn, filenameBase + "TRACE.txt");
		} else {
			System.out.println("Errors, unable to run program.");
		}
		symbolList.PrintSymbolTable(filenameBase + "ST-after.txt");
	}

	// Non Terminal progidentifier R is fully implemented here, leave it as-is.
	/* <identifier> */
	private int ProgIdentifier() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		// This non-term is used to uniquely mark the program identifier
		if (isIdentifier(token)) {
			// Because this is the progIdentifier, it will get a 'P' type to prevent re-use
			// as a var
			symbolList.UpdateSymbol(symbolList.LookupSymbol(token.lexeme), 'P', 0);

			// move on
			token = lex.GetNextToken();
		}

		return recur;
	}

	// Non Terminal program is fully implemented here.
	/* $UNIT <prog-identifier> $SEMICOLON <block>$PERIOD */
	private int Program() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Program", true);

		// if $UNIT starts program
		if (token.code == lex.codeForMnemonic("UNIT_")) {
			token = lex.GetNextToken();
			// recurse down prog-identifier
			recur = ProgIdentifier();
			// if $SEMICOLON follows <prog-identifier>
			if (token.code == lex.codeForMnemonic("SMCLN")) {
				token = lex.GetNextToken();
				// recurse down block
				recur = Block();
				// if program ends with $PERIOD success
				if (token.code == lex.codeForMnemonic("PRIOD")) {
					if (!anyErrors) {
						System.out.println("Success.");
					} else if (globalError && !anyErrors) {
						System.out.println("Compilation failed at statement level. No code generation.");
					} else {
						System.out.println("Compilation failed.");
					}
				} else {
					error(lex.reserveForMnemonic("PRIOD"), token.lexeme);
				}
			} else {
				error(lex.reserveForMnemonic("SMCLN"), token.lexeme);
			}
		} else {
			error(lex.reserveForMnemonic("UNIT_"), token.lexeme);
		}

		trace("Program", false);

		return recur;
	}

	// Non Terminal block body is fully implemented here.
	/* $BEGIN <statement> {$SEMICOLON <statement>}* $END */
	private int BlockBody() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}
		trace("BlockBody", true);

		// if $BEGIN starts block
		if (token.code == lex.codeForMnemonic("BEGIN")) {
			token = lex.GetNextToken();
			// recurse down statement
			recur = Statement();
			// if error in statement attempt to resynch it
			if (anyErrors) {
				resynchStatement();
				recur = Statement();
			}
			// while there is semicolon get next token and recurse down statement
			while ((token.code == lex.codeForMnemonic("SMCLN")) && (!lex.EOF()) && (!anyErrors)) {
				token = lex.GetNextToken();
				recur = Statement();
				// if error in statement attempt to resynch it
				if (anyErrors) {
					resynchStatement();
					recur = Statement();
				}
			}
			// if $END after last statement
			if (token.code == lex.codeForMnemonic("_END_")) {
				// bookkeeping and move on
				token = lex.GetNextToken();
			} else {
				error(lex.reserveForMnemonic("_END_"), token.lexeme);
			}
		} else {
			error(lex.reserveForMnemonic("BEGIN"), token.lexeme);
		}

		trace("BlockBody", false);

		return recur;
	}

	// Non Terminal block is fully implemented here.
	/* {<variable-dec-sec>}* <block-body> */
	private int Block() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}
		trace("Block", true);

		// while variable is being declared recursively call Variable-Dec-Sec
		// variable declaration starts with $VAR
		while (token.code == lex.codeForMnemonic("_VAR_") && (!lex.EOF()) && (!anyErrors)) {
			recur = VariableDecSec();
		}

		// sets all declared variables as declared
		for (int i = 0; i < symbolList.currElement; i++) {
			symbolList.setVariableAsDeclared(i);
		}

		// recurse down block body
		recur = BlockBody();

		trace("Block", false);

		return recur;
	}

	// Non Terminal Variable-Dec-Sec is fully implemented here.
	/* $VAR <variable-declaration> */
	private int VariableDecSec() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}
		trace("VariableDecSec", true);

		// $VAR is handled by <block>
		// recurse down variable declaration
		recur = VariableDeclaration();

		trace("VariableDecSec", false);

		return recur;
	}

	// Non Terminal Variable-Declaration is fully implemented here.
	/* {<identifier> {$COMMA <identifier>}* $COLON <simple type> $SEMICOLON}+ */
	private int VariableDeclaration() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("VariableDeclaration", true);

		token = lex.GetNextToken();

		// {<identifier> {$COMMA <identifier>}* $COLON <simple type> $SEMICOLON}+
		if (isIdentifier(token)) {
			while (isIdentifier(token) && (!lex.EOF()) && (!anyErrors)) {

				token = lex.GetNextToken();

				// {$COMMA <identifier>}*
				while (token.code == lex.codeForMnemonic("COMMA") && (!lex.EOF()) && (!anyErrors)) {
					token = lex.GetNextToken();
					// expecting <identifier>
					if (isIdentifier(token)) {
						token = lex.GetNextToken();
					} else {
						error(lex.reserveForMnemonic("IDENT"), token.lexeme);
					}
				}

				// if $COLON
				if (token.code == lex.codeForMnemonic("COLON")) {
					token = lex.GetNextToken();

					// recurse down simple type
					recur = SimpleType();

					// if $SEMICOLON
					if (token.code == lex.codeForMnemonic("SMCLN")) {
						// bookkeeping and move on
						token = lex.GetNextToken();
					} else {
						error(lex.reserveForMnemonic("SMCLN"), token.lexeme);
					}
				} else {
					error(lex.reserveForMnemonic("COLON"), token.lexeme);
				}
			}
		} else {
			error(lex.reserveForMnemonic("IDENT"), token.lexeme);
		}

		trace("VariableDeclaration", false);

		return recur;
	}

	// Not a NT, but used to shorten Statement code body for readability.
	/* $COLON-EQUALS (<simple expression> | <string literal>) */
	// note <variable> is handled in Statement() call before this method
	private int handleAssignment() {
		int left, right;
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("handleAssignment", true);

		// extract left ident S.T. index
		left = Variable();

		// if $COLON-EQUALS assignment token aka ":="
		if (token.code == lex.codeForMnemonic("ASSGN")) {
			token = lex.GetNextToken();
			// add simple expression quad and get S.T. index of result
			right = SimpleExpression();
			// assignment quad
			quadList.AddQuad(interp.opcodeFor("MOV"), right, 0, left);
		} else {
			error(lex.reserveForMnemonic("ASSGN"), token.lexeme);
		}

		trace("handleAssignment", false);

		return recur;
	}

	// handle do while loop in statement
	/* $DOWHILE <relexpression> <statement> */
	private int handleDoWhile() {
		int branchRelop, saveTop;
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("handleDoWhile", true);

		// $DOWHILE found in Statement move to next token
		token = lex.GetNextToken();

		saveTop = quadList.NextQuad();
		// extract relational quad
		branchRelop = RelExpression();

		// add do while statement quads
		recur = Statement();

		// add unconditional jump to top of loop
		quadList.AddQuad(interp.opcodeFor("JMP"), 0, 0, saveTop);

		// set target outside of loop
		quadList.UpdateJump(branchRelop, quadList.NextQuad());

		trace("handleDoWhile", false);

		return recur;
	}

	// handle if statement
	/* $IF <relexpression> $THEN <statement>[$ELSE <statement>] */
	private int handleIf() {
		int branchQuad, elseQuad;
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("handleIf", true);

		// $IF found in Statement move to next token
		token = lex.GetNextToken();

		// get branch quad index, and add relational quads
		branchQuad = RelExpression();

		// if $THEN
		if (token.code == lex.codeForMnemonic("THEN_")) {
			token = lex.GetNextToken();

			// add if statement quads
			recur = Statement();

			// optional [$ELSE <statement>] in an if statement
			if (token.code == lex.codeForMnemonic("ELSE_")) {
				token = lex.GetNextToken();

				// save else unconditional jump quad index
				elseQuad = quadList.NextQuad();
				// unconditional jump skipping if branch
				quadList.AddQuad(interp.opcodeFor("JMP"), 0, 0, 0);

				// set if branch jump target
				quadList.UpdateJump(branchQuad, quadList.NextQuad());

				// add else statement quads
				recur = Statement();

				// set else branch jump target
				quadList.UpdateJump(elseQuad, quadList.NextQuad());
			} else {
				// set if branch jump target
				quadList.UpdateJump(branchQuad, quadList.NextQuad());
			}
		} else {
			error(lex.reserveForMnemonic("THEN_"), token.lexeme);
		}

		trace("handleIf", false);

		return recur;
	}

	// handle repeat in statement
	/* $REPEAT <statement> $UNTIL <relexpression> */
	private int handleRepeat() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("handleRepeat", true);

		// move on from $REPEAT
		token = lex.GetNextToken();

		// recurse down statement
		recur = Statement();

		// if $LPARA
		if (token.code == lex.codeForMnemonic("UNTIL")) {
			token = lex.GetNextToken();
			// recurse down relexpression
			recur = RelExpression();
		} else {
			error(lex.reserveForMnemonic("UNTIL"), token.lexeme);
		}

		trace("handleRepeat", false);

		return recur;
	}

	// handle for loop in statement
	/*
	 * $FOR <variable> $ASSIGN <simple expression> $TO <simple expression> $DO
	 * <statement>
	 */
	private int handleForLoop() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("handleForLoop", true);

		token = lex.GetNextToken();

		// recurse down variable
		recur = Variable();

		// if $ASSIGN
		if (token.code == lex.codeForMnemonic("ASSGN")) {
			token = lex.GetNextToken();

			// recurse down simple expression
			recur = SimpleExpression();
			// if $TO
			if (token.code == lex.codeForMnemonic("_TO__")) {
				token = lex.GetNextToken();

				// recurse down simple expression
				recur = SimpleExpression();
				// if $DO
				if (token.code == lex.codeForMnemonic("_DO__")) {
					token = lex.GetNextToken();

					// recurse down statement
					recur = Statement();
				} else {
					error(lex.reserveForMnemonic("_DO__"), token.lexeme);
				}
			} else {
				error(lex.reserveForMnemonic("_TO__"), token.lexeme);
			}
		} else {
			error(lex.reserveForMnemonic("ASSGN"), token.lexeme);
		}

		trace("handleForLoop", false);

		return recur;
	}

	// handle writeln statement
	/*
	 * $WRITELN $LPAR (<simple expression> | <identifier> | <string constant>) $RPAR
	 */
	private int handleWriteLn() {
		int recur = 0;
		int toprint = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("handleWriteLn", true);

		// $WRITELN found in Statement move to next token
		token = lex.GetNextToken();

		// if $LPARA
		if (token.code == lex.codeForMnemonic("LPARA")) {
			token = lex.GetNextToken();

			// (NOTE* IGNORE <simple expression> | <identifier> | <string constant>)
			if (token.code == lex.codeForMnemonic("STRC_")) {
				// recurse down string constant
				// returns index of string constant in S.T.
				toprint = StringConstant();
			} else if (token.code == lex.codeForMnemonic("IDENT")) {
				// find index of identifier to be printed
				toprint = symbolList.LookupSymbol(token.lexeme);
				// bookkeeping and move on
				token = lex.GetNextToken();
			} else {
				error("identifier or string constant", token.lexeme);
			}

			// add quad for writeln
			quadList.AddQuad(interp.opcodeFor("PRINT"), 0, 0, toprint);

			// if $RPARA
			if (token.code == lex.codeForMnemonic("RPARA")) {
				// bookkeeping and move on
				token = lex.GetNextToken();
			} else {
				error(lex.reserveForMnemonic("RPARA"), token.lexeme);
			}
		} else {
			error(lex.reserveForMnemonic("LPARA"), token.lexeme);
		}

		trace("handleWriteLn", false);

		return recur;
	}

	// handle for loop in statement
	/* $READLN $LPAR <identifier> $RPAR */
	private int handleReadln() {
		int recur = 0;
		int toRead = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("handleReadln", true);

		// $READ found in Statement move to next token
		token = lex.GetNextToken();

		// if $LPARA
		if (token.code == lex.codeForMnemonic("LPARA")) {
			token = lex.GetNextToken();

			// if <identifier>
			if (isIdentifier(token)) {
				toRead = symbolList.LookupSymbol(token.lexeme);
				// variables are not required to be declared
				token = lex.GetNextToken();

				// add READ quad reading variables contents
				quadList.AddQuad(interp.opcodeFor("READ"), 0, 0, toRead);
				// if $RPARA
				if (token.code == lex.codeForMnemonic("RPARA")) {
					// bookkeeping and move on
					token = lex.GetNextToken();
				} else {
					error(lex.reserveForMnemonic("RPARA"), token.lexeme);
				}
			} else {
				error(lex.reserveForMnemonic("IDENT"), token.lexeme);
			}
		} else {
			error(lex.reserveForMnemonic("LPARA"), token.lexeme);
		}

		trace("handleReadln", false);

		return recur;
	}

	// Non-terminal relational expression searching for a relational
	// operation in between two simple expressions
	/* <simple expression> <relop> <simple expression> */
	private int RelExpression() {
		int left, right, saveRelop, result, temp;

		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("RelExpression", true);

		// extract left simple expression adding its quads
		left = SimpleExpression();

		// get relational operation mnemonic code
		saveRelop = Relop();

		// extract right simple expression adding its quads
		right = SimpleExpression();

		// new index temp from S.T.
		temp = GenSymbol();

		// subtract right from left simple expression into temp
		quadList.AddQuad(interp.opcodeFor("SUB"), left, right, temp);

		// save relational quad
		result = quadList.NextQuad();

		// add relational quad and save jump target for if or dowhile
		quadList.AddQuad(relopToOpcode(saveRelop), temp, 0, 0);

		trace("RelExpression", false);

		return result;
	}

	// Non-terminal simple expression searching for an optional sign
	// before the term followed by zero or more addops and terms
	/* [<sign>] <term> {<addop> <term>}* */
	private int SimpleExpression() {
		int left, right, signval, temp, opcode;
		signval = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("SimpleExpression", true);

		// optional call sign if sign
		if (isSignOrAddop(token)) {
			// extract sign -1 if negative 1 if positive
			signval = Sign();
		}

		// extract left side term from S.T.
		left = Term();

		// add a negation quad for negative sign
		if (signval == -1) {
			quadList.AddQuad(interp.opcodeFor("MUL"), left, Minus1Index, left);
		}

		// while there is $PLUS or $MINUS token recurse down addop then term
		while (isSignOrAddop(token) && (!lex.EOF()) && (!anyErrors)) {
			opcode = Addop(); // extract addition/subtraction mnemonic code
			right = Term(); // extract right side term from S.T.
			temp = GenSymbol(); // new index temp from S.T.

			quadList.AddQuad(opcode, left, right, temp); // add quad for math operation
			left = temp; // new leftmost term is last result
		}

		trace("SimpleExpression", false);

		return left;
	}

	// Non-Terminal looking for various types of statements
	// includes: assignments, if/else statements, do while loops, repeat statements,
	// for loops, writeln, and readln.
	/*
	 * { [ <variable> $ASSIGN (<simple expression> | <string literal>) |
	 * <block-body> | $IF <relexpression> $THEN <statement>[$ELSE <statement>] |
	 * $DOWHILE <relexpression> <statement> | $REPEAT <statement> $UNTIL
	 * <relexpression> | $FOR <variable> $ASSIGN <simple expression> $TO <simple
	 * expression> $DO <statement> | $WRITELN $LPAR (<simple expression> |
	 * <identifier> |<string constant> ) $RPAR $READLN $LPAR <identifier> $RPAR]+
	 */
	private int Statement() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Statement", true);

		boolean isStatementBool = isStatement(token);

		// check to make sure statement starts with valid option
		if (isStatementBool) {
			// while statement has valid options to recurse down
			while (isStatementBool && !anyErrors) {
				// if <variable>
				if (isIdentifier(token)) { // must be an ASSIGNMENT
					// recurse down handle assignment
					recur = handleAssignment();
				} else if (token.code == lex.codeForMnemonic("BEGIN")) { // if <block-body>
					// recurse down handle assignment
					recur = BlockBody();
				} else if (token.code == lex.codeForMnemonic("_IF__")) { // if if statement
					// recurse down if statement
					recur = handleIf();
				} else if (token.code == lex.codeForMnemonic("DWHLE")) { // if dowhile loop
					// recurse down do while loop statement
					recur = handleDoWhile();
				} else if (token.code == lex.codeForMnemonic("RPEAT")) { // if repeat
					// recurse down repeat statement
					recur = handleRepeat();
				} else if (token.code == lex.codeForMnemonic("_FOR_")) { // if for loop
					// recurse down for loop statement
					recur = handleForLoop();
				} else if (token.code == lex.codeForMnemonic("WRITE")) { // if writeln
					// recurse down writeln statement
					recur = handleWriteLn();
				} else if (token.code == lex.codeForMnemonic("READ_")) { // if readln
					// recurse down readln statement
					recur = handleReadln();
				}

				// check if next token is still a statement
				isStatementBool = isStatement(token);
			}
		} else {
			error("Statement start", token.lexeme);
		}

		trace("Statement", false);

		return recur;
	}

	// method for resynching error back to a valid starting statement
	public void resynchStatement() {
		// set global error that way it can stop code generation
		globalError = true;

		trace("Resynch", true);

		// while syntactic is unsynched and not at end of program
		while (anyErrors && token.code != lex.codeForMnemonic("PRIOD")) {
			token = lex.GetNextToken();
			boolean isStatementBool = isStatement(token);
			// check to make sure statement starts with valid option
			if (isStatementBool) {
				if (token.code == lex.codeForMnemonic("IDENT")
						&& lex.GetNextToken().code == lex.codeForMnemonic("ASSGN")) { // must be an ASSIGNMENT
					anyErrors = false;
				} else if (token.code == lex.codeForMnemonic("BEGIN")) { // if <block-body>
					anyErrors = false;
				} else if (token.code == lex.codeForMnemonic("_IF__")) { // if if statement
					anyErrors = false;
				} else if (token.code == lex.codeForMnemonic("DWHLE")) { // if dowhile loop
					anyErrors = false;
				} else if (token.code == lex.codeForMnemonic("RPEAT")) { // if repeat
					anyErrors = false;
				} else if (token.code == lex.codeForMnemonic("_FOR_")) { // if for loop
					anyErrors = false;
				} else if (token.code == lex.codeForMnemonic("WRITE")) { // if writeln
					anyErrors = false;
				} else if (token.code == lex.codeForMnemonic("READ_")) { // if readln
					anyErrors = false;
				}
			}
		}
		trace("Resynch", false);
	}

	// Non-terminal variable just looks for an IDENTIFIER.
	// returns index in S.T. for variable
	/* <identifier> */
	private int Variable() {
		int varIndex = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Variable", true);

		// if $IDENTIFIER token
		if ((isIdentifier(token))) {
			// Variables are not required to be declared

			// get index of variable
			varIndex = symbolList.LookupSymbol(token.lexeme);

			// bookkeeping and move on
			token = lex.GetNextToken();
		} else {
			error("Variable", token.lexeme);
		}

		trace("Variable", false);

		return varIndex;
	}

	// Non-terminal term that looks for one or more factors
	/* <factor> {<mulop> <factor> }* */
	private int Term() {
		int left, right, opcode, temp;

		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Term", true);

		// get left factor index in S.T.
		left = Factor();

		// while there is $MULTIPLY or $DIDIVE token recurse down mulop then factor
		while (isMulop(token) && (!lex.EOF()) && (!anyErrors)) {
			opcode = Mulop(); // MUL or DIV opcode
			right = Factor(); // get right factor index in S.T.
			temp = GenSymbol(); // generate temp in S.T.
			quadList.AddQuad(opcode, left, right, temp); // add math operation quad
			left = temp; // set left side of math operation to temp
		}

		trace("Term", false);

		return left;
	}

	// Non-terminal factor that looks for a constant, variable, or parenthesis and
	// simple expression
	/* <unsigned constant> | <variable> | $LPAR <simple expression> $RPAR */
	private int Factor() {
		int factorIndex = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Factor", true);

		// if unsigned constant
		if ((token.code == lex.codeForMnemonic("FLTC_")) || (token.code == lex.codeForMnemonic("INTC_"))) {
			// recurse down unsigned constant
			factorIndex = UnsignedConstant();
		} else if (isIdentifier(token)) { // else if variable aka identifier
			// recurse down variable
			factorIndex = Variable();
		} else if (token.code == lex.codeForMnemonic("LPARA")) { // else if left parenthesis call simple expression
			token = lex.GetNextToken();
			// recurse down simple expression
			factorIndex = SimpleExpression();
			// if right parenthesis ending factor
			if (token.code == lex.codeForMnemonic("RPARA")) {
				// bookkeeping and move on
				token = lex.GetNextToken();
			} else { // else error
				error("')'", token.lexeme);
			}
		} else {
			error("Number, Variable or '('", token.lexeme);
		}

		trace("Factor", false);

		return factorIndex;
	}

	// Non-terminal sign just looks for an "+" or "-"
	// returns 1 for "+", -1 for "-"
	/* $PLUS | $MINUS */
	private int Sign() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Sign", true);

		// positive sign
		recur = 1;

		// if $PLUS or $MINUS token
		if (token.code == lex.codeForMnemonic("_ADD_")) {
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else if (token.code == lex.codeForMnemonic("SBTRC")) {
			// negative sign
			recur = -1;
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else {
			error("'+' or '-'", token.lexeme);
		}

		trace("Sign", false);

		return recur;
	}

	// Non-terminal addop just looks for an "+" or "-"
	/* $PLUS | $MINUS */
	private int Addop() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Addop", true);

		// if $PLUS or $MINUS token
		if (token.code == lex.codeForMnemonic("_ADD_")) {
			recur = interp.opcodeFor("ADD");
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else if (token.code == lex.codeForMnemonic("SBTRC")) {
			recur = interp.opcodeFor("SUB");
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else {
			error("'+' or '-'", token.lexeme);
		}

		trace("Addop", false);

		return recur;
	}

	// Non-terminal mulop just looks for an "*" or "/"
	/* $MULTIPLY | $DIDIVE */
	private int Mulop() {
		int mulDivOp = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Mulop", true);

		// if $MULTIPLY or $DIDIVE token
		if (token.code == lex.codeForMnemonic("MULTY")) {
			mulDivOp = interp.opcodeFor("MUL");
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else if (token.code == lex.codeForMnemonic("DIVDE")) {
			mulDivOp = interp.opcodeFor("DIV");
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else {
			error("'*' or '/'", token.lexeme);
		}

		trace("Mulop", false);

		return mulDivOp;
	}

	// Non-terminal unsigned constant that looks for a unsigned number
	/* <unsigned number> */
	private int UnsignedConstant() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		// no need to check for errors
		// error checking handled in factor
		trace("UnsignedConstant", true);

		// recurse down unsigned number
		recur = UnsignedNumber();

		trace("UnsignedConstant", false);

		return recur;
	}

	// Non-terminal unsigned number that looks for a float or integer from lexical
	/* $FLOAT | $INTEGER */
	private int UnsignedNumber() {
		int numIndex = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		// no need to check for errors
		// error checking handled in factor
		trace("UnsignedNumber", true);

		// float or int or ERROR
		// unsigned constant starts with integer or float number
		if ((token.code == lex.codeForMnemonic("INTC_") || (token.code == lex.codeForMnemonic("FLTC_")))) {
			// return the s.t. index
			numIndex = symbolList.LookupSymbol(token.lexeme);
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else {
			error("Integer or Floating Point Number", token.lexeme);
		}

		trace("UnsignedNumber", false);

		return numIndex;
	}

	// Non-terminal simple-type that looks for a float, integer, or string from
	// lexical
	/* $INTEGER | $FLOAT | $STRING */
	private int SimpleType() {
		int recur = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("SimpleType", true);

		// if $FLOAT | $INTEGER | $STRING
		if ((token.code == lex.codeForMnemonic("FLOAT")) || (token.code == lex.codeForMnemonic("INTEG"))
				|| (token.code == lex.codeForMnemonic("STRNG"))) {
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else {
			error("Float, Integer, or String", token.lexeme);
		}

		trace("SimpleType", false);

		return recur;
	}

	// Non-terminal relational operation >, <, >=, <=, =, <>
	/* $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ */
	private int Relop() {
		int relOpcode = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("Relop", true);

		// boolean for $EQ | $LSS | $GTR
		boolean greaterLessEqualBool = (token.code == lex.codeForMnemonic("GRTHN"))
				|| (token.code == lex.codeForMnemonic("LSTHN")) || (token.code == lex.codeForMnemonic("EQUAL"));
		// boolean for $NEQ | $LEQ | $GEQ
		boolean greaterEqLessEqNotEqBool = (token.code == lex.codeForMnemonic("GTEQL"))
				|| (token.code == lex.codeForMnemonic("LTEQL")) || (token.code == lex.codeForMnemonic("NTEQL"));

		// if $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ
		if (greaterLessEqualBool || greaterEqLessEqNotEqBool) {
			relOpcode = token.code;
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else {
			error("'>', '<', '>=', '<=', '=', or '<>'", token.lexeme);
		}

		trace("Relop", false);

		return relOpcode;
	}

	// Non-terminal string constant that looks for a string character
	/* $STRINGC */
	private int StringConstant() {
		int stringIndex = 0;
		// if errors exit recursion
		if (anyErrors) {
			return -1;
		}

		trace("StringConstant", true);

		// $STRINGC
		if ((token.code == lex.codeForMnemonic("STRC_"))) {
			stringIndex = symbolList.LookupSymbol(token.lexeme);
			// bookkeeping and move on
			token = lex.GetNextToken();
		} else {
			error("STRNG", token.lexeme);
		}

		trace("StringConstant", false);

		return stringIndex;
	}

	/**
	 * *************************************************
	 */
	/* UTILITY FUNCTIONS USED THROUGHOUT THIS CLASS */
	// error provides a simple way to print an error statement to standard output
	// and avoid reduncancy
	private void error(String wanted, String got) {
		anyErrors = true;
		System.out.println("ERROR: Expected " + wanted + " but found " + got);
	}

	// trace simply RETURNs if traceon is false; otherwise, it prints an
	// ENTERING or EXITING message using the proc string
	private void trace(String proc, boolean enter) {
		String tabs = "";

		if (!traceOn) {
			return;
		}

		if (enter) {
			tabs = repeatChar(" ", level);
			System.out.print(tabs);
			System.out.println("--> Entering " + proc);
			level++;
		} else {
			if (level > 0) {
				level--;
			}
			tabs = repeatChar(" ", level);
			System.out.print(tabs);
			System.out.println("<-- Exiting " + proc);
		}
	}

	// repeatChar returns a string containing x repetitions of string s;
	// nice for making a varying indent format
	private String repeatChar(String s, int x) {
		int i;
		String result = "";
		for (i = 1; i <= x; i++) {
			result = result + s;
		}

		return result;
	}

	// method for returning true if token is "*" or "/"
	private boolean isMulop(Lexical.token tok) {

		return ((tok.code == lex.codeForMnemonic("MULTY")) || (tok.code == lex.codeForMnemonic("DIVDE")));
	}

	// method for returning true if token is "+" or "-"
	private boolean isSignOrAddop(Lexical.token tok) {

		return ((tok.code == lex.codeForMnemonic("_ADD_")) || (tok.code == lex.codeForMnemonic("SBTRC")));
	}

	// method for returning true if token is $IDENTIFIER
	private boolean isIdentifier(Lexical.token tok) {

		return (tok.code == lex.codeForMnemonic("IDENT"));
	}

	// method for checking if start to statement begins with valid option:
	private boolean isStatement(Lexical.token tok) {

		// if $IDENTIFIER, $BEGIN, $IF, $DOWHILE, $REPEAT, $FOR, $WRITELN, $READLN
		return isIdentifier(tok) || token.code == lex.codeForMnemonic("BEGIN")
				|| token.code == lex.codeForMnemonic("_IF__") || token.code == lex.codeForMnemonic("DWHLE")
				|| token.code == lex.codeForMnemonic("RPEAT") || token.code == lex.codeForMnemonic("_FOR_")
				|| token.code == lex.codeForMnemonic("WRITE") || token.code == lex.codeForMnemonic("READ_");
	}

	// method for checking if current token is identifier and has not declared
	private void isUndeclared() {
		// if undeclared variable display error and change variable to declared to avoid
		// repeats
		if (symbolList.isDeclared(symbolList.currElement - 1) == false
				&& symbolList.GetUsage(symbolList.currElement - 1) == 'v') {
			symbolList.setVariableAsDeclared(symbolList.currElement - 1);
			System.out.println("Undeclared Identifier: " + token.lexeme);
		}
	}

	// method for adding a temp variable to S.T. and reurning its index
	private int GenSymbol() {
		int ret;
		// add empty temp int to S.T.
		symbolList.AddSymbol(TEMP_STR + tempCount, VAR_USE, TEMP_VAL);

		ret = symbolList.LookupSymbol(TEMP_STR + tempCount);
		tempCount++;

		return ret;
	}

	// private method for converting relop to a jump opcode
	private int relopToOpcode(int relop) {
		int result = -1;

		// use relational operation to find false result jump
		if (relop == lex.codeForMnemonic("EQUAL")) {
			result = interp.opcodeFor("JNZ");
		} else if (relop == lex.codeForMnemonic("NTEQL")) {
			result = interp.opcodeFor("JZ");
		} else if (relop == lex.codeForMnemonic("LSTHN")) {
			result = interp.opcodeFor("JNN");
		} else if (relop == lex.codeForMnemonic("GRTHN")) {
			result = interp.opcodeFor("JNP");
		} else if (relop == lex.codeForMnemonic("LTEQL")) {
			result = interp.opcodeFor("JP");
		} else if (relop == lex.codeForMnemonic("GTEQL")) {
			result = interp.opcodeFor("JN");
		}

		return result;
	}
}
