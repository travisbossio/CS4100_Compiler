package ADT;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class Interpreter {
	// declare variables for interpreter
	private int pc;
	private ReserveTable optable;

	// declare opcode constants
	private final int STOP_OPCODE = 0;
	private final int DIV_OPCODE = 1;
	private final int MUL_OPCODE = 2;
	private final int SUB_OPCODE = 3;
	private final int ADD_OPCODE = 4;
	private final int MOV_OPCODE = 5;
	private final int PRINT_OPCODE = 6;
	private final int READ_OPCODE = 7;
	private final int JMP_OPCODE = 8;
	private final int JZ_OPCODE = 9;
	private final int JP_OPCODE = 10;
	private final int JN_OPCODE = 11;
	private final int JNZ_OPCODE = 12;
	private final int JNP_OPCODE = 13;
	private final int JNN_OPCODE = 14;
	private final int JINDR_OPCODE = 15;

	// reserve table size
	private final int RESERVE_SIZE = 15;

	// default constructor initializing optable and program counter
	public Interpreter() {
		optable = new ReserveTable(RESERVE_SIZE);
		initReserve(optable);
	}

	// initialize symbol and quad table to test factorial
	public boolean initializeFactorialTest(SymbolTable stable, QuadTable qtable) {
		InitSTF(stable);
		InitQTF(qtable);

		return true;
	}

	// initialize factorial Symbols
	public static void InitSTF(SymbolTable st) {
		st.AddSymbol("n", 'V', 10);
		st.AddSymbol("i", 'V', 0);
		st.AddSymbol("product", 'V', 0);
		st.AddSymbol("1", 'c', 1);
		st.AddSymbol("$temp", 'V', 0);
	}

	// initialize factorial Quads
	public void InitQTF(QuadTable qt) {
		qt.AddQuad(5, 3, 0, 2); // MOV
		qt.AddQuad(5, 3, 0, 1); // MOV
		qt.AddQuad(3, 1, 0, 4); // SUB
		qt.AddQuad(10, 4, 0, 7); // JP
		qt.AddQuad(2, 2, 1, 2); // MUL
		qt.AddQuad(4, 1, 3, 1); // ADD
		qt.AddQuad(8, 0, 0, 2); // JMP
		qt.AddQuad(6, 0, 0, 2); // PRINT
		qt.AddQuad(0, 0, 0, 0); // STOP
	}

	// initialize symbol and quad table to test summation
	public boolean initializeSummationTest(SymbolTable stable, QuadTable qtable) {
		InitSTS(stable);
		InitQTS(qtable);

		return true;
	}

	// initialize summation Symbols
	public static void InitSTS(SymbolTable st) {
		st.AddSymbol("n", 'V', 10);
		st.AddSymbol("i", 'V', 0);
		st.AddSymbol("sum", 'V', 0);
		st.AddSymbol("1", 'c', 1);
		st.AddSymbol("$temp", 'V', 0);
		st.AddSymbol("0", 'c', 0);
	}

	// initialize summation Quads
	public void InitQTS(QuadTable qt) {
		qt.AddQuad(5, 5, 0, 2); // MOV
		qt.AddQuad(5, 3, 0, 1); // MOV
		qt.AddQuad(3, 1, 0, 4); // SUB
		qt.AddQuad(10, 4, 0, 7); // JP
		qt.AddQuad(4, 2, 1, 2); // ADD
		qt.AddQuad(4, 1, 3, 1); // ADD
		qt.AddQuad(8, 0, 0, 2); // JMP
		qt.AddQuad(6, 0, 0, 2); // PRINT
		qt.AddQuad(0, 0, 0, 0); // STOP
	}

	// populate reserve table with opcode mnemonics and opcodes
	private void initReserve(ReserveTable optable) {
		optable.Add("STOP", STOP_OPCODE);
		optable.Add("DIV", DIV_OPCODE);
		optable.Add("MUL", MUL_OPCODE);
		optable.Add("SUB", SUB_OPCODE);
		optable.Add("ADD", ADD_OPCODE);
		optable.Add("MOV", MOV_OPCODE);
		optable.Add("PRINT", PRINT_OPCODE);
		optable.Add("READ", READ_OPCODE);
		optable.Add("JMP", JMP_OPCODE);
		optable.Add("JZ", JZ_OPCODE);
		optable.Add("JP", JP_OPCODE);
		optable.Add("JN", JN_OPCODE);
		optable.Add("JNZ", JNZ_OPCODE);
		optable.Add("JNP", JNP_OPCODE);
		optable.Add("JNN", JNN_OPCODE);
		optable.Add("JINDR", JINDR_OPCODE);
	}

	// method for executing quads and printing to file
	public void InterpretQuads(QuadTable qt, SymbolTable st, boolean traceOn, String fileName) {
		// initialize variables for interpreting quads
		pc = 0;
		int opcode, op1, op2, op3;
		int[] tempArray;
		int maxQuad = qt.NextQuad();
		// print file string
		String outputString = "";

		// while pc is less than maxquad
		while (pc < maxQuad) {
			// gets current quad into temp array
			tempArray = qt.GetQuad(pc);
			opcode = tempArray[0];
			op1 = tempArray[1];
			op2 = tempArray[2];
			op3 = tempArray[3];
			// echo trace to file and console if on
			if (traceOn) {
				outputString += makeTraceString(pc, opcode, op1, op2, op3) + "\n";
				System.out.println(makeTraceString(pc, opcode, op1, op2, op3));
			}
			// if valid opcode
			if (opcode >= 0 && opcode <= optable.getElementSize()) {
				switch (opcode) {
				case STOP_OPCODE: // STOP
					outputString += "Execution terminated by program STOP.\n";
					System.out.println("Execution terminated by program STOP.");
					pc = maxQuad;
					break;
				case DIV_OPCODE: // DIV
					st.UpdateSymbol(op3, st.GetUsage(op3), (st.GetInteger(op1) / st.GetInteger(op2)));
					pc++;
					break;
				case MUL_OPCODE: // MUL
					st.UpdateSymbol(op3, st.GetUsage(op3), (st.GetInteger(op1) * st.GetInteger(op2)));
					pc++;
					break;
				case SUB_OPCODE: // SUB
					st.UpdateSymbol(op3, st.GetUsage(op3), (st.GetInteger(op1) - st.GetInteger(op2)));
					pc++;
					break;
				case ADD_OPCODE: // ADD
					st.UpdateSymbol(op3, st.GetUsage(op3), (st.GetInteger(op1) + st.GetInteger(op2)));
					pc++;
					break;
				case MOV_OPCODE: // MOV
					st.UpdateSymbol(op3, st.GetUsage(op3), st.GetInteger(op1));
					pc++;
					break;
				case PRINT_OPCODE: // PRINT
					String output = getDatatypeValueString(op3, st);
					outputString += output + "\n";
					System.out.println(output);
					// print symbol name and value
					pc++;
					break;
				case READ_OPCODE: // READ
					// reads user input from keyboard
					st.UpdateSymbol(op3, st.GetUsage(op3), readStandardInputInteger());
					pc++;
					break;
				case JMP_OPCODE: // JMP
					pc = op3;
					break;
				case JZ_OPCODE: // JZ
					// if op1 = 0 set pc to op3 else incr pc by one
					pc = st.GetInteger(op1) == 0 ? op3 : pc + 1;
					break;
				case JP_OPCODE: // JP
					// if op1 > 0 set pc to op3 else incr pc by one
					pc = st.GetInteger(op1) > 0 ? op3 : pc + 1;
					break;
				case JN_OPCODE: // JN
					// if op1 < 0 set pc to op3 else incr pc by one
					pc = st.GetInteger(op1) < 0 ? op3 : pc + 1;
					break;
				case JNZ_OPCODE: // JNZ
					// if op1 != 0 set pc to op3 else incr pc by one
					pc = st.GetInteger(op1) != 0 ? op3 : pc + 1;
					break;
				case JNP_OPCODE: // JNP
					// if op1 <= 0 set pc to op3 else incr pc by one
					pc = st.GetInteger(op1) <= 0 ? op3 : pc + 1;
					break;
				case JNN_OPCODE: // JNN
					// if op1 >= 0 set pc to op3 else incr pc by one
					pc = st.GetInteger(op1) >= 0 ? op3 : pc + 1;
					break;
				case JINDR_OPCODE: // JINDR
					// set pc to op3 symbol table value
					pc = st.GetInteger(op3);
					break;
				}
			}
		}

		// output the quads
		// System.out.print(outputString);
		// print to file if trace is on
		if (traceOn) {
			PrintInterpreterOutput(fileName, outputString);
		}
	}

	// gets the proper dataype of value and returns as a string
	public String getDatatypeValueString(int index, SymbolTable st) {
		String output;

		// if integer append, else if float append, else string append
		if (st.GetDataType(index) == 'I') {
			output = st.GetSymbol(index) + " = " + st.GetInteger(index);
		} else if (st.GetDataType(index) == 'F') {
			output = st.GetSymbol(index) + " = " + st.GetFloat(index);
		} else {
			output = st.GetString(index);
		}

		return output;
	}

	// function prints interpreter output to file
	public void PrintInterpreterOutput(String fileName, String outputString) {
		// Prints to the named file with the required error catching
		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

			// write all exec quad instructions if trace on
			bufferedWriter.write(outputString);

			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// method for printing formatted code instructions when in trace mode
	private String makeTraceString(int pc, int opcode, int op1, int op2, int op3) {
		String result = "";
		result = "PC = " + String.format("%04d", pc) + ": " + (optable.LookupCode(opcode) + "     ").substring(0, 6)
				+ String.format("%02d", op1) + ", " + String.format("%02d", op2) + ", " + String.format("%02d", op3);
		return result;
	}

	// read integer from standard input
	private int readStandardInputInteger() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter Integer For READ:\n> ");
		int integer = scanner.nextInt();
		scanner.close();

		return integer;
	}

	// public method for returning opcode int for a given opcode
	public int opcodeFor(String op) {

		return optable.LookupName(op);
	}
}
