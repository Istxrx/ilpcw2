package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Path {

    private ArrayList<Point> points;
    private ArrayList<Integer> moveDirections;
    
    public Path(ArrayList<Point> points, ArrayList<Integer> moveDirections) {
        this.points = points;
        this.moveDirections = moveDirections;
    }

    public Path(Point origin) {
        this.points = new ArrayList<>(List.of(origin));
        this.moveDirections = new ArrayList<>();
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public ArrayList<Integer> getMoveDirections() {
        return moveDirections;
    }
    
    public Point getEndPoint() {
        return points.get(points.size() - 1);
    }
    
    public void addMove(Point end, int direction) {
        this.points.add(end);
        this.moveDirections.add(direction);
    }
    
    public Feature toFeature() {
        var lineString = LineString.fromLngLats(this.points);
        var feature = Feature.fromGeometry((Geometry) lineString);
        
        return feature;
    }

    public ArrayList<Path> findContinuations (double distance, ArrayList<Integer> directions, ArrayList<Polygon> obstacles) {
        
        var paths = new ArrayList<Path>();
        var start = this.getEndPoint();
        
        for (Integer direction : directions) {
            var end = Utils2D.movePoint(start, distance, direction);
            
            if (!Utils2D.lineIntersectPolygons(start, end, obstacles)) {
                
                var newPoints = new ArrayList<>(this.points);
                newPoints.add(end);
                var newMoveDirections = new ArrayList<>(this.moveDirections);
                newMoveDirections.add(direction);
                
                paths.add(new Path(newPoints, newMoveDirections));
            }
        }
        return paths;
    }
    
    public double heuristicValue (Point destination, double moveLength) {
        var pathLength = this.moveDirections.size() * moveLength;
        var distanceToDestination = Utils2D.distance(this.getEndPoint(), destination);
        
        return pathLength + distanceToDestination;
    }

    public static Path findBestPath (ArrayList<Path> paths, Point destination, double moveLength) {
        var bestPath = paths.get(0);
        var bestHeuristicValue = bestPath.heuristicValue(destination, moveLength);
        
        for (int i = 1; i < paths.size(); i++) {
            var currentPath = paths.get(i);
            var currentHeuristicValue = currentPath.heuristicValue(destination, moveLength);
            //var difference = Math.abs(currentHeuristicValue - bestHeuristicValue);
            // && difference > moveLength
            if (currentHeuristicValue < bestHeuristicValue) {
                bestPath = currentPath;
                bestHeuristicValue = currentHeuristicValue;
                
            }
        }
        return bestPath;
    }
    
}
