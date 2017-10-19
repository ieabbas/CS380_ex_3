
/* CS 380 - Computer Networks
 * Exercise 3 : Checksums & CRC Checking
 * Ismail Abbas
 */

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * This class will verify a checksum with a predetermined server
 */
public class Ex3Client {
	// These lists will account for the bytes that are read in, as well as the
	// buffer
	private static ArrayList<Integer> inputBytes = new ArrayList<>();
	private static ArrayList<Integer> buffer = new ArrayList<>();

	/*
	 * The main method, this accomplishes the meat of the tasks
	 */
	public static void main(String[] args) {
		// Like everything else in life, you gotta try first before
		// giving up
		try {
			// Connect to server
			Socket s = new Socket("18.221.102.182", 38103);
			System.out.println("Connected to server.");
			InputStream isStr = s.getInputStream();

			// Read in first byte to determine how many bytes to read in
			int count = isStr.read();
			System.out.println("Reading " + count + " bytes.");
			// Read in bytes
			int columnCount = 0; // Keep track of how many columns are printed
			System.out.print("   ");
			while (true) {
				if (count == 0) { // Stop reading in bytes
					break;
				}
				int bytes = isStr.read(); // Read in data
				System.out.print(formatDaByte(bytes));
				inputBytes.add(bytes);
				// System.out.println(count+": "+Integer.toHexString(bytes &
				// 0xFF));
				columnCount++;
				// Start a new line when 20 characters in a row
				if (columnCount % 10 == 0)
					System.out.print("\n   ");
				count--;
			}
			// Clearing the print buffer
			System.out.println();
			// Concatenate two 8 bits into a 16 bit
			to16Bits();
			// Separate 16 bit check sum to two bytes
			byte[] chkSum = new byte[2];
			short sum = (short) cksum();
			System.out.println("Checksum calculated: 0x" + Integer.toHexString(sum & 0xFFFF));
			chkSum[0] = (byte) ((sum >> 8) & 0xFF);
			chkSum[1] = (byte) (sum & 0xFF);
			PrintStream pStr = new PrintStream(s.getOutputStream(), true);
			// Send the info to the server
			pStr.write(chkSum, 0, chkSum.length);
			// Get the correct response from the server because we don't make
			// mistakes
			byte response = (byte) isStr.read();
			if (response == 1)
				System.out.println("Response good because you're amazing");
			else
				System.out.println("Response bad because you don't know what you're doing");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * The java translated variant of the provided checksum code from the
	 * professor, this method implements the Internet checksum algorithm for
	 * IPv4 packets
	 */
	public static long cksum() {
		int length = buffer.size();
		int index = 0;
		long sum = 0;

		// While the length of the buffer isn't zero, append the sum at the
		// location of the index
		while (length > 0) {
			sum += buffer.get(index);
			if ((sum & 0xFFFF0000) > 0) {
				// Carry occurred
				sum = sum & 0xFFFF;
				sum++;
			}
			index++;
			length--;
		}
		sum = ~sum;
		sum = sum & 0xFFFF;
		return sum;
	}

	/*
	 * This method formats the byte from the passed in bits, to output properly
	 * on the console as well as pad it if necessary
	 */
	public static String formatDaByte(int bits) {
		// Retrieve the last 8 bits
		String part = Integer.toHexString(bits & 0xFF);
		int size = part.length();
		if (size == 1) {
			return "0" + part;
		}
		return part;
	}

	/*
	 * This method will create a 16 bit number from two paired bytes
	 */
	public static void to16Bits() {
		int length = inputBytes.size();
		int i = 0;
		// For every two bytes make a 16 bit number and add the 16 bit number to
		// buffer
		while (length > 1) {
			int numeroUno = inputBytes.get(i);
			numeroUno = numeroUno << 8;
			int numeroDos = inputBytes.get(i + 1);
			int finalProduct = numeroUno | numeroDos;
			buffer.add(finalProduct);
			i += 2;
			length -= 2;
		}
		// If the number of bytes is odd, put the last byte in the buffer again
		if (length > 0) {
			int numOdd = inputBytes.get(inputBytes.size() - 1);
			numOdd = numOdd << 8;
			buffer.add(numOdd);
		}
	}
}