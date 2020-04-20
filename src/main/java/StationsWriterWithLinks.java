import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StationsWriterWithLinks {
    BufferedWriter outputBufferedWriter;

    public StationsWriterWithLinks(String outputFileName) throws IOException {
        File outputFile = new File(outputFileName);
        this.outputBufferedWriter = new BufferedWriter(new FileWriter(outputFile));
    }

    public void addLineToOutputBW(String lineToAdd) throws IOException {
        this.outputBufferedWriter.write(lineToAdd);
        this.outputBufferedWriter.newLine();
    }

    public void closeOutputBW() throws IOException {
        this.outputBufferedWriter.close();
    }
}
