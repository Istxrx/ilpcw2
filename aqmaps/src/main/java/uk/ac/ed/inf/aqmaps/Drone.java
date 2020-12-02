package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Represents a drone that visits and collects readings from sensors. 
 */
public class Drone {

    // Constants for movement of the drone
    private static final int MAX_MOVE_COUNT = 150;
    private static final double MOVE_LENGTH = 0.0003;
    private static final double READING_RANGE = 0.0002;
    private static final double START_LOCATION_RANGE = 0.0001;
    private static final int DIRECTION_STEP = 10;
    private static final ArrayList<Integer> ALLOWED_MOVE_DIRECTIONS;
    static {
        ALLOWED_MOVE_DIRECTIONS = new ArrayList<Integer>();
        var direction = 0;

        while (direction < 360) {
            ALLOWED_MOVE_DIRECTIONS.add(direction);
            direction += DIRECTION_STEP;
        }
    }

    private Point position;
    private int moveCount;
    private Path flightPath;
    private String flightPathLog;
    private ArrayList<Polygon> noFlyZones;
    private ArrayList<AirQualitySensor> sensors;
    private ArrayList<AirQualitySensor> visitedSensors;

    public Drone(Point startPosition, ArrayList<Polygon> noFlyZones,
            ArrayList<AirQualitySensor> sensors) {

        this.position = startPosition;
        this.moveCount = 0;
        this.flightPath = new Path(startPosition);
        this.flightPathLog = "";
        this.noFlyZones = noFlyZones;
        this.sensors = sensors;
        this.visitedSensors = new ArrayList<AirQualitySensor>();
    }
    
    /**
     * @return the number of moves this drone has done so far
     */
    public int getMoveCount() {
        return this.moveCount;
    }

    /**
     * @return the flight path loq that describes the actions of this drone so far
     */
    public String getFlightPathLog() {
        return this.flightPathLog;
    }
    
    /**
     * @return line string feature representing the current flight path
     */
    public Feature getFlightPathAsFeature() {
        return this.flightPath.toFeature();
    }

    /**
     * @return ArrayList of features representing sensors and their reading values
     */
    public ArrayList<Feature> getReadingsAsFeatures() {
        var features = new ArrayList<Feature>();

        for (AirQualitySensor sensor : this.sensors) {
            var feature = sensor.toFeature(this.visitedSensors.contains(sensor));
            features.add(feature);
        }
        return features;
    }

    /**
     * Reads a sensor within a reading range and marks it as visited.
     *
     * @param sensor the sensor that is to be read
     * @return true if read is successful, false otherwise
     */
    private boolean readSensor(AirQualitySensor sensor) {
        
        // Check if this drone is in the reading range of the sensor
        if (Utils2D.distance(this.position, sensor.getLocationAsPoint()) < READING_RANGE) {
            // Simulates obtaining the reading data by adding the sensor as a whole object together
            // including the reading value inside to a list of visited sensors. Assumes the value
            // was not accessible up to this point
            this.flightPathLog += "," + sensor.getLocation() + "\n";
            this.visitedSensors.add(sensor);
            
            if (!sensor.hasLowBattery()) {
                sensor.getReading();
            }
            return true;
        }
        return false;
    }

    /**
     * Moves the drone in a given direction and updates the location of the drone correspondingly.
     *
     * @param direction the direction of the move, must be in the allowed set of move directions the
     *                  drone can go
     * @return true if the drone can make the move, false otherwise
     */
    private boolean move(int direction) {

        if (!ALLOWED_MOVE_DIRECTIONS.contains(direction)) {
            return false;
        }
        if (this.moveCount < MAX_MOVE_COUNT) {
            // Commits to making the move, updates log and position
            this.moveCount += 1;
            this.flightPathLog += this.moveCount + "," + this.position.longitude() + ","
                    + this.position.latitude() + "," + direction;
            this.position = Utils2D.movePoint(this.position, MOVE_LENGTH, direction);
            this.flightPathLog += "," + this.position.longitude() + "," + this.position.latitude();
            this.flightPath.addMove(this.position, direction);
            return true;
        }
        return false;
    }

    /**
     * Executes a sequence of moves to travel exactly on a given path.
     *
     * @param path the path that contains the list of direction in which the drone should move
     * @return true if every move in the sequence was successful, false otherwise
     */
    private boolean move(Path path) {

        if (path == null) {
            return false;
        }

        int i = 0;
        for (Integer direction : path.getMoveDirections()) {
            if (!this.move(direction)) {
                return false;
            }
            // Only the last move of the path will reach a sensor
            if (i < path.getMoveDirections().size() - 1) {
                this.flightPathLog += "," + null + "\n";
            }
            i++;
        }
        return true;
    }

    /**
     * Moves the drone to a target point within a given range.
     *
     * @param target the point that the drone should reach
     * @param range  the range around the target within which the drone should end
     * @return true if the drone reaches the target, false otherwise
     */
    private boolean moveToPoint(Point target, double range) {

        var path = Path.findPathToPoint(this.position, target, range, MOVE_LENGTH,
                ALLOWED_MOVE_DIRECTIONS, this.noFlyZones);
        return this.move(path);
    }

    /**
     * Moves the drone to the sensors location to make a reading.
     *
     * @param sensor the sensor that the drone should visit and read
     * @return true if the sensor was reached and read, false otherwise
     */
    private boolean visitSensor(AirQualitySensor sensor) {
        
        // if the drone successfully moves to the sensor it can read it
        if (this.moveToPoint(sensor.getLocationAsPoint(), READING_RANGE)) {
            return this.readSensor(sensor);
        }
        return false;
    }

    /**
     * Moves the drone close to its starting position.
     *
     * @return true if the drone reaches the starting position, false otherwise
     */
    private boolean returnToStartPosition() {

        if (this.moveToPoint(this.flightPath.getStartPoint(), START_LOCATION_RANGE)) {
            this.flightPathLog += "," + null + "\n";
            return true;
        }
        return false;
    }

    /**
     * Tries to visit and collect readings from all the sensors.The order in which the sensors are
     * visited is optimized to possibly achieve lower move count. After that the drone tries to
     * return back to the starting position.
     */
    public void executeReadingRoutine() {

        var points = AirQualitySensor.toPoints(sensors);
        // Adding the start position of the drone to the beginning of the list makes it the start
        // of the visit route produced by Graph class
        points.add(0, this.position);

        // Determine optimized order in which to visit the sensors based on their straight line
        // distances
        var graph = new Graph(points);
        graph.toGreedyOrder();
        graph.swapOptimizeOrder(30);
        var visitOrder = graph.getVisitOrder();
        
        // Visit sensors in order determined by Graph class
        for (int i = 1; i < visitOrder.length; i++) {
            this.visitSensor(this.sensors.get(visitOrder[i] - 1));
        }
        // Complete the cycle by returning back to starting area
        this.returnToStartPosition();
    }

}
