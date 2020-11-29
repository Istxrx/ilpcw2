package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {

    private Point position;
    private int moveCount;
    private Path flightPath;
    private String flightPathLog;
    private ArrayList<Polygon> noFlyZones;
    private ArrayList<AirQualitySensor> sensors;
    private ArrayList<AirQualitySensor> visitedSensors;

    private static final int MAX_MOVE_COUNT = 150;
    private static final double MOVE_LENGTH = 0.0003;
    private static final double READING_RANGE = 0.0002;
    private static final int DIRECTION_STEP = 10;
    private static final ArrayList<Integer> ALLOWED_DIRECTIONS;
    static {
        ALLOWED_DIRECTIONS = new ArrayList<Integer>();
        var direction = 0;

        while (direction < 360) {
            ALLOWED_DIRECTIONS.add(direction);
            direction += DIRECTION_STEP;
        }
    }

    public Drone(Point position, ArrayList<Polygon> noFlyZones,
            ArrayList<AirQualitySensor> sensors) {

        this.position = position;
        this.moveCount = 0;
        this.flightPath = new Path(position);
        this.flightPathLog = "";
        this.noFlyZones = noFlyZones;
        this.sensors = sensors;
        this.visitedSensors = new ArrayList<AirQualitySensor>();
    }
    public int getMoveCount() {
        return this.moveCount;
    }

    public String getFlightPathLog() {
        return this.flightPathLog;
    }

    public Feature getFlightPathAsFeature() {
        return this.flightPath.toFeature();
    }

    public ArrayList<Feature> getReadingsAsFeatures() {
        var features = new ArrayList<Feature>();

        for (AirQualitySensor sensor : this.sensors) {
            var feature = sensor.toFeature(this.visitedSensors.contains(sensor));
            features.add(feature);
        }
        return features;
    }

    public boolean readSensor(AirQualitySensor sensor) {

        if (Utils2D.distance(this.position, sensor.getLocationAsPoint()) < READING_RANGE) {
            this.flightPathLog = this.flightPathLog + "," + sensor.getLocation() + "\n";
            this.visitedSensors.add(sensor);
            return true;
        }
        return false;
    }

    public boolean move(int direction) {
        
        if (!ALLOWED_DIRECTIONS.contains(direction)) {
            return false;
        }
        if (this.moveCount < MAX_MOVE_COUNT) {
            this.moveCount += 1;
            this.flightPathLog = this.flightPathLog + this.moveCount + ","
                    + this.position.longitude() + "," + this.position.latitude() + "," + direction;
            this.position = Utils2D.movePoint(this.position, MOVE_LENGTH, direction);
            this.flightPathLog = this.flightPathLog + "," + this.position.longitude() + ","
                    + this.position.latitude();
            this.flightPath.addMove(this.position, direction);
            return true;
        }
        return false;
    }

    public boolean move(Path path) {

        if (path == null) {
            return false;
        }
        int i = 0;
        for (Integer direction : path.getMoveDirections()) {
            if (!this.move(direction)) {
                return false;
            }
            if (i < path.getMoveDirections().size() - 1) {
                this.flightPathLog = this.flightPathLog + "," + null + "\n";
            }
            i++;
        }
        return true;
    }

    public boolean moveToPoint(Point target, double range) {

        var path = Path.findPathToPoint(this.position, target, range, MOVE_LENGTH,
                ALLOWED_DIRECTIONS, this.noFlyZones);

        return this.move(path);

    }

    public boolean moveToSensor(AirQualitySensor sensor) {

        if (this.moveToPoint(sensor.getLocationAsPoint(), READING_RANGE)) {
            return this.readSensor(sensor);
        }
        return false;

    }

    public boolean returnToStartPosition() {
        
        if (this.moveToPoint(this.flightPath.getStartPoint(), 0.0001)) {
            this.flightPathLog = this.flightPathLog + "," + null + "\n";
            return true;
        }
        return false;
    }

    public void initiateRoutine() {
        
        var points = AirQualitySensor.toPoints(sensors);
        points.add(0, this.position);
        var graph = new Graph(points);
        graph.greedyOrder();
        graph.swapOptimizeOrder(30);
        var visitOrder = graph.getVisitOrder();

        for (int i = 1; i < visitOrder.length; i++) {
            this.moveToSensor(this.sensors.get(visitOrder[i] - 1));
        }
        this.returnToStartPosition();
    }

}
