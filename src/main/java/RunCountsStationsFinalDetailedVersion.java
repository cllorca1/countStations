/*
 * Created by Carlos Llorca and Kamil Moreau on 2/18/2020.
 */

import javafx.util.Pair;
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

import java.io.IOException;
import java.util.Map;


public class RunCountsStationsFinalDetailedVersion {

    final static int bufferSimpleNetworkAutobahn = 4000;
    final static int bufferDetailedNetworkAutobahn = 300;
    final static int bufferSimpleNetworkAusland = 600;

    static int stationsProcessed = 0;
    static int totalStations = 0;
    static int bdStationsProcessed = 0;
    static int autoBahnStationsProcessed = 0;
    static int auslandStationsProcessed = 0;
    static int noParentsFound = 0;
    static int noNeighborNodes = 0;
    static int noDestination1ButDestination2 = 0;
    static int noDestination1NorDestination2 = 0;
    static int noOppositeLink = 0;
    static int noMinLink = 0;
    static int noMaxLink = 0;
    static int noMinLinkNorMaxLink = 0;
    static int totalProcessed = 0;

    static Logger logger = Logger.getLogger(RunCountStations.class);

    public static void main(String[] args) throws IOException {

        /*
        args[0]: simpleNetwork
        args[1]: detailedNetwork
        args[2]: stationsFile
        args[3]: destinationsFile
        */

        //read matsim network
        logger.info("Reading the simple network");
        Network simpleNetwork = NetworkUtils.readNetwork(args[0]);
        logger.info("Reading the detailed network");
        Network detailedNetwork = NetworkUtils.readNetwork(args[1]);


        TravelTime travelTime = new DistanceAsTravelTime();
        TravelDisutility travelDisutility = new DistanceAsTravelDisutility();

        DijkstraTree leastCostPathTreeDetailedNetwork =
                new DijkstraTree(detailedNetwork, travelDisutility, travelTime);

        DijkstraTree leastCostPathTreeSimpleNetwork =
                new DijkstraTree(simpleNetwork, travelDisutility, travelTime);

        //read the station list
        StationsReader stationsReader = new StationsReader(args[2]);
        //./bast_coord_dest_DHDN_GK3_AUSLAND.csv
        DestinationsReader destinationsReader = new DestinationsReader(args[3]);
        //./kreise_zentren_DHDN_GK3.csv


        StationsWriterWithLinks stationsWriter = new StationsWriterWithLinks("./stationsWithMatsimLinks.csv");
        String stationsWriterColumnNames = "stationID" + ";"
                + "roadType" + ";"
                + "xCoord" + ";"
                + "yCoord" + ";"
                + "dest1" + ";"
                + "linkIDDest1" + ";"
                + "osmIDlinkDest1" + ";"
                + "dest2" + ";"
                + "linkIDDest2" + ";"
                + "osmIDlinkDest2";
        stationsWriter.addLineToOutputBW(stationsWriterColumnNames);

        for (Station station : stationsReader.getStationsMap().values()) {
            totalStations++;
            findNetworkLinksForThisStation(station, simpleNetwork, detailedNetwork, destinationsReader.getDestinationsCoordinatesMap(), leastCostPathTreeDetailedNetwork, leastCostPathTreeSimpleNetwork);
            String stationInfos;
            String linkIDDestination1;
            String linkIDDestination2;
            String osmIDlinkDest1;
            String osmIDlinkDest2;

            if (station.getLinkToDestination1() != null){
                linkIDDestination1 = station.getLinkToDestination1().getId().toString();
                osmIDlinkDest1 = station.getLinkToDestination1().getAttributes().getAsMap().get("origid").toString();
            }
            else{
                linkIDDestination1 = "";
                osmIDlinkDest1 = "";
            }

            if (station.getLinkToDestination2() != null){
                linkIDDestination2 = station.getLinkToDestination2().getId().toString();
                osmIDlinkDest2 = station.getLinkToDestination2().getAttributes().getAsMap().get("origid").toString();
            }
            else{
                linkIDDestination2 = "";
                osmIDlinkDest2 = "";
            }
            stationInfos = station.getId() + ";"
                    + station.getRoadType() + ";"
                    + station.getCoordinates().getX() + ";"
                    + station.getCoordinates().getY() + ";"
                    + station.getDestination1() + ";"
                    + linkIDDestination1 + ";"
                    + osmIDlinkDest1 + ";"
                    + station.getDestination2() + ";"
                    + linkIDDestination2 + ";"
                    + osmIDlinkDest2 ;

            stationsWriter.addLineToOutputBW(stationInfos);

        }
        logger.info("Total stations handled: " + totalStations);
        logger.info("Stations with both links: " + stationsProcessed);
        logger.info("... of which Bundesstraßen: " + bdStationsProcessed);
        logger.info("... of which Autobahnen: " + autoBahnStationsProcessed);
        logger.info("......... of which border stations: " + auslandStationsProcessed);
        logger.info("No destination 1 but destination 2: " + noDestination1ButDestination2);
        logger.info("No destination 1 nor destination 2: " + noDestination1NorDestination2);
        logger.info("No minLink: " + noMinLink);
        logger.info("No maxLink: " + noMaxLink);
        logger.info("No minLink nor maxLink: " + noMinLinkNorMaxLink);
        logger.info("No neighboring nodes in the detailed network: " + noNeighborNodes);
        logger.info("No parents found in the simple network: " + noParentsFound);
        logger.info("No opposite link on the Bundesstraße: " + noOppositeLink);

        stationsWriter.closeOutputBW();

    }


