package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {
    
    private Point position;
    private int moveCount;
    private Path flightPath;
    private String flightPathLog;
    private ArrayList<Polygon> noFlyZones;
    private ArrayList<AirQualitySensor> sensors;
    private ArrayList<AirQualitySensor> readSensors;
    private ArrayList<AirQualitySensor> lowBatterySensors;
    
    private static final int MAX_MOVE_COUNT = 150;
    private static final int DIRECTION_STEP = 10;
    private static final ArrayList<Integer> POSSIBLE_DIRECTIONS;
    static {
        POSSIBLE_DIRECTIONS = new ArrayList<Integer>();
        var direction = 0;
        
        while (direction < 360) {
            POSSIBLE_DIRECTIONS.add(direction);
            direction += DIRECTION_STEP;
        } 
    }
    private static final double MOVE_LENGTH = 0.0003;
    private static final double READING_RANGE = 0.0002;
    
    public Drone (Point position, 
            ArrayList<Polygon> noFlyZones, 
            ArrayList<AirQualitySensor> sensors) {
        
        this.position = position;
        this.moveCount = 0;
        this.flightPath = new Path(position);
        this.flightPathLog = "";
        this.noFlyZones = noFlyZones;
        this.sensors = sensors;
        this.readSensors = new ArrayList<AirQualitySensor>();
        this.lowBatterySensors = new ArrayList<AirQualitySensor>();
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
            var feature = Feature.fromGeometry((Geometry) sensor.getLocationAsPoint());
            
            feature.addStringProperty("marker-size", "medium");
            feature.addStringProperty("location",sensor.getLocation());
            
            if (this.readSensors.contains(sensor)) {
                feature.addStringProperty("rgb-string", App.pollutionColor(sensor.getReading()));
                feature.addStringProperty("marker-color", App.pollutionColor(sensor.getReading()));
                feature.addStringProperty("marker-symbol", App.pollutionSymbol(sensor.getReading()));
            } else if (this.lowBatterySensors.contains(sensor)) {
                feature.addStringProperty("rgb-string", "#000000");
                feature.addStringProperty("marker-color", "#000000");
                feature.addStringProperty("marker-symbol", "cross");
            } else {
                feature.addStringProperty("rgb-string", "#aaaaaa");
                feature.addStringProperty("marker-color", "#aaaaaa");
            }
            features.add(feature);
        }
        
        return features;
    }
    
    public boolean readSensor(AirQualitySensor sensor) {
        
        if (Utils2D.distance(this.position, sensor.getLocationAsPoint()) < READING_RANGE) {
            this.flightPathLog = this.flightPathLog + "," + sensor.getLocation() + "\n";
            if (sensor.getBattery() < 10) {
                this.lowBatterySensors.add(sensor);
            } else {
                this.readSensors.add(sensor);
            }
            return true;
        }
        return false;
    }
    
    public boolean move (int direction) {
        
        if (this.moveCount < MAX_MOVE_COUNT) {
            this.moveCount += 1;
            this.flightPathLog = this.flightPathLog + this.moveCount + "," + this.position.longitude() + "," + this.position.latitude() + "," + direction;
            this.position = Utils2D.movePoint(this.position, MOVE_LENGTH, direction);
            this.flightPathLog = this.flightPathLog + "," + this.position.longitude() + "," + this.position.latitude();
            this.flightPath.addMove(this.position, direction);
            return true;
        }
        return false;  
    }
    
    public boolean move (Path path) {
        
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

    public boolean moveToSensor(AirQualitySensor sensor) {

        var path = Path.findPathToPoint(this.position, sensor.getLocationAsPoint(), READING_RANGE, 
                MOVE_LENGTH, POSSIBLE_DIRECTIONS, this.noFlyZones);
        
        this.move(path);
        return this.readSensor(sensor);        
    }
    
    public boolean moveToPoint(Point target) {

        var path = Path.findPathToPoint(this.position, this.flightPath.getStartPoint(),
                0.00001, MOVE_LENGTH, POSSIBLE_DIRECTIONS, this.noFlyZones);
        
        if (this.move(path)) {
            this.flightPathLog = this.flightPathLog + "," + null + "\n";
        }  
        return false;
    } 
    
    public boolean returnToStartPosition() {
        return this.moveToPoint(this.flightPath.getStartPoint());
    }
       
    public void initiateRoutine () {
        var points = AirQualitySensor.toPoints(sensors);
        var graph = new Graph(points);
        graph.greedyOrder();
        
        for (int i = 0; i < 20; i++) {
            graph.swapOptimizeOrder();
        }
        var visitOrder = graph.getVisitOrder();

        for (int i = 0; i < visitOrder.length; i++) {
           this.moveToSensor(this.sensors.get(visitOrder[i]));
        }
        this.returnToStartPosition();
    }

}
