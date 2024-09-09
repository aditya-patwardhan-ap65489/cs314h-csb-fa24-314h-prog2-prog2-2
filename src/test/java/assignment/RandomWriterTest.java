package test.java.assignment;

import main.java.assignment.RandomWriter;
import main.java.assignment.TextProcessor;
import org.junit.Test;

import java.util.*;
import java.io.*;

public class RandomWriterTest {
    /**
     * Tests RandomWriter using cases with manually checkable answers.
     */
    @Test
    public void testRandomWriterManualInspection() throws IOException {
        // Test some simple manually inspectable cases.
        checkAlphabet();
        checkAllOneCharacter();
    }

    /**
     * Checks that RandomWriter works using a string of all 1 character.
     * @throws IOException - If the input file doesn't exist or output file
     * cannot be written to.
     */
    private void checkAllOneCharacter() throws IOException {
        // Test with all the same character
        // Using non-alphabet also covers a potential corner case
        int outputLen = 26;
        int analysisLevel = 3;
        TextProcessor generator = RandomWriter.createProcessor(analysisLevel);
        String allAndFilename = "test_text/And.txt";
        generator.readText(allAndFilename);
        String outputFilename = "AndOut.txt";
        generator.writeText(outputFilename, outputLen);
        String repeatAnd = "&".repeat(outputLen);

        // The generated text should be all ampersands.
        String outputText = getFileContents(outputFilename);
        assert outputText.equals(repeatAnd);
    }

    /**
     * Helper to manually prove RandomWriter works using the alphabet as input.
     * @throws IOException - If the input file doesn't exist or output file
     * cannot be written to.
     */
    private void checkAlphabet() throws IOException {
        int outputLen = 26; // 26 characters of output
        int analysisLevel = 1;

        // Test using the alphabet
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        String alphabetTwice = alphabet + alphabet;
        TextProcessor generator = RandomWriter.createProcessor(analysisLevel);
        String alphabetFilename = "test_text/ABCs.txt";
        generator.readText(alphabetFilename);
        String outputFilename = "ABCsOut.txt";
        generator.writeText(outputFilename, outputLen);

        // The generated text should be the alphabet with some offset
        String outputText = getFileContents(outputFilename);
        assert alphabetTwice.contains(outputText);
    }

    /**
     * Tests RandomWriter using probabilistic methods.
     * The probabilities of a certain next letter given a seed
     * should be close to each other for the input and output.
     * @throws IOException - if the input file is not found.
     */
    @Test
    public void testRandomWriterMonteCarlo() throws IOException {
        String inputFilename = "test_books/MuchAdo.txt";
        int outputLength = 1_000_000;
        int maxAnalysisLevel = 100;
        int analysisLevelIncrement = 10;
        // Ensure next character distributions are same to 1% accuracy
        double tolerance = 0.01;

        // Check writer output for multiple analysis levels
        for (int k = 0; k < maxAnalysisLevel; k += analysisLevelIncrement) {
            RandomWriter writer = (RandomWriter) RandomWriter.createProcessor(k);
            // Read input text, write to output
            writer.readText(inputFilename);
            Map<String, List<Character>> seedToNextCharsInput =
                    writer.getSeedToNextCharacters();
            String outputTextFilename = "test_text/random_text_level"
                    + k + "_out.txt";
            writer.writeText(outputTextFilename, outputLength);
            // Read in the outputted text for comparative analysis
            writer.readText(outputTextFilename);
            Map<String, List<Character>> seedToNextCharsOutput =
                    writer.getSeedToNextCharacters();

            // Compare input/output distributions
            Map<String, Map<Character, Double>> inputTextProbDistribution =
                    getSeedToNextCharProbabilities(seedToNextCharsInput);
            Map<String, Map<Character, Double>> outputTextProbDistribution =
                    getSeedToNextCharProbabilities(seedToNextCharsOutput);
            assert equalProbs(inputTextProbDistribution,
                    outputTextProbDistribution, tolerance);
        }
    }