    //Finds the links of the network that correspond to the directions 1 and 2 of the station. Assigns these values to the station.
    private static void findNetworkLinksForThisStation(Station station, Network simpleNetwork, Network detailedNetwork, Map<String, Coord> destinationCoordinateMap, DijkstraTree leastCostPathTreeDetailedNetwork, DijkstraTree leastCostPathTreeSimpleNetwork) {

        logger.info("processed station:" + station.getId());

        if (destinationCoordinateMap.get(station.getDestination1()) != null) {
            blockOne(station, simpleNetwork, detailedNetwork, destinationCoordinateMap, leastCostPathTreeDetailedNetwork, leastCostPathTreeSimpleNetwork);
        } else if (destinationCoordinateMap.get(station.getDestination2()) != null) {
            noDestination1ButDestination2++;
            block2(station, simpleNetwork, detailedNetwork, destinationCoordinateMap, leastCostPathTreeDetailedNetwork, leastCostPathTreeSimpleNetwork);
        } else{
            logger.info("Station " + station.getId() + ": no matching destination");
            noDestination1NorDestination2++;
        }
        totalProcessed++;
        if (stationsProcessed%20 == 0) {
            System.out.println("Total stations already handled: " + totalProcessed);
            System.out.println("Stations with both links: " + stationsProcessed);
            System.out.println("... of which Bundesstraßen: " + bdStationsProcessed);
            System.out.println("... of which Autobahnen: " + autoBahnStationsProcessed);
        }
    }

