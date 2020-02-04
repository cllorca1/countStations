import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

public class Station {


    private final String id;
    private final Coord coordinates;
    private final String destination1;
    private final String destination2;
    private final RoadType roadType;
    private Link linkToDestination1;
    private Link linkToDestination2;

    enum RoadType{
        AUTOBAHN, BUNDESTRASSE;
    }

    public Station(String id, Coord coordinates, String desitnation1, String destination2, RoadType roadType) {
        this.id = id;
        this.coordinates = coordinates;
        this.destination1 = desitnation1;
        this.destination2 = destination2;
        this.roadType = roadType;
    }

    public RoadType getRoadType() {
        return roadType;
    }

    public void setLinkToDestination1(Link linkToDestination1) {
        this.linkToDestination1 = linkToDestination1;
    }

    public void setLinkToDestination2(Link linkToDestination2) {
        this.linkToDestination2 = linkToDestination2;
    }


    public String getId() {
        return id;
    }

    public Coord getCoordinates() {
        return coordinates;
    }

    public String getDestination1() {
        return destination1;
    }

    public String getDestination2() {
        return destination2;
    }

    public Link getLinkToDestination1() {
        return linkToDestination1;
    }

    public Link getLinkToDestination2() {
        return linkToDestination2;
    }
}
