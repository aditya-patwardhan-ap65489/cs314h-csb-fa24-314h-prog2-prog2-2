package test.java.assignment;

import main.java.assignment.RandomWriter;
import main.java.assignment.TextProcessor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
        String allAFilename = "test_text/And.txt";
        generator.readText(allAFilename);
        outputFilename = "AOut.txt";
        generator.writeText(outputFilename, outputLen);
        String repeatA = "&".repeat(outputLen);
        // The generated text should be all A's
        outputText = getFileContents(outputFilename);
        assert outputText.equals(repeatA);
    }

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
}
