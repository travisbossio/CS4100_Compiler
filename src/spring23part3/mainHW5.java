/* 
 * Travis Bossio CS4100 Homework 5, Spring 2023
 * 
 */
package spring23part3;

import ADT.Syntactic;

public class mainHW5 {

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