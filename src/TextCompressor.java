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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Stevie K. Halprin
 */
public class TextCompressor {

    public static final int ESCAPE_CODE = 3;


    private static void compress() {

        // Read in every word in the text version of the binary file into an array
        String text = BinaryStdIn.readString();
        // Read in every word in the text version of the binary file into an array
        String[] textWords = BinaryStdIn.readString().split(" ");
        // Hashmap to hold the first occurrence of every word printed at least once
        HashMap<String, Integer> wordStarts = new LinkedHashMap<>();


        // Integer to hold the number of words added to the HashMap so far
        int numWords = 0;
        // Boolean to represent if the current mode is writing out characters
        boolean charMode = true;

        // Go through every word in the text array
        for (String word : textWords) {
            // Remove any special characters at the end of the word
            word = cleanWord(word);
            // If the word has already been printed, switch to code mode with escape code if needed
            if (wordStarts.containsKey(word)) {
                if (charMode) {
                    switchMode(charMode);
                }
            }
            // Otherwise switch to char mode with escape code if needed
            else {
                if (!charMode) {
                    switchMode(charMode);
                }
                BinaryStdOut.write(word);
            }
        }

        BinaryStdOut.close();
    }

    private static void expand() {

        // TODO: Complete the expand() method

        BinaryStdOut.close();
    }

    // If the given String has a special character at the end of it, remove that character
    private static String cleanWord(String word) {
        if (Character.isLetter(word.charAt(word.length() - 1))) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    // Method to write out the code to switch the current mode in the binary file
    private static void switchMode(boolean isCharMode) {
        // Length of escape code to be written
        int length = 8;
        // If the mode is currently char mode and is to be switched to code mode, alter the length of the escape code
        if (isCharMode) {
            length = 6;
        }
        BinaryStdOut.write(0, length);
    }

    // Write out the word in 6 bit binary codes
    private static void writeWord(String word) {
        for (int i = 0; i < word.length(); i++) {
            BinaryStdOut.write((int)word.charAt(i), 6);
        }
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
