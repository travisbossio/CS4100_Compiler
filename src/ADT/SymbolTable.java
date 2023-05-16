package ADT;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SymbolTable {
	// declare variables for symboltable
	Symbol[] symbolTable;
	int currElement;

	// inner class for Symbol object
	private class Symbol {
		private String name;
		private char usage;
		private int integerField;
		private String strField;
		private double doubleField;
		private char dataType;
		private boolean isDeclared = false;

		// overload constructor for integer
		public Symbol(String name, char usage, int integerField) {
			this.name = name;
			this.usage = usage;
			this.integerField = integerField;
			dataType = 'I';
		}

		// overload constructor for double
		public Symbol(String name, char usage, double doubleField) {
			this.name = name;
			this.usage = usage;
			this.doubleField = doubleField;
			dataType = 'F';
		}

		// overload constructor for string
		public Symbol(String name, char usage, String strField) {
			this.name = name;
			this.usage = usage;
			this.strField = strField;
			dataType = 'S';
		}
	}

	// public constructor for symbol table initializing size
	public SymbolTable(int maxSize) {
		symbolTable = new Symbol[maxSize];
		currElement = 0;
	}

	// overloaded function for adding symbol with integer to symboltable
	public int AddSymbol(String symbol, char usage, int value) {
		// check if symbol already in table
		int match = LookupSymbol(symbol);
		int index;

		// create symbol object with passed parameters
		Symbol newSymbol = new Symbol(symbol, usage, value);

		// if table already full
		if (currElement >= symbolTable.length) {
			index = -1;
		} else if (match == -1) { // else if table not full and no symbol match
			// add symbol to table
			symbolTable[currElement] = newSymbol;
			index = currElement;
			// increment total elements in table
			currElement++;
		} else { // else symbol match
			index = match;
		}

		return index;
	}

	// overloaded function for adding symbol with double to symboltable
	public int AddSymbol(String symbol, char usage, double value) {
		// check if symbol already in table
		int match = LookupSymbol(symbol);
		int index;

		// create symbol object with passed parameters
		Symbol newSymbol = new Symbol(symbol, usage, value);

		// if table already full
		if (currElement >= symbolTable.length) {
			index = -1;
		} else if (match == -1) { // else if table not full and no symbol match
			// add symbol to table
			symbolTable[currElement] = newSymbol;
			index = currElement;
			// increment total elements in table
			currElement++;
		} else { // else symbol match
			index = match;
		}

		return index;
	}

	// overloaded function for adding symbol with string to symboltable
	public int AddSymbol(String symbol, char usage, String value) {
		// check if symbol already in table
		int match = LookupSymbol(symbol);
		int index;

		// create symbol object with passed parameters
		Symbol newSymbol = new Symbol(symbol, usage, value);

		// if table already full
		if (currElement >= symbolTable.length) {
			index = -1;
		} else if (match == -1) { // else if table not full and no symbol match
			// add symbol to table
			symbolTable[currElement] = newSymbol;
			index = currElement;
			// increment total elements in table
			currElement++;
		} else { // else symbol match
			index = match;
		}

		return index;
	}

	// function for finding index of symbol in table
	public int LookupSymbol(String symbol) {
		int index = -1;

		// search for matching symbol name ignoring case
		for (int i = 0; i < currElement; i++) {
			if (symbolTable[i].name.equalsIgnoreCase(symbol)) {
				index = i;
			}
		}

		return index;
	}

	// getter for symbol name
	public String GetSymbol(int index) {

		return symbolTable[index].name;
	}

	// getter for symbol use
	public char GetUsage(int index) {

		return symbolTable[index].usage;
	}

	// getter for symbol data type
	public char GetDataType(int index) {

		return symbolTable[index].dataType;
	}

	// getter for symbol string value
	public String GetString(int index) {

		return symbolTable[index].strField;
	}

	// getter for symbol integer value
	public int GetInteger(int index) {

		return symbolTable[index].integerField;
	}

	// getter for symbol float value
	public double GetFloat(int index) {

		return symbolTable[index].doubleField;
	}

	// update symbol use and integer value at index
	public void UpdateSymbol(int index, char usage, int value) {
		symbolTable[index].usage = usage;
		symbolTable[index].integerField = value;
	}

	// update symbol use and float value at index
	public void UpdateSymbol(int index, char usage, double value) {
		symbolTable[index].usage = usage;
		symbolTable[index].doubleField = value;
	}

	// update symbol use and string value at index
	public void UpdateSymbol(int index, char usage, String value) {
		symbolTable[index].usage = usage;
		symbolTable[index].strField = value;
	}

	// function prints symboltable to file
	public void PrintSymbolTable(String fileName) {
		String symbString = formatSymbolTable();

		// Prints to the named file with the required error catching
		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

			bufferedWriter.write(symbString);

			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// function for formatting the symbol table
	public String formatSymbolTable() {
		int stringBuffer = FindLongestName() + 2;

		// format strging
		String symbString = "%s %s%" + stringBuffer + "s %s %s\n";
		String stringFormat = "  %-3d|%-" + stringBuffer + "s|%-3c|%-3c|";

		// format for each element in symbol table
		for (int i = 0; i < currElement; i++) {
			// format symbol values neatly
			symbString = String.format(symbString, "Index", "Name", "Use", "Typ", "Value") + String.format(stringFormat,
					i, symbolTable[i].name, symbolTable[i].usage, symbolTable[i].dataType);

			// if datatype integer print integer field
			if (symbolTable[i].dataType == 'I') {
				symbString = symbString + symbolTable[i].integerField + "\n";
			} else if (symbolTable[i].dataType == 'F') { // else if float datatype print float field
				symbString = symbString + symbolTable[i].doubleField + "\n";
			} else if (symbolTable[i].dataType == 'S') { // else if float datatype print string field
				symbString = symbString + symbolTable[i].strField + "\n";
			}
		}

		return symbString;
	}

	// returns longest name's size from symboltable
	public int FindLongestName() {
		int longest = 0;
		String longTemp = "";

		// check element in array for names for longest name
		for (int i = 0; i < currElement; i++) {
			if (symbolTable[i].name.length() > longTemp.length()) {
				longTemp = symbolTable[i].name;
				longest = longTemp.length();
			}
		}

		return longest;
	}

	// method to declared symbol table as set after declarations in block
	public void setVariableAsDeclared(int index) {
		symbolTable[index].isDeclared = true;
	}

	// method checking if all variables are declared in symbol table
	public boolean isDeclared(int index) {

		return symbolTable[index].isDeclared;
	}
}
