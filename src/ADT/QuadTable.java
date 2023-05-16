package ADT;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class QuadTable {
	// declare variables for quadtable
	private int quadTable[][];
	private int nextAvailable;

	// quadtable constructor
	public QuadTable(int maxSize) {
		quadTable = new int[maxSize][4];
		nextAvailable = 0;
	}

	// function returns next available row slot
	public int NextQuad() {

		return nextAvailable;
	}

	// function adds new quad row to quadtable and increments next available row
	public void AddQuad(int opcode, int op1, int op2, int op3) {
		quadTable[NextQuad()][0] = opcode;
		quadTable[NextQuad()][1] = op1;
		quadTable[NextQuad()][2] = op2;
		quadTable[NextQuad()][3] = op3;

		nextAvailable++;
	}

	// function returns desired quadtable row
	public int[] GetQuad(int index) {

		return quadTable[index];
	}

	// function change 3rd opcode of desired quadtable row
	public void UpdateJump(int index, int op3) {
		quadTable[index][3] = op3;
	}

	// function prints quadtable to file
	public void PrintQuadTable(String fileName) {
		String quadString = formatQuadTable();

		// Prints to the named file with the required error catching
		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

			bufferedWriter.write(quadString);

			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// function returns formatted quadtable contents in a string
	public String formatQuadTable() {
		String quadString = "Index Opcode  Op1   Op2   Op3\n";
		for (int i = 0; i < nextAvailable; i++) {
			quadString = quadString + String.format("  %-3d|  %-4d|  %-3d|  %-3d|  %-3d|\n", i, quadTable[i][0],
					quadTable[i][1], quadTable[i][2], quadTable[i][3]);
		}

		return quadString;
	}
}