    private static void blockOne(Station station, Network simpleNetwork, Network detailedNetwork, Map<String, Coord> destinationCoordinateMap, DijkstraTree leastCostPathTreeDetailedNetwork, DijkstraTree leastCostPathTreeSimpleNetwork) {
        //FIRST CASE: The station is located on a Bundesstraße
        if (station.getRoadType().equals(Station.RoadType.BUNDESSTRASSE)) {

            Link linkOne = NetworkUtils.getNearestLink(simpleNetwork, station.getCoordinates());
            Link linkTwo = NetworkUtils.findLinkInOppositeDirection(linkOne);
            if(linkTwo == null){
                logger.info("B-Station " + station.getId() + ": no opposite link");
                noOppositeLink++;
                return;
            }

            Node destination1 = NetworkUtils.getNearestNode(simpleNetwork, destinationCoordinateMap.get(station.getDestination1()));
            leastCostPathTreeSimpleNetwork.calcLeastCostPathTree(destination1, 0);

            double distanceBetweenLinkOneAndDestination1 = leastCostPathTreeSimpleNetwork.getTime(linkOne.getToNode());
            double distanceBetweenLinkTwoAndDestination1 = leastCostPathTreeSimpleNetwork.getTime(linkTwo.getToNode());

            if (distanceBetweenLinkOneAndDestination1 < distanceBetweenLinkTwoAndDestination1) {
                station.setLinkToDestination1(linkOne);
                station.setLinkToDestination2(linkTwo);
            } else {
                station.setLinkToDestination1(linkTwo);
                station.setLinkToDestination2(linkOne);
            }
            stationsProcessed++;
            bdStationsProcessed++;
            logger.info("B-Station " + station.getId() + ": it worked!");
        }

        //SECOND CASE: The station is located on an Autobahn
        else {

            //FIRST SUB-CASE: The station is a border station
            if(station.getDestination1().contains("AUSLAND")) {
                Node destination1 = NetworkUtils.getNearestNode(simpleNetwork, destinationCoordinateMap.get(station.getDestination1()));
                leastCostPathTreeSimpleNetwork.calcLeastCostPathTree(destination1, 0);
                if (NetworkUtils.getNearestNodes(simpleNetwork, station.getCoordinates(), bufferSimpleNetworkAusland).size() != 0) {
                    Link[] minMaxLinks;
                    minMaxLinks = getMinAndMaxLinksForAStation(simpleNetwork, station, leastCostPathTreeSimpleNetwork, bufferSimpleNetworkAusland);

                    if (minMaxLinks[0] == null) {
                        if (minMaxLinks[1] == null) {
                            noMinLinkNorMaxLink++;
                        }
                        else{
                            noMinLink++;
                        }
                    }
                    else{
                        if (minMaxLinks[1] == null) {
                            noMaxLink++;
                        } else {
                            station.setLinkToDestination1(minMaxLinks[1]);
                            station.setLinkToDestination2(minMaxLinks[0]);
                            auslandStationsProcessed++;
                            stationsProcessed++;
                            autoBahnStationsProcessed++;
                            logger.info("Border A-Station " + station.getId() + ": it worked!");
                        }
                    }
                }
            }

            //SECOND SUB-CASE: The station is not a border station
            else {
                Node destination1 = NetworkUtils.getNearestNode(detailedNetwork, destinationCoordinateMap.get(station.getDestination1()));
                leastCostPathTreeDetailedNetwork.calcLeastCostPathTree(destination1, 0);
                if (NetworkUtils.getNearestNodes(detailedNetwork, station.getCoordinates(), bufferDetailedNetworkAutobahn).size() != 0) {
                    Link[] minMaxLinks;
                    minMaxLinks = getMinAndMaxLinksForAStation(detailedNetwork, station, leastCostPathTreeDetailedNetwork, bufferDetailedNetworkAutobahn);

                    if (minMaxLinks[0] == null) {
                        if (minMaxLinks[1] == null) {
                            noMinLinkNorMaxLink++;
                        }
                        else{
                            noMinLink++;
                        }
                    }
                    else{
                        if (minMaxLinks[1] == null) {
                            noMaxLink++;
                        }
                        else{

                            //Now we must find the corresponding links in the simpleNetwork
                            Pair<Link[], boolean[]> updatedLinksOrNot = getLinksFromSimpleNetwork(minMaxLinks, simpleNetwork, station, bufferSimpleNetworkAutobahn);


                            if (!((updatedLinksOrNot.getValue())[1]) || !((updatedLinksOrNot.getValue())[0])) {
                                noParentsFound++;
                                logger.info("A-Station " + station.getId() + ": no corresponding links found in the simple network");

                            } else {
                                station.setLinkToDestination1((updatedLinksOrNot.getKey())[1]);
                                station.setLinkToDestination2((updatedLinksOrNot.getKey())[0]);
                                stationsProcessed++;
                                autoBahnStationsProcessed++;
                                logger.info("A-Station " + station.getId() + ": it worked!");
                            }
                        }
                    }

                } else {
                    noNeighborNodes++;
                    logger.info("A-Station " + station.getId() + ": unable to set the links - no neighbor nodes");
                }
            }
        }
    }

