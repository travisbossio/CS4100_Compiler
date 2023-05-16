/* 
 * Travis Bossio CS4100 Homework 6, Spring 2023
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
package spring23part3;

import ADT.Syntactic;

public class mainHW6 {

	public static void main(String[] args) {
		String filePath = args[0];
		boolean traceon = true;
		// Required student name header
		System.out.println("Travis Bossio, 3332, CS4100/5100, SPRING 2023\n");
		System.out.println("INPUT FILE TO PROCESS IS: " + filePath);

		Syntactic parser = new Syntactic(filePath, traceon);
		parser.parse();
		System.out.println("Done.");
	}

}