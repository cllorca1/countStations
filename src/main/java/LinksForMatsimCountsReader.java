import org.matsim.api.core.v01.Coord;

import java.io.*;
import java.util.Arrays;

public class LinksForMatsimCountsReader {

    public static void main(String[] args) throws IOException {

        //args[0] = "./networks/matsim/stationsWithMatsimLinksCORRECTED.csv" (inputFile)
        //args[1] = "./input/linksForCounts_BaST_Germany.csv" (outputFile)
        LinksForMatsimCountsReader linksForMatsimCountsReader = new LinksForMatsimCountsReader(args[0], args[1]);
    }
    public LinksForMatsimCountsReader(String inputFilename, String outputFileName) throws IOException {
        this.readFile(inputFilename, outputFileName);
    }

    public void readFile(String inputFileName, String outputFileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
        String newLine;
        while((newLine = br.readLine()) != null) {
            String[] splitLine;
            splitLine = newLine.split("[;]|[\r]");

            if (splitLine.length == 10) {
                String linkDestination1 = splitLine[5];
                String linkDestination2 = splitLine[8];

                if (!linkDestination1.equals("")) {
                    bw.write(linkDestination1);
                    bw.newLine();
                }
                if (!linkDestination2.equals("")) {
                    bw.write(linkDestination2);
                    bw.newLine();
                }
            }
        }
        br.close();
        bw.close();
    }
}