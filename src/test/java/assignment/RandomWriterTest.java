package test.java.assignment;

import main.java.assignment.RandomWriter;
import main.java.assignment.TextProcessor;
import org.junit.Test;

import java.util.*;
import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RandomWriterTest {
    /**
     * Tests RandomWriter's input validation for the main method.
     * Should not throw an Exception.
     */
    @Test
    public void testRandomWriterInputValidation() throws IOException {
        // Wrong # of arguments
        String[] args1 = new String[]{"arg0", "arg1", "arg2"};
        assertThrows(IllegalArgumentException.class,
                ()->RandomWriter.validateInput(args1));

        // Invalid file names
        String[] args2 = new String[]{"notafile", "notafile", "1", "200"};
        assertThrows(IOException.class,
                ()->RandomWriter.validateInput(args2));

        // k or length aren't integers
        String[] args3 = new String[]{"test_books/MuchAdo.txt",
                "MuchAdoOut.txt", "abc", "123"};
        assertThrows(IllegalArgumentException.class,
                ()->RandomWriter.validateInput(args3));
        String[] args4 = new String[]{"test_books/MuchAdo.txt", "MuchAdoOut.txt", "1.23", "123"};
        assertThrows(IllegalArgumentException.class,
                ()->RandomWriter.validateInput(args4));

        // k or length aren't nonnegative
        String[] args5 = new String[]{"test_books/MuchAdo.txt", "MuchAdoOut.txt", "-1", "2"};
        assertThrows(IllegalArgumentException.class,
                ()->RandomWriter.validateInput(args5));
        String[] args6 = new String[]{"test_books/MuchAdo.txt", "MuchAdoOut.txt", "1", "-2"};
        assertThrows(IllegalArgumentException.class,
                ()->RandomWriter.validateInput(args6));

        // length < k
        String[] args7 = new String[]{"test_text/Blank.txt", "BlankOut.txt", "1", "2"};
        assertThrows(IllegalArgumentException.class,
                ()->RandomWriter.validateInput(args7));
        // Input file does not exist
        String[] args8 = new String[]{"test_text/Nonexistent.txt", "NonexistentOut.txt", "1", "2"};
        assertThrows(IOException.class,
                ()->RandomWriter.validateInput(args8));

        // This is actually valid input, shouldn't throw an Exception
        String[] args9 = new String[]{"test_books/MuchAdo.txt", "MuchAdoOut2.txt", "1", "2"};
        RandomWriter.validateInput(args9);

        // Same, but output file doesn't exist, shouldn't throw an Exception
        String[] args10 = new String[]{"test_books/MuchAdo.txt", "MuchAdoOut1234.txt", "1", "2"};
        RandomWriter.validateInput(args10);
    }

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
     * Tests randomWriter readText to make sure it properly reads files.
     */
    @Test
    public void testReading() throws IOException {
        String alphabetReadTwice =
                RandomWriter.fileContentsToString("test_text/ABCs.txt");
        String alphabetActual = "abcdefghijklmnopqrstuvwxyz";
        assertEquals(alphabetReadTwice, alphabetActual+alphabetActual);
        String hindiAlphabetRead = RandomWriter.fileContentsToString(
                "test_text/Hindi.txt");
        String hindiAlphabetActual = "कखगघड़चछजझञटठडढणतथदधनपफबभमयरलवशषसह";
        assertEquals(hindiAlphabetRead, hindiAlphabetActual);
    }

    /**
     * Checks that RandomWriter works using a string of all 1 character.
     * @throws IOException - If the input file doesn't exist or output file
     * cannot be written to.
     */
    private void checkAllOneCharacter() throws IOException {
        // Test with all the same character
        // Using non-alphabet also covers a potential corner case
        int outputLength = 26;
        int analysisLevel = 3;
        TextProcessor generator = RandomWriter.createProcessor(analysisLevel);
        String allAndFilename = "test_text/And.txt";
        generator.readText(allAndFilename);
        String outputFilename = "AndOut.txt";
        generator.writeText(outputFilename, outputLength);
        String repeatAnd = "&".repeat(outputLength);

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
        int outputLength = 26; // 26 characters of output
        int analysisLevel = 1;

        // Test using the alphabet
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        String alphabetTwice = alphabet + alphabet;
        TextProcessor generator = RandomWriter.createProcessor(analysisLevel);
        String alphabetFilename = "test_text/ABCs.txt";
        generator.readText(alphabetFilename);
        String outputFilename = "ABCsOut.txt";
        generator.writeText(outputFilename, outputLength);

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
        String inputFilename = "test_text/BeeMovie.txt";
        int outputLength = 1_000_000;
        int maxAnalysisLevel = 10;
        int analysisLevelIncrement = 1;
        // Ensure next character distributions are same to 1% accuracy
        double tolerance = 0.03;

        // Check writer output for multiple analysis levels
        for (int k = 0; k <= maxAnalysisLevel; k += analysisLevelIncrement) {
            RandomWriter writer = (RandomWriter) RandomWriter.createProcessor(k);
            // Read input text, write to output
            writer.readText(inputFilename);
            Map<String, List<Character>> seedToNextCharsInput =
                    new HashMap<>(writer.getSeedToNextCharacters());
            String outputTextFilename = "test_text/random_text_level"
                    + k + "_out.txt";
            writer.writeText(outputTextFilename, outputLength);
            // Read in the outputted text for comparative analysis
            writer.readText(outputTextFilename);
            Map<String, List<Character>> seedToNextCharsOutput =
                    new HashMap<>(writer.getSeedToNextCharacters());

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

        double averageError = 0.0;
        int divBy = 0;

        // Iterate through seeds to compare the next character distributions.
        for (String seed : allSeeds) {
            Map<Character, Double> probsFirst = first.getOrDefault(seed,
                    new HashMap<>());
            Map<Character, Double> probsSecond = second.getOrDefault(seed,
                    new HashMap<>());
            Set<Character> allNextChars = new HashSet<>(probsFirst.keySet());
            allNextChars.addAll(probsSecond.keySet());

            // Check that probabilities for each next character match.
            for (Character nextChar : allNextChars) {
                double curCharFirstProb = probsFirst.getOrDefault(nextChar,
                        0.0);
                double curCharSecondProb = probsSecond.getOrDefault(
                        nextChar, 0.0);
                averageError += Math.abs(curCharFirstProb - curCharSecondProb);
                divBy++;
            }
        }

        averageError /= divBy;
        return averageError <= tolerance;
    }
}
