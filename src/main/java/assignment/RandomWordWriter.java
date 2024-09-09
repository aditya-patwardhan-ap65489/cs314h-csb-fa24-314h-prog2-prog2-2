package main.java.assignment;
import java.io.*;
import java.util.*;

/*
 * CS 314H Assignment 2 - Random Writing
 *
 * Implementation of RandomWordWriter class
 */
public class RandomWordWriter implements TextProcessor {
    private final int level; // The level of the analysis being done.
    private String[] inputTextWords; // The words of the input text.
    private final Map<String, List<String>> seedToNextWords;

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
        int inputWordCount = getFileWordCount(sourceFile);
        if (inputWordCount < k) {
            System.err.println("The input file must have more than k characters.");
            System.err.println(source + " has " + inputWordCount + "characters.");
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
     * Returns the number of words in a file.
     * @param file - File to read contents of.
     * @throws IOException - If the file does not exist.
     */
    private static int getFileWordCount(File file)
            throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        int wordCount = 0;

        // Read lines to determine total number of words.
        String line = fileReader.readLine();
        while (line != null) {
            String[] words = line.split(" ");
            wordCount += words.length;
            line = fileReader.readLine();
        }
        fileReader.close();
        return wordCount;
    }

    /**
     * Static factory method for RandomWriter.
     * @param level - The level of text analysis to perform.
     * @return The newly instantiated TextProcessor.
     */
    public static TextProcessor createProcessor(int level) {
        return new RandomWordWriter(level);
    }

    /**
     * Private constructor for the RandomWriter class.
     * @param level - The level of analysis used to generate random output.
     */
    private RandomWordWriter(int level) {
        this.level = level;
        this.seedToNextWords = new HashMap<>();
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
            textBuffer.append(line).append('\n');
            line = textReader.readLine();
        }
        textReader.close();

        // Store input text words and compute next word frequency map.
        this.inputTextWords = textBuffer.toString().split(" ");
        populateNextWordMap();
    }

    /**
     * Populates seedToNextWords from the input text.
     */
    private void populateNextWordMap() {
        int lastSeedIndex = inputTextWords.length - level - 1;

        // Iterate over possible seeds to populate list of next words.
        for (int i = 0; i <= lastSeedIndex; i++) {
            StringBuffer seedBuffer = new StringBuffer();
            for (int j = 0; j < level; j++) {
                seedBuffer.append(inputTextWords[i + j]);
                if (j < level - 1) {
                    seedBuffer.append(' ');
                }
            }
            String seed = seedBuffer.toString();

            // Create new key if this seed hasn't been encountered yet.
            if (!seedToNextWords.containsKey(seed)) {
                List<String> nextWords = new ArrayList<>();
                seedToNextWords.put(seed, nextWords);
            }

            // Add word after the seed to seed's list of next word.
            int nextWordIndex = i + level;
            String nextWord = inputTextWords[nextWordIndex];
            seedToNextWords.get(seed).add(nextWord);
        }
    }

    /**
     * Writes text generated using input text to the output file.
     * @param outputFilename - Name of the file to write to.
     * @param length - Number of words to write (non-negative).
     * @throws IOException - If the destination file cannot be written to.
     */
    public void writeText(String outputFilename, int length) throws IOException {
        FileWriter outputWriter = new FileWriter(outputFilename);
        String generatedText = generateText(length);
        outputWriter.write(generatedText);
        outputWriter.close();
    }

    /**
     * Probabilistically generates text of (length) words using input text.
     * @param length - Number of words to generate
     * @return String - the generated text.
     */
    private String generateText(int length) {
        StringBuffer generatedTextBuffer = new StringBuffer();
        String seed = getRandomSeed();
        System.out.println("seed="+seed);
        // Generate random words.
        for (int i = 0; i < length; i++) {
            // Get random seed if current seed doesn't occur in the text.
            if (!seedToNextWords.containsKey(seed)) {
                seed = getRandomSeed();
                System.out.println("seed="+seed);
            }

            // Pick a word using the seed and write it to the output file.
            List<String> possibleNextWords = seedToNextWords.get(seed);
            int randomIndex = (int)(Math.random() * possibleNextWords.size());
            String randomWord = possibleNextWords.get(randomIndex);
            generatedTextBuffer.append(randomWord).append(' ');

            // Update seed
            String[] seedIndividualWords = seed.split(" ");
            StringBuffer newSeedBuffer = new StringBuffer();
            if (level != 0) {
                for (int j = 1; j < level; j++) {
                    newSeedBuffer.append(seedIndividualWords[j]).append(' ');
                }
                newSeedBuffer.append(randomWord);
            }
            seed = newSeedBuffer.toString();
        }
        return generatedTextBuffer.toString();
    }

    /**
     * Gets a random seed from the text for analysis.
     * @return String which is the random seed.
     */
    private String getRandomSeed() {
        int maxSeedStartIndex = inputTextWords.length - 1 - level;
        int seedStartIndex = (int)(Math.random() * (maxSeedStartIndex + 1));
        StringBuffer seedBuffer = new StringBuffer();
        for (int i = seedStartIndex; i < seedStartIndex + level; i++) {
            seedBuffer.append(inputTextWords[i]);
            if (i < seedStartIndex + level - 1) {
                seedBuffer.append(' ');
            }
        }
        return seedBuffer.toString();
    }
}
