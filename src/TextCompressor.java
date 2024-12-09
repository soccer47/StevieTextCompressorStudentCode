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
    public static final int R = 256;
    // Length of binary codes representing the codewords
    public static final int WIDTH = 12;
    // Integer representing code of EOF
    public static final int EOF = 256;
    // Integer representing maximum number of codewords
    // Initialized to 2^12
    public static final int MAX_CODES = 4096;


    private static void compress() {
        // Read in the text version of the binary file into a string
        String text = BinaryStdIn.readString();
        // TST holding all the value codes associated with each added character sequence
        TST codes = new TST();
        // Integer representing next available code for a String
        int nextCode = R + 1;

        // String to hold text of the current index
        String prefix;
        // Integer to hold current index before any alteration
        int originalIndex = 0;
        // Integer representing the current index being checked in String text
        int index = 0;
        // While the end of the text hasn't been reached, continue
        while (index < text.length()) {
            // Set the prefix to the String value of the current code
            prefix = codes.getLongestPrefix(text, index);

            // Check to make sure the current character has been added to the TST already
            if (prefix.isEmpty()) {
                // If the character isn't in the TST, set prefix equal to the char
                prefix = "" + text.charAt(index);
                // Also add the char to the TST with its ASCII value
                codes.insert(prefix, text.charAt(index));
            }
            // Write out the code representing the prefix
            BinaryStdOut.write(codes.lookup(prefix), WIDTH);
            // Store the current index before it is incremented
            originalIndex = index;
            // Increment index by the length of prefix to the next new char in the text
            index += prefix.length();

            // Get the next character if possible
            if (index < text.length() - prefix.length() - 1) {
                // Get the next character to add to the TST of codes
                prefix = prefix + text.charAt(originalIndex + prefix.length());
                // While there are more codes available for Strings, add the new prefix to the TST of codes
                if (nextCode < MAX_CODES) {
                    codes.insert(prefix, nextCode);
                    // Increment nextCode by 1
                    nextCode++;
                }
            }
        }
        // Write out the code signifying the end of the file
        BinaryStdOut.write(EOF, WIDTH);

        BinaryStdOut.close();
    }

    private static void expand() {

        // HashMap holding all the value codes associated with each added character sequence
        HashMap<Integer, String> codes = new HashMap<>();
        // Integer representing next available code for a String
        int nextCode = R + 1;

        // String to hold text of the current index
        String prefix;
        // String to hold text of the next index
        String nextPrefix;
        // Integer to hold code of the next prefix
        int nextPreVal;

        // Get the first code from the compressed file
        nextPreVal = BinaryStdIn.readInt(WIDTH);
        // Set nextPrefix equal to the char of the code
        nextPrefix = "" + (char)nextPreVal;
        // Add the code-char pair to the HashMap
        codes.put(nextPreVal, nextPrefix);

        // While the end of the text hasn't been reached, continue
        while (true) {
            // Set the prefix to the String associated with the current code
            prefix = nextPrefix;
            // Write out the String representing the prefix
            BinaryStdOut.write(prefix);

            // Read in the next code, and get the String associated with the code from the HashMap
            nextPreVal = BinaryStdIn.readInt(WIDTH);
            // If the next character is the end of the file code, close out of the file and stop iterating
            if (nextPreVal == EOF) {
                BinaryStdOut.close();
                break;
            }

            // If the code is already in the Hashmap, set nextPrefix to the String value associated with the code
            if (codes.containsKey(nextPreVal)) {
                nextPrefix = codes.get(nextPreVal);

                // While there are more codes available for Strings, add the new prefix to the HashMap of codes
                if (nextCode < MAX_CODES) {
                    codes.put(nextCode, prefix + nextPrefix.charAt(0));
                    // Increment nextCode by 1
                    nextCode++;
                }
            }
            else {
                // If not, and if the code is for a letter's own ASCII value, add the character to the HashMap
                if (nextPreVal < R) {
                    nextPrefix = "" + (char)nextPreVal;
                    codes.put(nextPreVal, nextPrefix);

                    // While there are more codes available for Strings, add the new prefix to the HashMap of codes
                    if (nextCode < MAX_CODES) {
                        codes.put(nextCode, prefix + nextPrefix.charAt(0));
                        // Increment nextCode by 1
                        nextCode++;
                    }
                }
                else {
                    // Otherwise the code is the code being added after the current prefix
                    // Set the next prefix to itself + its first character
                    nextPrefix = prefix + prefix.charAt(0);
                    // Add this new code to the HashMap
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
