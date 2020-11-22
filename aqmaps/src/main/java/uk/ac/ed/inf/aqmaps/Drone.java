package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {
    
    private Point position;
    private int moveCount;
    private Path flightPath;
    private String flightPathLog;
    private ArrayList<Polygon> noFlyZones;
    private ArrayList<What3Words> sensorLocations;
    private ArrayList<What3Words> visitedSensorLocations;
    private ArrayList<AirQualitySensor> sensors;
    
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
        this.noFlyZones = noFlyZones;
        this.sensors = sensors;    
        this.sensorLocations = new ArrayList<What3Words>();
        this.visitedSensorLocations = new ArrayList<What3Words>();
        
        for (AirQualitySensor sensor : sensors) {
            sensorLocations.add(new What3Words(sensor.getLocation()));
        }
          
    }
    
    public boolean move (int direction) {
        
        if (this.moveCount < MAX_MOVE_COUNT) {
            this.position = Utils2D.movePoint(this.position, MOVE_LENGTH, direction);
            this.flightPath.addMove(this.position, direction);
            this.moveCount += 1;
            System.out.println(moveCount);
            return true;
        }
        return false;  
    }
    
    public boolean move (Path path) {
        
        for (Integer direction : path.getMoveDirections()) {
            if (!this.move(direction)) {
                return false;
            }
        }
        return true;
    }
    
    public Feature getFlightPath () {
        return this.flightPath.toFeature();
    }
    
    public boolean moveToNearestSensor () {
              
        var startingPath = new Path(this.position);
        var possiblePaths = startingPath.findContinuations(MOVE_LENGTH, POSSIBLE_DIRECTIONS, noFlyZones);
        var points = new ArrayList<Point>();
        
        for (What3Words w3w : this.sensorLocations) {
            points.add(w3w.toPoint());
        }
        
        var nearestSensor = Utils2D.findNearestPoint(this.position, points);
        
        while (true) {

            var path = Path.findBestPath(possiblePaths, nearestSensor, MOVE_LENGTH);

            for (What3Words location : sensorLocations) {
                if (Utils2D.distance(path.getEndPoint(), location.toPoint()) < READING_RANGE) {  
                    if (this.move(path)) {
                        this.visitedSensorLocations.add(location);
                        this.sensorLocations.remove(location);
                        return true;
                    }
                    
                }
            }
            possiblePaths.addAll(path.findContinuations(MOVE_LENGTH, POSSIBLE_DIRECTIONS, noFlyZones));
            possiblePaths.remove(path);

        }

    }
    
    public void collectReadings (int limit) {
        int count = 0;
        
        while (this.sensorLocations.size() > 0 && this.moveCount < MAX_MOVE_COUNT && count < limit) {
            this.moveToNearestSensor();
            count++;
        }
    }

    

}
