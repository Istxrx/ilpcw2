package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
    private ArrayList<AirQualitySensor> sensors;
    private ArrayList<AirQualitySensor> visitedSensors;
    
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
        this.visitedSensors = new ArrayList<AirQualitySensor>();
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
    
    public boolean moveToSensor(AirQualitySensor sensor) {

        var startingPath = new Path(this.position);
        var possiblePaths = startingPath.findContinuations(MOVE_LENGTH, POSSIBLE_DIRECTIONS, noFlyZones);

        while (true) {

            var path = Path.findBestPath(possiblePaths, sensor.getLocationAsPoint(), MOVE_LENGTH);

            System.out.println("search space size = " + possiblePaths.size());

            if (Utils2D.distance(path.getEndPoint(), sensor.getLocationAsPoint()) < READING_RANGE) {
                return this.move(path);

            }
            possiblePaths.addAll(0, path.findContinuations(MOVE_LENGTH, POSSIBLE_DIRECTIONS, noFlyZones));
            possiblePaths.remove(path);

        }
    }
    
    public void collectReadings () {
        var points = AirQualitySensor.toPoints(sensors);
        var graph = new Graph(points);
        graph.greedyOrder();
        
        for (int i = 0; i < 20; i++) {
            graph.swapOptimizeOrder();
        }
        


        var visitOrder = graph.getVisitOrder();
        
        for (int i = 0; i < visitOrder.length; i++) {
            System.out.println(visitOrder[i]);
        }
        
        
        for (int i = 0; i < visitOrder.length; i++) {
           this.moveToSensor(this.sensors.get(visitOrder[i]));
        }
    }
    
    public boolean moveToNearestSensor () {
              
        var startingPath = new Path(this.position);
        var possiblePaths = startingPath.findContinuations(MOVE_LENGTH, POSSIBLE_DIRECTIONS, noFlyZones);
        var points = new ArrayList<Point>();
        
        for (AirQualitySensor sensor : sensors) {
            points.add(sensor.getLocation().toPoint());
        }
        
        var nearestSensor = Utils2D.findNearestPoint(this.position, points);
        
        while (true) {

            var path = Path.findBestPath(possiblePaths, nearestSensor, MOVE_LENGTH);
            
            System.out.println("search space size = " + possiblePaths.size());

            for (AirQualitySensor sensor : sensors) {
                if (Utils2D.distance(path.getEndPoint(), sensor.getLocation().toPoint()) < READING_RANGE) {  
                    if (this.move(path)) {
                        this.visitedSensors.add(sensor);
                        this.sensors.remove(sensor);
                        return true;
                    }
                    
                }
            }
            possiblePaths.addAll(0,path.findContinuations(MOVE_LENGTH, POSSIBLE_DIRECTIONS, noFlyZones));
            possiblePaths.remove(path);

        }

    }
    
    public void collectReadings (int limit) {
        int count = 0;
        
        while (this.sensors.size() > 0 && this.moveCount < MAX_MOVE_COUNT && count < limit) {
            this.moveToNearestSensor();
            count++;
        }
    }

    

}