    private static void block2(Station station, Network simpleNetwork, Network detailedNetwork, Map<String, Coord> destinationCoordinateMap, DijkstraTree leastCostPathTreeDetailedNetwork, DijkstraTree leastCostPathTreeSimpleNetwork) {
        //FIRST CASE: The station is located on a Bundesstraße
        if (station.getRoadType().equals(Station.RoadType.BUNDESSTRASSE)) {

            Link linkOne = NetworkUtils.getNearestLink(simpleNetwork, station.getCoordinates());
            Link linkTwo = NetworkUtils.findLinkInOppositeDirection(linkOne);

            if(linkTwo == null){
                logger.info("B-Station " + station.getId() + ": no opposite link on the Bundesstraße");
                noOppositeLink++;
                return;
            }

            Node destination2 = NetworkUtils.getNearestNode(simpleNetwork, destinationCoordinateMap.get(station.getDestination2()));
            leastCostPathTreeSimpleNetwork.calcLeastCostPathTree(destination2, 0);

            double distanceBetweenLinkOneAndDestination2 = leastCostPathTreeSimpleNetwork.getTime(linkOne.getToNode());
            double distanceBetweenLinkTwoAndDestination2 = leastCostPathTreeSimpleNetwork.getTime(linkTwo.getToNode());

            if (distanceBetweenLinkOneAndDestination2 < distanceBetweenLinkTwoAndDestination2) {
                station.setLinkToDestination2(linkOne);
                station.setLinkToDestination1(linkTwo);
            } else {
                station.setLinkToDestination2(linkTwo);
                station.setLinkToDestination1(linkOne);
            }
            stationsProcessed++;
            bdStationsProcessed++;
            logger.info("B-Station " + station.getId() + ": it worked!");

        }

        //SECOND CASE: The station is located on an Autobahn
        else {

            //FIRST SUB-CASE: The station is a border station
            if(station.getDestination2().contains("AUSLAND")) {
                if (NetworkUtils.getNearestNodes(simpleNetwork, station.getCoordinates(), bufferSimpleNetworkAusland).size() != 0) {
                    Link[] minMaxLinks;
                    Node destination2 = NetworkUtils.getNearestNode(simpleNetwork, destinationCoordinateMap.get(station.getDestination2()));
                    leastCostPathTreeSimpleNetwork.calcLeastCostPathTree(destination2, 0);
                    minMaxLinks = getMinAndMaxLinksForAStation(simpleNetwork, station, leastCostPathTreeSimpleNetwork, bufferSimpleNetworkAusland);

                    if (minMaxLinks[0] == null) {
                        if (minMaxLinks[1] == null) {
                            noMinLinkNorMaxLink++;
                        }
                        else{
                            noMinLink++;
                        }
                    }
                    else{
                        if (minMaxLinks[1] == null) {
                            noMaxLink++;
                        } else {
                            station.setLinkToDestination2(minMaxLinks[1]);
                            station.setLinkToDestination1(minMaxLinks[0]);
                            auslandStationsProcessed++;
                            stationsProcessed++;
                            autoBahnStationsProcessed++;
                            logger.info("Border A-Station " + station.getId() + ": it worked!");
                        }
                    }
                }
            }

            //SECOND SUB-CASE: The station is not a border station
            else {

                Node destination2 = NetworkUtils.getNearestNode(detailedNetwork, destinationCoordinateMap.get(station.getDestination2()));
                leastCostPathTreeDetailedNetwork.calcLeastCostPathTree(destination2, 0);

                if (NetworkUtils.getNearestNodes(detailedNetwork, station.getCoordinates(), bufferDetailedNetworkAutobahn).size() != 0) {
                    Link[] minMaxLinks;
                    minMaxLinks = getMinAndMaxLinksForAStation(detailedNetwork, station, leastCostPathTreeDetailedNetwork, bufferDetailedNetworkAutobahn);

                    if (minMaxLinks[0] == null) {
                        if (minMaxLinks[1] == null) {
                            noMinLinkNorMaxLink++;
                        }
                        else{
                            noMinLink++;
                        }
                    }
                    else{
                        if (minMaxLinks[1] == null) {
                            noMaxLink++;
                        }
                    }

                    //now we must find the corresponding links in the simpleNetwork

                    Pair<Link[], boolean[]> updatedLinksOrNot = getLinksFromSimpleNetwork(minMaxLinks, simpleNetwork, station, bufferSimpleNetworkAutobahn);

                    if (!((updatedLinksOrNot.getValue())[1]) || !((updatedLinksOrNot.getValue())[0])) {
                        logger.info("A-Station " + station.getId() + ": no corresponding links found in the simple network");
                        noParentsFound++;
                    } else {
                        station.setLinkToDestination2((updatedLinksOrNot.getKey())[1]);
                        station.setLinkToDestination1((updatedLinksOrNot.getKey())[0]);
                        stationsProcessed++;
                        autoBahnStationsProcessed++;
                        logger.info("A-Station " + station.getId() + ": it worked!");
                    }
                } else {
                    logger.info("A-Station " + station.getId() + ": unable to set the links - no neighbor nodes");
                    noNeighborNodes++;
                }
            }
        }
    }