    /**
     * Private helper to read a file.
     * @param filename - name of file to read.
     * @return - String with the file text.
     * @throws IOException - If the file is not found.
     */
    private String getFileContents(String filename) throws IOException {
        BufferedReader textReader = new BufferedReader(new FileReader(
                filename));
        StringBuffer textBuffer = new StringBuffer();

        // Read in input file.
        String line = textReader.readLine();
        while (line != null) {
            textBuffer.append(line);
            line = textReader.readLine();
        }
        textReader.close();

        // Return as String
        return textBuffer.toString();
    }

    /**
     * Helper to write random uniform text to a file.
     * A character is selected at random and written.
     * @param filename - Name of file to write to as a String.
     * @param length - Number of characters to write.
     */
    private void writeUniformRandomText(String filename, int length)
            throws IOException {
        BufferedWriter textWriter = new BufferedWriter(new FileWriter(filename));
        StringBuffer textBuffer = new StringBuffer();
        String chars = "abcdefghijklmnopqrstuvwxyz1234567890 ";
        int charsLength = chars.length();

        // Generate a random text of characters
        for (int i = 0; i < length; i++) {
            int randomIndex = (int)(Math.random() * charsLength);
            char randomChar = textBuffer.charAt(randomIndex);
            textBuffer.append(randomChar);
        }

        // Write it to the output file
        String text = textBuffer.toString();
        textWriter.write(text);
        textWriter.close();
    }


    /**
     * Helper to compute probabilities of a certain next character given a seed.
     * Uses an input map with raw data on next characters to do this.
     *
     */
    private Map<String, Map<Character, Double>> getSeedToNextCharProbabilities(
            Map<String, List<Character>> seedToNextChars) {
        Map<String, Map<Character, Double>> seedToNextCharProbs =
                new HashMap<>(); // The Map to return

        // Go through each seed, convert the list it maps to
        // into a map with frequencies for each next character.
        for (String seed : seedToNextChars.keySet()) {
            List<Character> nextChars = seedToNextChars.get(seed);
            Map<Character, Double> probsForOneSeed =
                    getProbsForOneSeed(nextChars);
            seedToNextCharProbs.put(seed, probsForOneSeed);
        }
        return seedToNextCharProbs;
    }

    /**
     * Helper that gets probabilities for each next character for a seed,
     * given the list of next characters.
     * @param nextChars - List of characters that come after a seed.
     * @return Map<Character, Double> with the probability of each character
     * appearing after the seed.
     */
    private static Map<Character, Double> getProbsForOneSeed(List<Character> nextChars) {
        Map<Character, Double> probs = new HashMap<>();
        // Compute cumulative frequencies
        for (Character nextChar : nextChars) {
            if (!probs.containsKey(nextChar)) {
                probs.put(nextChar, 0.0);
            }
            // Increment count for that char
            probs.put(nextChar, probs.get(nextChar) + 1);
        }
        double numNextChars = nextChars.size();
        // Turn into probability by dividing by total # of next characters
        for (Character nextChar : probs.keySet()) {
            probs.put(nextChar, probs.get(nextChar) / numNextChars);
        }
        return probs;
    }

    /**
     * Checks that the 2 texts (input and output) have the same probability
     * distribution for the next character after a seed, to within
     * a specified tolerance.
     * @param first - Map with probabilities for the first text
     * @param second - Map with probabilities for the second text
     * @param tolerance - Maximum probability difference allowed
     * between corresponding entries in first and second.
     * @return boolean - Whether the distributions are equal to within
     * the specified tolerance.
     */
    private boolean equalProbs(Map<String, Map<Character, Double>> first,
                               Map<String, Map<Character, Double>> second,
                               double tolerance) {
        Set<String> allSeeds = new HashSet<>(first.keySet());
        allSeeds.addAll(second.keySet()); // All seeds in both texts

        // Iterate through seeds to compare the next character distributions.
        for (String seed : allSeeds) {
            Map<Character, Double> probsFirst = first.get(seed);
            Map<Character, Double> probsSecond = second.get(seed);
            Set<Character> allNextChars = new HashSet<>(probsFirst.keySet());
            allNextChars.addAll(probsSecond.keySet());

            // Check that probabilities for each next character match.
            for (Character nextChar : allNextChars) {
                double curCharFirstProb = probsFirst.getOrDefault(nextChar,
                        0.0);
                double curCharSecondProb = probsSecond.getOrDefault(
                        nextChar, 0.0);
                if (Math.abs(curCharFirstProb - curCharSecondProb) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }
}
