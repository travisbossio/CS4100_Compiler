/* 
 * Travis Bossio CS4100 Homework 4, Spring 2023
 * 
 */
package spring23hw2;

import ADT.Lexical;
import ADT.SymbolTable;

public class mainHW4 {

	public static void main(String[] args) {
		// Required student name header
		System.out.println("Travis Bossio CS4100 Homework 4, Spring 2023\n");

		String inFileAndPath = args[0];
		String outFileAndPath = args[1];
		System.out.println("Lexical for " + inFileAndPath);
		boolean traceOn = true;
		// Create a symbol table to store appropriate3 symbols found
		SymbolTable symbolList;
		symbolList = new SymbolTable(150);
		Lexical myLexer = new Lexical(inFileAndPath, symbolList, traceOn);
		Lexical.token currToken;
		currToken = myLexer.GetNextToken();
		while (currToken != null) {
			System.out.println("\t" + currToken.mnemonic + " | \t" + String.format("%04d", currToken.code) + " | \t"
					+ currToken.lexeme);
			currToken = myLexer.GetNextToken();
		}
		symbolList.PrintSymbolTable(outFileAndPath);
		System.out.println("Done.");
	}

}
