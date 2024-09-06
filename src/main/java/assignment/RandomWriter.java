package assignment;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
 * CS 314H Assignment 2 - Random Writing
 *
 * Your task is to implement this RandomWriter class
 */
public class RandomWriter implements TextProcessor {
    private int level; // The level of the analysis being done.
    private Map<String, List<String>> seedToNextCharacters;

    /**
     * Randomly writes based on text from the specified input file.
     * @param args - Command line arguments: args[0] is the input filename,
     *             args[1] is the output filename, args[2] is the level of
     *             analysis, and args[3] is the length of output.
     */
    public static void main(String[] args) throws IOException {
        if (validateInput(args)) {
            String source = args[0];
            String result = args[1];
            int k = Integer.parseInt(args[2]);
            int length = Integer.parseInt(args[3]);

            TextProcessor processor = createProcessor(k);

            // TODO: Check style guide on exception handling
            processor.readText(source);
            processor.writeText(result, length);
        }
    }

    /**
     * Helper for main method to validate the input.
     * @param args - The command line arguments.
     * @return boolean - Whether the input arguments are valid.
     */
    private static boolean validateInput(String[] args) {
        // Exit if not all arguments are provided
        if (args.length != 4) {
            System.err.print("Exactly 4 arguments must be provided. ");
            System.out.println("Terminating program");
            return false;
        }

        // Parse command line arguments
        String source = args[0];
        String result = args[1];
        int k = Integer.parseInt(args[2]);
        int length = Integer.parseInt(args[3]);

        File sourceFile = new File(source);
        if (!sourceFile.exists()) {
            // TODO: Improve error message
            System.err.println("The source file " + source + " does not exist.");
            return false;
        }
        // TODO: Also include code to check result.
        if (k < 0) {
            System.err.println("The level of analysis (k) must be non-negative.");
            System.err.println("You set k = " + k);
            return false;
        }
        if (length < 0) {
            System.err.println("The length of output must be non-negative.");
            System.out.println("You set length to be " + length);
            return false;
        }
        return true;
    }

    // Unless you need extra logic here, you might not have to touch this method
    public static TextProcessor createProcessor(int level) {
      return new RandomWriter(level);
    }

    private RandomWriter(int level) {
      // Do whatever you want here
        this.level = level;
        this.seedToNextCharacters = new HashMap<String, List<String>>();
    }


    public void readText(String inputFilename) throws IOException {

    }

    public void writeText(String outputFilename, int length) throws IOException {

    }
}
