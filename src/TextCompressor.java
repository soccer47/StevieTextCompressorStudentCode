/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

import java.util.HashMap;

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Stevie K. Halprin
 */
public class TextCompressor {

    // Number of chars being accounted for (in ASCII value) in input
    public static final int R = 128;
    // Length of binary codes representing the codewords
    public static final int WIDTH = 12;
    // Integer representing code of EOF
    public static final int EOF = 128;
    // Integer representing maximum number of codewords
    // Initialized to 2^12
    public static final int MAX_CODES = 4096;


    private static void compress() {

        // Read in the text version of the binary file into a string
        String text = BinaryStdIn.readString();
        // TST holding all the value codes associated with each added character sequence
        TST codes = new TST();
        // Integer representing next available code for a String
        int nextCode = 129;

        // Add the first 122 ASCII characters to the TST of codes
        for (int i = 32; i <= 122; i++) {
            codes.insert((char)i + "", i);
        }

        // String to hold text of the current index
        String prefix;

        // Integer representing the current index being checked in String text
        int index = 0;
        // While the end of the text hasn't been reached, continue
        while (index < text.length()) {
            // Set the prefix to the String value of the current code
            prefix = codes.getLongestPrefix(text, index);
            // Write out the code representing the prefix
            BinaryStdOut.write(codes.lookup(prefix), WIDTH);

            // Get the next character if possible
            if (index < text.length() - 1) {
                prefix = prefix + text.charAt(index + 1);
                // While there are more codes available for Strings, add the new prefix to the TST of codes
                if (nextCode < MAX_CODES) {
                    codes.insert(prefix, nextCode);
                    // Increment nextCode by 1
                    nextCode++;
                }
            }
            // Increment index by one to move to the next char in the text
            index++;
        }
        // Write out the code signifying the end of the file
        BinaryStdOut.write(EOF, WIDTH);

        BinaryStdOut.close();
    }

    private static void expand() {

        // HashMap holding all the value codes associated with each added character sequence
        HashMap<Integer, String> codes = new HashMap<>();
        // Integer representing next available code for a String
        int nextCode = 129;

        // Add the first 122 ASCII characters to the HashMap of codes
        for (int i = 0; i <= 122; i++) {
            codes.put(i, "" + (char)i);
        }
        // Add the EOF code to the HashMap
        codes.put(EOF, "END_OF_FILE");

        // String to hold text of the current index
        String prefix;
        // String to hold text of the next index
        String nextPrefix;

        // Get the first code from the compressed file
        nextPrefix = codes.get(BinaryStdIn.readInt(WIDTH));

        // While the end of the text hasn't been reached, continue
        while (true) {
            // Set the prefix to the String associated with the current code
            prefix = nextPrefix;
            // Write out the String representing the prefix
            BinaryStdOut.write(prefix);

            // Read in the next code, and get the String associated with the code from the HashMap
            nextPrefix = codes.get(BinaryStdIn.readInt(WIDTH));

            // If the next character is the end of the file character, close out the file
            if (nextPrefix.equals("END_OF_FILE")) {
                BinaryStdOut.close();
            }
            else {
                // While there are more codes available for Strings, add the new prefix to the HashMap of codes
                if (nextCode < MAX_CODES) {
                    codes.put(nextCode, prefix + nextPrefix.charAt(0));
                    // Increment nextCode by 1
                    nextCode++;
                }
            }
        }
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
