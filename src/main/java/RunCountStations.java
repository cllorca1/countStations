import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.dvrp.router.DijkstraTree;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;


import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class RunCountStations {


    static Logger logger = Logger.getLogger(RunCountStations.class);

    public static void main(String[] args) {

        //read matsim network
        Network network = NetworkUtils.readNetwork(args[0]);


        TravelTime travelTime = new DistanceAsTravelTime();
        TravelDisutility travelDisutility = new DistanceAsTravelDisutility();

        DijkstraTree leastCostPathTree =
                new DijkstraTree(network, travelDisutility, travelTime);

        //read the station list
        Map<String, Station> stationMap = new HashMap<String, Station>();

        Coord stationCoordinates = new Coord(4363370.788086067, 5515530.34227314);
        Station station1 = new Station("9041", stationCoordinates, "Würzburg", "Nürnberg", Station.RoadType.BUNDESTRASSE);
        stationMap.put(station1.getId(), station1);


        Coord station2Coordinates = new Coord(4374942.157910948, 5517540.483802367);
        Station station2 = new Station("9027", station2Coordinates, "Würzburg", "Nürnberg", Station.RoadType.AUTOBAHN);
        stationMap.put(station2.getId(), station2);

        //Read the list of destination Coordinates
        Map<String, Coord> destinationCoordinateMap = new HashMap<String, Coord>();
        destinationCoordinateMap.put("Nürnberg", new Coord(4433245.3, 5479893.5));
        destinationCoordinateMap.put("Würzburg", new Coord(4351259, 5519116));

        for (Station station : stationMap.values()) {
            findNetworkLinksForThisStation(station, network, destinationCoordinateMap, leastCostPathTree);
        }

    }

    /**
     * Finds the links of the network that correspond to the directions 1 and 2 of the station. Assigns this values
     * to the station
     *
     * @param station
     * @param network
     */
    private static void findNetworkLinksForThisStation(Station station, Network network, Map<String, Coord> destinationCoordinateMap, DijkstraTree leastCostPathTree) {


        if (station.getRoadType().equals(Station.RoadType.BUNDESTRASSE)) {

            Link linkOne = NetworkUtils.getNearestLink(network, station.getCoordinates());
            Link linkTwo = NetworkUtils.findLinkInOppositeDirection(linkOne);

            Node destination1 = NetworkUtils.getNearestNode(network, destinationCoordinateMap.get(station.getDestination1()));
            //Node destination2 = NetworkUtils.getNearestNode(network, destinationCoordinateMap.get(station.getDestination2()));
            leastCostPathTree.calcLeastCostPathTree(destination1, 0);

            double distanceBetweenLinkOneAndDestination1 = leastCostPathTree.getTime(linkOne.getToNode());
            double distanceBetweenLinkTwoAndDestination1 = leastCostPathTree.getTime(linkTwo.getToNode());

            if (distanceBetweenLinkOneAndDestination1 < distanceBetweenLinkTwoAndDestination1) {
                station.setLinkToDestination1(linkOne);
                station.setLinkToDestination2(linkTwo);
            } else {
                station.setLinkToDestination1(linkTwo);
                station.setLinkToDestination2(linkOne);
            }

        } else {
            double buffer = 500;

            Node destination1 = NetworkUtils.getNearestNode(network, destinationCoordinateMap.get(station.getDestination1()));
            leastCostPathTree.calcLeastCostPathTree(destination1, 0);


            double max = 0;
            Link maxLink = null;
            double min = Double.MAX_VALUE;
            Link minLink = null;

            for (Node node : network.getNodes().values()) {

                if (NetworkUtils.getEuclideanDistance(station.getCoordinates(), node.getCoord()) < buffer) {
                    if (node.getInLinks().size() == 1 && node.getOutLinks().size() == 1) {
                        Link inLink = node.getInLinks().values().iterator().next();
                        double distance = leastCostPathTree.getTime(inLink.getToNode());
                        if (distance < min){
                            min = distance;
                            minLink = inLink;
                        }

                        if (distance > max ){
                            max = distance;;
                            maxLink = inLink;
                        }

                    } else {
                        logger.warn("Careful");
                    }

                }




            }

            logger.info(maxLink);
            logger.info(minLink);

            station.setLinkToDestination1(maxLink);
            station.setLinkToDestination2(minLink);

            //todo check that the links belong to two different motorway directions (they are not connected with each other)

        }

    }


}