    //This method returns an array [minLink, maxLink]
    public static Link[] getMinAndMaxLinksForAStation(Network network, Station station, DijkstraTree leastCostPathTreeNetwork, double bufferNetwork){
        double max = 0;
        Link maxLink = null;
        double min = Double.MAX_VALUE;
        Link minLink = null;

        for (Node node : NetworkUtils.getNearestNodes(network, station.getCoordinates(), bufferNetwork)) {
            if (node.getInLinks().size() == 1 && node.getOutLinks().size() == 1) {
                Link inLink = node.getInLinks().values().iterator().next();
                if (inLink.getAttributes().getAsMap().get("type").equals("motorway")) {
                    double distance = leastCostPathTreeNetwork.getTime(inLink.getToNode());
                    if (distance < min && (inLink.getAttributes().getAsMap().containsKey("origid"))){
                        if(minLink != null) {
                            if (!(inLink.getAttributes().getAsMap().get("origid").equals(
                                    minLink.getAttributes().getAsMap().get("origid")))) {
                                min = distance;
                                minLink = inLink;
                            }
                        }
                        else{
                            min = distance;
                            minLink = inLink;
                        }
                        }

                    else if (distance > max && (inLink.getAttributes().getAsMap().containsKey("origid"))){
                        if(maxLink != null) {
                            if (!(inLink.getAttributes().getAsMap().get("origid").equals(
                                    maxLink.getAttributes().getAsMap().get("origid")))) {
                                max = distance;
                                maxLink = inLink;
                            }
                        }
                        else{
                            max = distance;
                            maxLink = inLink;
                        }
                    }
                }
            }
        }

        Link[] linksArray = new Link[2];
        linksArray[0] = minLink;
        linksArray[1] = maxLink;
        return linksArray;
    }

    //This method returns a Pair<Link[], boolean[]>(minMaxLinks, updatedLinks)
    public static Pair<Link[], boolean[]> getLinksFromSimpleNetwork(Link[] minMaxLinks, Network simpleNetwork, Station station, double bufferSimpleNetwork){
        boolean minLinkUpdated = false;
        boolean maxLinkUpdated = false;
        for (Node node : NetworkUtils.getNearestNodes(simpleNetwork, station.getCoordinates(), bufferSimpleNetwork)) {
            if (node.getInLinks().size() == 1 && node.getOutLinks().size() == 1) {
                Link inLink = node.getInLinks().values().iterator().next();
                Link outLink = node.getOutLinks().values().iterator().next();

                //Check the inLink
                if ((minMaxLinks[0] != null) &&
                        (inLink.getAttributes().getAsMap().containsKey("origid")) &&
                        inLink.getAttributes().getAsMap().get("origid").equals(
                                minMaxLinks[0].getAttributes().getAsMap().get("origid"))) {
                    minMaxLinks[0] = inLink;
                    minLinkUpdated = true;
                } else if ((minMaxLinks[1] != null) &&
                        (inLink.getAttributes().getAsMap().containsKey("origid")) &&
                        inLink.getAttributes().getAsMap().get("origid").equals(
                                minMaxLinks[1].getAttributes().getAsMap().get("origid"))) {
                    minMaxLinks[1] = inLink;
                    maxLinkUpdated = true;
                }

                //Check the outLink
                else if ((minMaxLinks[0] != null) &&
                        (outLink.getAttributes().getAsMap().containsKey("origid")) &&
                        outLink.getAttributes().getAsMap().get("origid").equals(
                                minMaxLinks[0].getAttributes().getAsMap().get("origid"))) {
                    minMaxLinks[0] = outLink;
                    minLinkUpdated = true;

                } else if ((minMaxLinks[1] != null) &&
                        (outLink.getAttributes().getAsMap().containsKey("origid")) &&
                        outLink.getAttributes().getAsMap().get("origid").equals(
                                minMaxLinks[1].getAttributes().getAsMap().get("origid"))) {
                    minMaxLinks[1] = outLink;
                    maxLinkUpdated = true;
                }
            }
        }

        boolean[] updatedLinks = new boolean[2];
        updatedLinks[0] = minLinkUpdated;
        updatedLinks[1] = maxLinkUpdated;
        return new Pair<>(minMaxLinks, updatedLinks);
    }
}