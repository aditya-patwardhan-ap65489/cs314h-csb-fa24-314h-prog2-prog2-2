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
    // The level of the analysis being done.
    private final int level;
    // The text used to generate output.
    private String inputText = "";
    private final Map<String, List<Character>> seedToNextCharacters;

    /**
     * Randomly writes based on text from the specified input file.
     * @param args - Command line arguments
     * args[0] is the input filename
     * args[1] is the output filename
     * args[2] is the level of analysis (k)
     * args[3] is the length (number of characters) of output
     */
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
    public static boolean validateInput(String[] args) throws IOException {
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
            System.err.println("k and length must be non-negative integers.");
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
        int inputCharCount = getFileCharCount(source);
        if (inputCharCount <= k) {
            System.err.println("The input file must have more than k characters.");
            System.err.println(source + " has " + inputCharCount + "characters.");
        }

        // Check that the result file is ok, or deal with resulting problems.
        File resultFile = new File(result);
        if (!resultFile.exists()) {
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
     * Returns the number of characters in a file.
     * @param filename - Name of file to read contents of.
     * @throws IOException - If the file does not exist.
     */
    private static int getFileCharCount(String filename) throws IOException {
        String contents = fileContentsToString(filename);
        return contents.length(); // Number of characters = length of contents.
    }

    /**
     * Static factory method for RandomWriter.
     * @param level - The level of text analysis to perform.
     * @return The newly instantiated TextProcessor.
     */
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
     * @param inputFilename - Name of the source text file.
     */
    public void readText(String inputFilename) {
        // Store input text and compute next letter frequency map for writing.
        try {
            this.inputText = fileContentsToString(inputFilename);
            populateNextLetterMap();
        } catch (IOException e) {
            System.err.println("File " + inputFilename + " was not found or ");
            System.err.println("could not be read.");
        }
    }

    private static String fileContentsToString(String inputFilename)
            throws IOException {
        BufferedReader textReader = new BufferedReader(new FileReader(
                inputFilename));
        StringBuffer textBuffer = new StringBuffer();

        // Read in input file.
        String line = textReader.readLine();
        while (line != null) {
            textBuffer.append(line).append('\n');
            line = textReader.readLine();
        }
        // Remove the last newline character
        if (textBuffer.length() > 0) {
            textBuffer.deleteCharAt(textBuffer.length() - 1);
        }

        textReader.close();
        return textBuffer.toString();
    }

    /**
     * Populates seedToNextCharacters from the input text.
     */
    public void populateNextLetterMap() {
        // Clear map because the same RandomWriter can analyze multiple texts.
        seedToNextCharacters.clear();
        int lastSeedIndex = inputText.length() - level - 1;

        // Iterate over possible seeds to populate list of next letters.
        for (int i = 0; i <= lastSeedIndex; i++) {
            String seed = inputText.substring(i, i + level);
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
     */
    public void writeText(String outputFilename, int length) {
        try {
            FileWriter outputWriter = new FileWriter(outputFilename);
            String generatedText = generateText(length);
            outputWriter.write(generatedText);
            outputWriter.close();
        } catch (IOException e) {
            System.err.println("Could not write to output file " +
                    outputFilename);
        }
    }

    /**
     * Probabilistically generates text of (length) characters using input text.
     * @param length - Number of characters to generate
     * @return String - the generated text.
     */
    public String generateText(int length) {
        StringBuffer generatedTextBuffer = new StringBuffer();
        String seed = pickRandomSeed();

        // Edge case - seedToNextCharacters is empty (caused by empty input)
        if (seedToNextCharacters.isEmpty()) {
            return "";
        }

        // Generate (length) random characters.
        for (int i = 0; i < length; i++) {
            // Get random seed if current seed doesn't occur in the text.
            if (!seedToNextCharacters.containsKey(seed)) {
                seed = pickRandomSeed();
            }

            // Pick a character using the seed and write it to the output file.
            List<Character> possibleNextChars = seedToNextCharacters.get(seed);
            int randomIndex = (int)(Math.random() * possibleNextChars.size());
            char randomChar = possibleNextChars.get(randomIndex);
            generatedTextBuffer.append(randomChar);

            // Update seed
            if (level != 0) {
                seed = seed.substring(1) + randomChar;
            }
        }
        return generatedTextBuffer.toString();
    }

    /**
     * Chooses a random seed from the text for analysis.
     * @return String which is the random seed.
     */
    public String pickRandomSeed() {
        int maxSeedStartIndex = inputText.length() - 1 - level;
        int seedStartIndex = (int)(Math.random() * (maxSeedStartIndex + 1));
        return inputText.substring(seedStartIndex, seedStartIndex + level);
    }

    /**
     * Accessor for seedToNextCharacters for white-box testing.
     * @return Map<String, List<Character>> - seedToNextCharacters
     */
    public Map<String, List<Character>> getSeedToNextCharacters() {
        return seedToNextCharacters;
    }
}
