import org.matsim.api.core.v01.Coord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DestinationsReader {
    private Map<String, Coord> destinationsCoordinatesMap = new HashMap<>();

    public DestinationsReader(String filename) throws IOException {
        this.readFile(filename);
    }
    public void readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String[] splitLine;
        String newLine;
        br.readLine();
        while((newLine = br.readLine()) != null) {
            splitLine = newLine.split("[,]+");

            String destinationName = splitLine[1];
            Coord destinationCoordinates = new Coord(Double.parseDouble(splitLine[4]), Double.parseDouble(splitLine[5]));
            this.destinationsCoordinatesMap.put(destinationName, destinationCoordinates);
        }
        br.close();
    }

    public Map<String, Coord> getDestinationsCoordinatesMap(){
        return this.destinationsCoordinatesMap;
    }
}
