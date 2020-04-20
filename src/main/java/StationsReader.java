import org.matsim.api.core.v01.Coord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StationsReader {
    private Map<Integer,Station> stationsMap = new HashMap<>();

    public StationsReader(String filename) throws IOException {
        this.readFile(filename);
    }

    public void readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String[] splitLine;
        String newLine;
        br.readLine();
        while((newLine = br.readLine()) != null) {
            splitLine = newLine.split("[,]+");

            String stationId = splitLine[1].replaceAll("\"", "");
            System.out.println(Arrays.toString(splitLine));
            Coord stationCoordinates = new Coord(Double.parseDouble(splitLine[splitLine.length - 2 ]),
                    Double.parseDouble(splitLine[splitLine.length - 1]));
            String roadType;
            String destination1 = splitLine[3];
            String destination2 = splitLine[4];


            if (splitLine[2].equals("A")){
                roadType = "AUTOBAHN";
            }
            else{
                roadType = "BUNDESSTRASSE";
            }
            this.stationsMap.put(Integer.parseInt(stationId), new Station(stationId, stationCoordinates, destination1, destination2, Station.RoadType.valueOf(roadType)));
        }
        br.close();
    }
    public Map<Integer,Station> getStationsMap(){
        return this.stationsMap;
    }
}
