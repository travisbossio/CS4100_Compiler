package ADT;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ReserveTable {
	private ReserveTableObj[] resArray;
	private int elements = 0;

	/*
	 * inner class for reserve table array objects
	 */
	private class ReserveTableObj {
		// private variables for code and name
		private int code;
		private String name;

		/*
		 * public constructor for array objects
		 */
		public ReserveTableObj(String name, int code) {
			this.code = code;
			this.name = name;
		}
	}

	/*
	 * public constructor for ADT reserve table sets reserve table size
	 */
	public ReserveTable(int maxSize) {
		resArray = new ReserveTableObj[maxSize];
	}

	/*
	 * method for adding to reserve table returns index added to array
	 */
	public int Add(String name, int code) {
		ReserveTableObj obj = new ReserveTableObj(name, code);
		boolean flag = true;
		int index = 0;

		while (flag) {
			// if index of array empty add object
			// add to elements to track total elements in use
			if (resArray[index] == null) {
				resArray[index] = obj;
				elements++;
				flag = false;
			}
			// else increment index
			else {
				index++;
			}

			// if fail add to array return -1
			if (index >= resArray.length) {
				flag = false;
				index = -1;
			}
		}

		return index;
	}

	/*
	 * method to lookup name value in reserve table returns index
	 */
	public int LookupName(String name) {
		boolean isFound = false;
		int code = 0;

		// iterate through array looking for name match
		for (int i = 0; i < elements; i++) {
			// if match set return index
			if (resArray[i].name.compareToIgnoreCase(name) == 0) {
				code = resArray[i].code;
				isFound = true;
			}
		}

		// return index
		if (isFound) {

			return code;
		}
		// else not found return -1
		else {

			return -1;
		}
	}

	/*
	 * method to lookup name value in reserve table returns index
	 */
	public String LookupCode(int code) {
		String str = "";

		// iterate through array looking for code match
		for (int i = 0; i < elements; i++) {
			// if match set string
			if (resArray[i].code == code) {
				str = resArray[i].name;
			}
		}

		return str;
	}

	/*
	 * method prints reserve table in neat format
	 */
	public void PrintReserveTable(String fileName) {
		int longest = FindLongestName();

		String myNumberString = "";
		String title = "Index" + pad("Name", 6, true) + pad("Code", longest + 6, true);
		System.out.println(title);

		// for each element in array print index, name, and code
		for (int i = 0; i < elements; i++) {
			myNumberString += pad(pad(Integer.toString(i), 3, true), 8, false);
			myNumberString += pad(resArray[i].name.toUpperCase(), longest, false);
			myNumberString += pad(String.valueOf(resArray[i].code), 8, true) + "\n";
		}
		System.out.println(myNumberString);
		PrintToFile(fileName, title, myNumberString);

	}

	public void PrintToFile(String fileName, String title, String myNumberString) {
		// Prints to the named file with the required error catching
		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

			bufferedWriter.write(title);
			bufferedWriter.newLine();
			bufferedWriter.write(myNumberString);

			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * method returns index of longest name
	 */
	public int FindLongestName() {
		int longest = 0;
		String longTemp = "";

		// check element in array for names for longest name
		for (int i = 0; i < elements; i++) {
			if (resArray[i].name.length() > longTemp.length()) {
				longTemp = resArray[i].name;
				longest = longTemp.length();
			}
		}

		return longest;
	}

	/*
	 * provided method for printing with pad format
	 */
	public String pad(String input, int len, boolean left) {
		while (input.length() < len) {
			if (left)
				input = " " + input;
			else
				input = input + " ";
		}

		return input;
	}

	// getter for reserve table size
	public int getElementSize() {

		return elements;
	}

}
