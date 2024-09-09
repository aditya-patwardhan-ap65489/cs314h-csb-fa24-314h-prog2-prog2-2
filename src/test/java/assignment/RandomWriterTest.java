package test.java.assignment;

import main.java.assignment.RandomWriter;
import main.java.assignment.TextProcessor;
import org.junit.Test;

import java.io.*;

public class TestRandomWriter {
    /**
     * Tests RandomWriter using cases with manually checkable answers.
     */
    @Test
    public void testRandomWriterManualInspection() throws IOException {
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
        // Test with all the same character
        // Using non-alphabet also covers a potential corner case
        analysisLevel = 3;
        generator = RandomWriter.createProcessor(analysisLevel);
        String allAndFilename = "test_text/And.txt";
        generator.readText(allAndFilename);
        outputFilename = "AndOut.txt";
        generator.writeText(outputFilename, outputLen);
        String repeatAnd = "&".repeat(outputLen);
        // The generated text should be all &'s
        outputText = getFileContents(outputFilename);
        assert outputText.equals(repeatAnd);
    }

    /**
     * Tests RandomWriter using probabilistic methods.
     * The probabilities of a certain next letter given a seed
     * should be close to each other for the input and output.
     * @throws IOException - if the input file is not found.
     */
    @Test
    public void testRandomWriterMonteCarlo() throws IOException {
        int inputTextLength = 1000; // Source text is 1000 random characters
        int outputLength = 1000000; // 1 million characters of output
        int maxAnalysisLevel = 10;
        // Test that the writer output makes sense for 10 levels of analysis
        for (int k = 0; k < maxAnalysisLevel; k++) {
            String inputTextFilename = "test_text/random_text_level"
                    + k + ".txt";
            writeUniformRandomText(inputTextFilename, inputTextLength);
            RandomWriter writer = (RandomWriter)
                    RandomWriter.createProcessor(k);
            writer.readText(inputTextFilename);
            String outputTextFilename = "test_text/random_text_level"
                    + k + "_out.txt"; // "out" in filename indicates it's output
            writer.writeText(outputTextFilename, outputLength);

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
}
