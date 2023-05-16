/*
 *  Travis Bossio CS4100 Homework 4, Spring 2023
 */
package ADT;

/**
 * credit to @author abrouill for provided methods
 * Travis Bossio CS 4100
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Lexical {

	private File file; // File to be read for input
	private FileReader filereader; // Reader, Java reqd
	private BufferedReader bufferedreader; // Buffered, Java reqd
	private String line; // Current line of input from file
	private int linePos; // Current character position in the current line
	private SymbolTable saveSymbols; // SymbolTable used in Lexical sent as parameter to construct
	private boolean EOF; // End Of File indicator
	private boolean echo; // true means echo each input line
	private boolean printToken; // true to print found tokens here
	private int lineCount; // line #in file, for echo-ing
	private boolean needLine; // track when to read a new line

	// Tables to hold the reserve words and the mnemonics for token codes
	private final int sizeReserveTable = 50;
	private ReserveTable reserveWords = new ReserveTable(sizeReserveTable); // a few more than # reserves
	private ReserveTable mnemonics = new ReserveTable(sizeReserveTable); // a few more than # reserves

	// Constant codes for ident, string, int
	private final int IDENT_CODE = 50;
	private final int INT_CODE = 51;
	private final int FLOAT_CODE = 52;
	private final int STRING_CODE = 53;
	private final int OTHER_CODE = 99;
	private final char VAR_USE = 'v';
	private final char CONS_USE = 'c';
	private final int VAR_VALUE = 0;
	private final int IDENT_TRUNC = 20;
	private final int INT_TRUNC = 6;
	private final int FLOAT_TRUNC = 12;
	private final int INVALID_CODE = -1;
	private final String TRUNC_INT_VALUE = "0";
	private final String TRUNC_FLOAT_VALUE = "0.0";

	// Lexical constructor
	public Lexical(String filename, SymbolTable symbols, boolean echoOn) {
		saveSymbols = symbols; // map the initialized parameter to the local ST
		echo = echoOn; // store echo status
		lineCount = 0; // start the line number count
		line = ""; // line starts empty
		needLine = true; // need to read a line
		printToken = false; // default OFF, do not print tokens here within GetNextToken
		// call setPrintToken to change it publicly.
		linePos = -1; // no chars read yet

		// call initializations of tables
		initReserveWords(reserveWords);
		initMnemonics(mnemonics);

		// set up the file access, get first character, line retrieved 1st time
		try {
			file = new File(filename); // creates a new file instance
			filereader = new FileReader(file); // reads the file
			bufferedreader = new BufferedReader(filereader); // creates a buffering character input stream
			EOF = false;
			currCh = GetNextChar();
		} catch (IOException e) {
			EOF = true;
			e.printStackTrace();
		}
	}

	// inner class "token" is declared here, no accessors needed
	public class token {

		public String lexeme;
		public int code;
		public String mnemonic;

		token() {
			lexeme = "";
			code = 0;
			mnemonic = "";
		}
	}

	// ******************* PUBLIC USEFUL METHODS
	// These are nice for syntax to call later
	// given a mnemonic, find its token code value
	public int codeForMnemonic(String mnemonic) {

		return mnemonics.LookupName(mnemonic);
	}

	// given a mnemonic, return its reserve word
	public String reserveForMnemonic(String mnemonic) {

		return reserveWords.LookupCode(mnemonics.LookupName(mnemonic));
	}

	public String mnemonicFromCode(int code) {

		return mnemonics.LookupCode(code);
	}

	public int codeForLexeme(String lexeme) {

		return reserveWords.LookupName(lexeme);
	}

	// Public access to the current End Of File status
	public boolean EOF() {

		return EOF;
	}

	// DEBUG enabler, turns on/OFF token printing inside of GetNextToken
	public void setPrintToken(boolean on) {
		printToken = on;
	}

	// method for initializing all reserve words in this project
	private void initReserveWords(ReserveTable reserveWords) {
		// reserve words codes 0-25
		reserveWords.Add("GOTO", 0);
		reserveWords.Add("INTEGER", 1);
		reserveWords.Add("TO", 2);
		reserveWords.Add("DO", 3);
		reserveWords.Add("IF", 4);
		reserveWords.Add("THEN", 5);
		reserveWords.Add("ELSE", 6);
		reserveWords.Add("FOR", 7);
		reserveWords.Add("OF", 8);
		reserveWords.Add("WRITELN", 9);
		reserveWords.Add("READLN", 10);
		reserveWords.Add("BEGIN", 11);
		reserveWords.Add("END", 12);
		reserveWords.Add("VAR", 13);
		reserveWords.Add("DOWHILE", 14);
		reserveWords.Add("UNIT", 15);
		reserveWords.Add("LABEL", 16);
		reserveWords.Add("REPEAT", 17);
		reserveWords.Add("UNTIL", 18);
		reserveWords.Add("PROCEDURE", 19);
		reserveWords.Add("DOWNTO", 20);
		reserveWords.Add("FUNCTION", 21);
		reserveWords.Add("RETURN", 22);
		reserveWords.Add("FLOAT", 23);
		reserveWords.Add("STRING", 24);
		reserveWords.Add("ARRAY", 25);

		// 1 and 2-char; codes 30-48
		reserveWords.Add("/", 30);
		reserveWords.Add("*", 31);
		reserveWords.Add("+", 32);
		reserveWords.Add("-", 33);
		reserveWords.Add("(", 34);
		reserveWords.Add(")", 35);
		reserveWords.Add(";", 36);
		reserveWords.Add(":=", 37);
		reserveWords.Add(">", 38);
		reserveWords.Add("<", 39);
		reserveWords.Add(">=", 40);
		reserveWords.Add("<=", 41);
		reserveWords.Add("=", 42);
		reserveWords.Add("<>", 43);
		reserveWords.Add(",", 44);
		reserveWords.Add("[", 45);
		reserveWords.Add("]", 46);
		reserveWords.Add(":", 47);
		reserveWords.Add(".", 48);

		// ID, Numeric, String, and Other codes
		reserveWords.Add("IDENTIFIER", 50);
		reserveWords.Add("INT", 51);
		reserveWords.Add("FLOATC", 52);
		reserveWords.Add("STRINGC", 53);
		reserveWords.Add("OTHER", 99);
	}

	// method for initializing 5-char reserve word mnemonics
	private void initMnemonics(ReserveTable mnemonics) {
		// reserve words codes 0-25
		mnemonics.Add("GOTO_", 0);
		mnemonics.Add("INTEG", 1);
		mnemonics.Add("_TO__", 2);
		mnemonics.Add("_DO__", 3);
		mnemonics.Add("_IF__", 4);
		mnemonics.Add("THEN_", 5);
		mnemonics.Add("ELSE_", 6);
		mnemonics.Add("_FOR_", 7);
		mnemonics.Add("_OF__", 8);
		mnemonics.Add("WRITE", 9);
		mnemonics.Add("READ_", 10);
		mnemonics.Add("BEGIN", 11);
		mnemonics.Add("_END_", 12);
		mnemonics.Add("_VAR_", 13);
		mnemonics.Add("DWHLE", 14);
		mnemonics.Add("UNIT_", 15);
		mnemonics.Add("LABEL", 16);
		mnemonics.Add("RPEAT", 17);
		mnemonics.Add("UNTIL", 18);
		mnemonics.Add("PROCD", 19);
		mnemonics.Add("DOWNT", 20);
		mnemonics.Add("FUNCT", 21);
		mnemonics.Add("RETRN", 22);
		mnemonics.Add("FLOAT", 23);
		mnemonics.Add("STRNG", 24);
		mnemonics.Add("ARRAY", 25);

		// 1 and 2-char; codes 30-48
		mnemonics.Add("DIVDE", 30);
		mnemonics.Add("MULTY", 31);
		mnemonics.Add("_ADD_", 32);
		mnemonics.Add("SBTRC", 33);
		mnemonics.Add("LPARA", 34);
		mnemonics.Add("RPARA", 35);
		mnemonics.Add("SMCLN", 36);
		mnemonics.Add("ASSGN", 37);
		mnemonics.Add("GRTHN", 38);
		mnemonics.Add("LSTHN", 39);
		mnemonics.Add("GTEQL", 40);
		mnemonics.Add("LTEQL", 41);
		mnemonics.Add("EQUAL", 42);
		mnemonics.Add("NTEQL", 43);
		mnemonics.Add("COMMA", 44);
		mnemonics.Add("LBRKT", 45);
		mnemonics.Add("RBRKT", 46);
		mnemonics.Add("COLON", 47);
		mnemonics.Add("PRIOD", 48);

		// ID, Numeric, String, and Other codes
		mnemonics.Add("IDENT", 50);
		mnemonics.Add("INTC_", 51);
		mnemonics.Add("FLTC_", 52);
		mnemonics.Add("STRC_", 53);
		mnemonics.Add("OTHER", 99);
	}

	// ********************** UTILITY FUNCTIONS
	private void consoleShowError(String message) {
		System.out.println("**** ERROR FOUND: " + message);
	}

	// Character category for alphabetic chars
	private boolean isLetter(char ch) {

		return (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')));
	}

	// Character category for 0..9
	private boolean isDigit(char ch) {

		return ((ch >= '0') && (ch <= '9'));
	}

	// Character is a newline
	private boolean isCarriageReturn(char ch) {

		return ((ch == '\n') || (ch == '\r'));
	}

	// Category for any whitespace to be skipped over
	private boolean isWhitespace(char ch) {

		// SPACE, TAB, NEWLINE are white space
		return ((ch == ' ') || (ch == '\t') || (ch == '\n'));
	}

	// Returns the VALUE of the next character without removing it from the
	// input line. Useful for checking 2-character tokens that start with
	// a 1-character token.
	private char PeekNextChar() {
		char result = ' ';
		if ((needLine) || (EOF)) {
			result = ' '; // at end of line, so nothing
		} else //
		{
			if ((linePos + 1) < line.length()) { // have a char to peek
				result = line.charAt(linePos + 1);
			}
		}

		return result;
	}

	// Called by GetNextChar when the characters in the current line are used up.
	// STUDENT CODE SHOULD NOT EVER CALL THIS!
	private void GetNextLine() {
		try {
			line = bufferedreader.readLine();
			if ((line != null) && (echo)) {
				lineCount++;
				System.out.println(String.format("%04d", lineCount) + " " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (line == null) { // The readLine returns null at EOF, set flag
			EOF = true;
		}
		linePos = -1; // reset vars for new line if we have one
		needLine = false; // we have one, no need
		// the line is ready for the next call to get a character
	}

	// Called to get the next character from file, automatically gets a new
	// line when needed. CALL THIS TO GET CHARACTERS FOR GETIDENT etc.
	public char GetNextChar() {
		char result;
		if (needLine) // ran out last time we got a char, so get a new line
		{
			GetNextLine();
		}
		// try to get char from line buff
		if (EOF) {
			result = '\n';
			needLine = false;
		} else {
			if ((linePos < line.length() - 1)) { // have a character available
				linePos++;
				result = line.charAt(linePos);
			} else { // need a new line, but want to return eoln on this call first
				result = '\n';
				needLine = true; // will read a new line on next GetNextChar call
			}
		}

		return result;
	}

	// The constants below allow flexible comment start/end characters
	final char commentStart_1 = '{';
	final char commentEnd_1 = '}';
	final char commentStart_2 = '(';
	final char commentPairChar = '*';
	final char commentEnd_2 = ')';

	// error string for unterminated comment
	String unterminatedComment = "Comment not terminated before End Of File";

	// Skips past single and multi-line comments, and outputs UNTERMINATED
	// COMMENT when end of line is reached before terminating
	public char skipComment(char curr) {
		if (curr == commentStart_1) {
			curr = GetNextChar();
			while ((curr != commentEnd_1) && (!EOF)) {
				curr = GetNextChar();
			}
			if (EOF) {
				consoleShowError(unterminatedComment);
			} else {
				curr = GetNextChar();
			}
		} else {
			if ((curr == commentStart_2) && (PeekNextChar() == commentPairChar)) {
				curr = GetNextChar(); // get the second
				curr = GetNextChar(); // into comment or end of comment
//            while ((curr != commentPairChar) && (PeekNextChar() != commentEnd_2) &&(!EOF)) {
				while ((!((curr == commentPairChar) && (PeekNextChar() == commentEnd_2))) && (!EOF)) {
//                if (lineCount >=4) {
					// System.out.println("In Comment, curr, peek: "+curr+", "+PeekNextChar());}
					curr = GetNextChar();
				}
				if (EOF) {
					consoleShowError(unterminatedComment);
				} else {
					curr = GetNextChar(); // must move past close
					curr = GetNextChar(); // must get following
				}
			}
		}

		return (curr);
	}

	// Reads past all whitespace as defined by isWhiteSpace
	// NOTE THAT COMMENTS ARE SKIPPED AS WHITESPACE AS WELL!
	public char skipWhiteSpace() {
		do {
			while ((isWhitespace(currCh)) && (!EOF)) {
				currCh = GetNextChar();
			}
			currCh = skipComment(currCh);
		} while (isWhitespace(currCh) && (!EOF));

		return currCh;
	}

	private boolean isPrefix(char ch) {

		return ((ch == ':') || (ch == '<') || (ch == '>'));
	}

	private boolean isStringStart(char ch) {

		return ch == '"';
	}

	// global char
	char currCh;

	// private method for filling token lexeme, code, and mnemonic with identifier
	// info
	private token getIdentifier() {
		// create new token and fill its lexeme with input string
		token result = new token();
		result.lexeme += currCh;
		currCh = GetNextChar();

		// truncate string for symbol table
		String truncateLexeme;

		// while is letter or digit or underscore add to lexeme
		while (isLetter(currCh) || isDigit(currCh) || currCh == '_') {
			result.lexeme += currCh;
			currCh = GetNextChar();
		}

		// look for lexeme code in reserve words
		result.code = codeForLexeme(result.lexeme);

		// if no lexeme in reserve words make it identifier code (50)
		// add variable to symbol table
		if (result.code == INVALID_CODE) {
			result.code = IDENT_CODE;

			truncateLexeme = result.lexeme;

			// if lexeme longer than 20 truncate to 20
			if (truncateLexeme.length() > IDENT_TRUNC) {
				truncateLexeme = truncateToken(truncateLexeme, "Identifier", IDENT_TRUNC);
			}

			saveSymbols.AddSymbol(truncateLexeme, VAR_USE, VAR_VALUE);
		}

		// lookup mnemonic for reserve word code
		result.mnemonic = mnemonicFromCode(result.code);

		return result;
	}

	/* a number is: <digit>+[.<digit>*[E<digit>+]] */
	// private method for filling token lexeme, code, and mnemonic with integer or
	// float info
	private token getNumber() {
		// create new token and fill its lexeme with input string
		token result = new token();
		result.lexeme += currCh;
		currCh = GetNextChar();

		String lexemeSymbolTable;
		String lexemeValue;

		// while ch is digit add to lexeme
		// covers <digit>+
		while (isDigit(currCh)) {
			result.lexeme += currCh;
			currCh = GetNextChar();
		}

		// if period number is a float
		if (currCh == '.') {
			// set code to float and add to lexeme
			result.code = FLOAT_CODE;
			result.lexeme += currCh;
			currCh = GetNextChar();

			while (isDigit(currCh)) { // while digit add floating point digits to float
				result.lexeme += currCh;
				currCh = GetNextChar();
			}

			// if E then number has scientific notation
			if (currCh == 'E') {
				result.lexeme += currCh;
				currCh = GetNextChar();
				// digits are immediately follow E
				if (isDigit(currCh)) {
					result.lexeme += currCh;
					currCh = GetNextChar();
					// add digits to exponential constant
					while (isDigit(currCh)) {
						result.lexeme += currCh;
						currCh = GetNextChar();
					}
				} else { // else non digits following E then other
					result.code = OTHER_CODE;
				}
			}
		} else { // else number is integer
			result.code = INT_CODE;
		}

		// lookup mnemonic for reserve word code
		result.mnemonic = mnemonics.LookupCode(result.code);

		// save lexeme to truncate value for adding to symbol table
		lexemeSymbolTable = result.lexeme;

		// save lexeme value for printing to symbol table
		lexemeValue = result.lexeme;

		// if lexeme int and longer than 6 truncate to 6
		// add both int and float to symbol table with 0 and 0.0 as value
		if (result.code == INT_CODE && lexemeSymbolTable.length() > INT_TRUNC) {
			lexemeSymbolTable = truncateToken(lexemeSymbolTable, "Integer", INT_TRUNC);
			lexemeValue = TRUNC_INT_VALUE;
			// else if float truncate to 12
		} else if (result.code == FLOAT_CODE && lexemeSymbolTable.length() > FLOAT_TRUNC) {
			lexemeSymbolTable = truncateToken(lexemeSymbolTable, "Float", FLOAT_TRUNC);
			lexemeValue = TRUNC_FLOAT_VALUE;
		}

		// add int or float to symbol table
		if (result.code == INT_CODE && integerOK(lexemeValue)) {
			saveSymbols.AddSymbol(lexemeSymbolTable, CONS_USE, Integer.parseInt(lexemeValue));
		} else if (result.code == FLOAT_CODE && doubleOK(lexemeValue)) {
			saveSymbols.AddSymbol(lexemeSymbolTable, CONS_USE, Double.parseDouble(lexemeValue));
		}

		return result;
	}

	// error string for unterminated string
	String untermString = "Unterminated string found.";

	/* a string is: "<string-element>*" */
	// private method for filling token lexeme, code, and mnemonic with string info
	private token getString() {
		// create new token and fill its lexeme with input string
		token result = new token();
		currCh = GetNextChar();

		// while current char is a string element add to lexeme and get next char
		while (!isCarriageReturn(currCh) && !isStringStart(currCh)) {
			result.lexeme += currCh;
			currCh = GetNextChar();
		}

		// if a newline before end "; print error and add other code
		if (isCarriageReturn(currCh)) {
			System.out.println(untermString);
			result.code = OTHER_CODE;
		} else { // else " ended string; add string code and add to ST
			result.code = STRING_CODE;
			saveSymbols.AddSymbol(result.lexeme, CONS_USE, result.lexeme);
		}

		currCh = GetNextChar();

		// lookup mnemonic for reserve word code
		result.mnemonic = mnemonicFromCode(result.code);

		return result;
	}

	/* an other is: "<char-token><char-token>?" */
	// private method for filling token lexeme, code, and mnemonic with 1/2-char
	// token info
	private token getOtherToken() {
		// create new token and fill its lexeme with input string
		token result = new token();
		result.lexeme += currCh;

		// lookup code 2-char token
		int code = codeForLexeme(result.lexeme + PeekNextChar());

		// if code is a 2-char token
		if (code != INVALID_CODE) {
			result.lexeme += PeekNextChar();
			result.code = code;
			currCh = GetNextChar();
		} else { // else 1 char token or other
			// lookup code for 1-char token
			code = codeForLexeme(result.lexeme);
			// if found code for 1-cha
			if (code != INVALID_CODE) {
				result.code = code;
			} else { // else non recognized character
				result.code = OTHER_CODE;
			}
		}

		currCh = GetNextChar();

		// lookup mnemonic for reserve word code
		result.mnemonic = mnemonicFromCode(result.code);

		return result;
	}

	// Checks to see if a string contains a valid DOUBLE
	public boolean doubleOK(String stin) {
		boolean result;
		Double x;
		try {
			x = Double.parseDouble(stin);
			result = true;
		} catch (NumberFormatException ex) {
			result = false;
		}

		return result;
	}

	// Checks the input string for a valid INTEGER
	public boolean integerOK(String stin) {
		boolean result;
		int x;
		try {
			x = Integer.parseInt(stin);
			result = true;
		} catch (NumberFormatException ex) {
			result = false;
		}

		return result;
	}

	// method for getting next token in input stream
	public token GetNextToken() {
		token result = new token();

		// only track characters
		currCh = skipWhiteSpace();
		if (isLetter(currCh)) { // is identifier
			result = getIdentifier();
		} else if (isDigit(currCh)) { // is numeric
			result = getNumber();
		} else if (isStringStart(currCh)) { // string literal
			result = getString();
		} else // default char checks
		{
			result = getOtherToken();
		}

		// if end of file
		if ((result.lexeme.equals("")) || (EOF)) {
			result = null;
		}
		// set the mnemonic
		if (result != null) {
			// THIS LINE REMOVED-- PUT BACK IN TO USE LOOKUP
			// result.mnemonic = mnemonics.LookupCode(result.code);
			if (printToken) {
				System.out.println("\t" + result.mnemonic + " | \t" + String.format("%04d", result.code) + " | \t"
						+ result.lexeme);
			}
		}

		return result;
	}

	// method for truncating specific type to specific length
	private String truncateToken(String lexeme, String type, int truncLength) {
		System.out.print(String.format(type + " length > %d, truncated " + lexeme, truncLength));
		// save lexeme into char array
		char[] chArray = lexeme.toCharArray();
		// clear lexeme
		lexeme = "";
		for (int i = 0; i < truncLength; i++) {
			lexeme += chArray[i];
		}
		System.out.println(" to " + lexeme);

		return lexeme;
	}
}
