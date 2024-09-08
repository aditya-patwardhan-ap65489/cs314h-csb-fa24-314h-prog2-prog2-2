package main.java.assignment;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * CS 314H Assignment 2 - Random Writing
 *
 * Your task is to implement this RandomWriter class
 */
public class RandomWriter implements TextProcessor {
    private final int level; // The level of the analysis being done.
    private String inputText; // The text used to generate output.
    private final Map<String, List<Character>> seedToNextCharacters;

    /**
     * Randomly writes based on text from the specified input file.
     * @param args - Command line arguments
     * args[0] is the input filename
     * args[1] is the output filename, args[2] is the level of
     *             analysis, and args[3] is the length of output.
     */
    // TODO: Check style guide on exception handling
    public static void main(String[] args) throws IOException {
        if (validateInput(args)) {
            // Get input arguments
            String source = args[0];
            String result = args[1];
            int k = Integer.parseInt(args[2]);
            int length = Integer.parseInt(args[3]);

            // Write random text based on input
            TextProcessor processor = createProcessor(k);

            processor.readText(source);
            processor.writeText(result, length);
        } else {
            System.err.println("Terminating program");
        }
    }

    /**
     * Helper for main method to validate the input.
     * @param args - The command line arguments.
     * @return boolean - Whether the input arguments are valid.
     */
    private static boolean validateInput(String[] args) throws IOException {
        // Exit if not all arguments are provided
        if (args.length != 4) {
            System.err.println("Exactly 4 arguments must be provided.");
            return false;
        }

        // Parse command line arguments
        String source = args[0];
        String result = args[1];
        int k, length;

        // Edge case where k or length are not integers
        try {
            k = Integer.parseInt(args[2]);
            length = Integer.parseInt(args[3]);
        } catch (Exception e) {
            System.err.println("k and length must be integers.");
            return false;
        }

        // Deal with k < 0 or length < 0
        if (k < 0) {
            System.err.println("The level of analysis (k) must be non-negative.");
            System.err.println("You set k = " + k);
            return false;
        }
        if (length < 0) {
            System.err.println("The length of output must be non-negative.");
            System.err.println("You set length = " + length);
            return false;
        }

        // Deal with source file problems.
        File sourceFile = new File(source);
        if (!sourceFile.exists()) {
            System.err.println("Source file " + source + " does not exist.");
            return false;
        }
        if (!sourceFile.canRead()) {
            System.err.println("Source file " + source + " is not readable.");
            return false;
        }
        // Source file not long enough for k-th order analysis
        int inputCharCount = getFileCharCount(sourceFile);
        if (inputCharCount < k) {
            System.err.println("The input file must have more than k characters.");
            System.err.println(source + " has " + inputCharCount + "characters.");
        }

        // Check that the result file is ok, or deal with resulting problems.
        File resultFile = new File(result);
        if (!resultFile.exists()) {
            // TODO: Check try catch on the style guide
            try {
                resultFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create result file " + result);
                return false;
            }
        }
        if (!resultFile.canWrite()) {
            System.err.println("The result file cannot be written.");
            return false;
        }
        return true;
    }

    /**
     * Determines whether a file has more than k characters in it.
     * @param file - File to read contents of.
     * @return boolean - Whether the file has at least k characters.
     * @throws IOException - If the file does not exist.
     */
    private static int getFileCharCount(File file)
            throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        int charCount = 0;

        // Read lines to determine total number of characters.
        String line = fileReader.readLine();
        while (line != null) {
            charCount += line.length();
            line = fileReader.readLine();
        }
        fileReader.close();
        return charCount;
    }

    // Unless you need extra logic here, you might not have to touch this method
    public static TextProcessor createProcessor(int level) {
      return new RandomWriter(level);
    }

    /**
     * Private constructor for the RandomWriter class.
     * @param level - The level of analysis used to generate random output.
     */
    private RandomWriter(int level) {
        this.level = level;
        this.seedToNextCharacters = new HashMap<>();
    }

    /**
     * Reads the text from a file for level-k analysis.
     * @param inputFilename - Filename of the source text file.
     * @throws IOException - If the file with the specified name doesn't exist.
     */
    public void readText(String inputFilename) throws IOException {
        BufferedReader textReader = new BufferedReader(new FileReader(
                inputFilename));
        StringBuffer textBuffer = new StringBuffer();

        // Read in input file.
        String line = textReader.readLine();
        while (line != null) {
            textBuffer.append(line);
            line = textReader.readLine();
        }
        textReader.close();

        this.inputText = textBuffer.toString();
        populateNextLetterMap();
    }

    /**
     * Populates seedToNextCharacters from the input text.
     */
    private void populateNextLetterMap() {
        int lastSeedIndex = inputText.length() - level - 1;

        // Iterate over possible seeds to populate list of next letters.
        for (int i = 0; i < lastSeedIndex; i++) {
            String seed = inputText.substring(i, i + level);
            seed = seed.toLowerCase(); // The program spec does this.
            // Create new key if this seed hasn't been encountered yet.
            if (!seedToNextCharacters.containsKey(seed)) {
                List<Character> nextChars = new ArrayList<>();
                seedToNextCharacters.put(seed, nextChars);
            }

            // Add character after the seed to seed's list of next characters.
            int nextLetterIndex = i + level;
            char nextLetter = inputText.charAt(nextLetterIndex);
            seedToNextCharacters.get(seed).add(nextLetter);
        }
    }

    /**
     * Writes text generated using input text to the output file.
     * @param outputFilename - Name of the file to write to.
     * @param length - Number of characters to write (non-negative).
     * @throws IOException - If the destination file cannot be written to.
     */
    public void writeText(String outputFilename, int length) throws IOException {
        FileWriter outputWriter = new FileWriter(outputFilename);
        String seed = getRandomSeed();

        // Write (length) characters to the output file.
        for (int i = 0; i < length; i++) {
            // Get random seed if current seed doesn't occur in the text.
            if (!seedToNextCharacters.containsKey(seed)) {
                seed = getRandomSeed().toLowerCase();
            }

            // Pick a character using the seed and write it to the output file.
            List<Character> possibleNextChars = seedToNextCharacters.get(seed);
            int randomIndex = (int)(Math.random() * possibleNextChars.size());
            char randomChar = possibleNextChars.get(randomIndex);
            outputWriter.write(randomChar);

            // Update seed
            seed = seed.substring(1) + randomChar;
            seed = seed.toLowerCase();
        }
        outputWriter.close();
    }

    /**
     * Gets a random seed from the text for analysis.
     * @return String which is the random seed.
     */
    private String getRandomSeed() {
        int maxSeedStartIndex = inputText.length() - 1 - level;
        int seedStartIndex = (int)(Math.random() * (maxSeedStartIndex + 1));
        return inputText.substring(seedStartIndex, seedStartIndex + level);
    }
}
