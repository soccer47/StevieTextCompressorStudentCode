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

    // Length of binary codes representing the first index of the word, and then the length of the word
    public static final int CODE_LENGTH = 21;
    // Length of header holding length of the text
    public static final int TEXT_LENGTH = 30;
    // Length of binary codes representing the first index of the word
    public static final int CODE_LENGTH_INDEX = 14;
    // Length of binary codes representing the length of the word
    public static final int CODE_LENGTH_LEN = 5;
    // Length of the binary codes representing individual chars
    public static final int LETTER_LENGTH = 6;
    // Boolean to represent if the current mode is writing out characters
    public static boolean charMode = true;


    private static void compress() {

        // Read in every word in the text version of the binary file into an array
        String text = BinaryStdIn.readString();
        // Read in every word in the text version of the binary file into an array
        String[] textWords = BinaryStdIn.readString().split(" ");
        // Hashmap to hold the first occurrence of every word printed at least once
        HashMap<String, Integer> wordStarts = new LinkedHashMap<>();

        // Write out the length of the original String in TEXT_LENGTH characters
        BinaryStdOut.write(text.length(), TEXT_LENGTH);


        // Go through every word in the text array
        for (String word : textWords) {
            // Remove any special characters at the end of the word
            word = cleanWord(word);
            // If the word has already been printed, switch to code mode with escape code if needed
            if (wordStarts.containsKey(word)) {
                if (charMode) {
                    switchMode(charMode);
                }
                // Then print the code referencing the first instance of the word in the String
                writeCode(wordStarts.get(word), word.length());
            }
            // Otherwise write out the word character by character into the binary file
            else {
                // Switch to char mode with escape code if needed
                if (!charMode) {
                    switchMode(charMode);
                }
                // Add the word to the HashMap of already printed words
                wordStarts.put(word, text.indexOf(word));
                // Then write out the word into the binary file
                writeWord(word);
            }
        }

        BinaryStdOut.close();
    }

    private static void expand() {

        // Get the length of the original text from the header of the binary file
        int finalLength = BinaryStdIn.readInt(TEXT_LENGTH);
        // String holding output of compressed binary file
        String text = "";
        // Boolean representing if the current mode being used in the file is reading characters or word codes
        // Initialized to true because first word cannot have been stated before in text
        boolean isCharMode = true;
        // Length of binary codes being read in
        int codeLength = LETTER_LENGTH;
        // Escape code for letter codes


        while (text.length() <= finalLength) {

        }

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
        int length = CODE_LENGTH;
        // If the mode is currently char mode and is to be switched to code mode, alter the length of the escape code
        if (isCharMode) {
            length = LETTER_LENGTH;
        }
        // Write the escape code into the binary file
        BinaryStdOut.write(0, length);
        // Switch the mode of being used in the file accordingly
        charMode = !charMode;
    }

    // Write out the word in 6 bit binary codes
    private static void writeWord(String word) {
        // Go through every character in the word, and print each out in
        for (int i = 0; i < word.length(); i++) {
            // Get the next character in the word
            char c = word.charAt(i);
            // A-Z will be written as 1-26 respectively
            if (!Character.isLowerCase(c)) {
                BinaryStdOut.write((int)c - 64, 6);
            }
            // a-z will be written as 27-52 respectively
            else {
                BinaryStdOut.write((int)c - 70, 6);
            }
        }
    }

    // Write out the word in 6 bit binary codes
    private static void writeCode(int firstIndex, int wordLength) {
        // Write out the index where the word first occurs in the text
        BinaryStdOut.write(firstIndex, CODE_LENGTH_INDEX);
        // Then write out the length of the word
        BinaryStdOut.write(wordLength, CODE_LENGTH_LEN);
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
