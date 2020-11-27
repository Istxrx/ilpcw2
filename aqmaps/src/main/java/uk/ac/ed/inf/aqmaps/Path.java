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

    public ArrayList<Integer> getMoveDirections() {
        return moveDirections;
    }
    
    public Point getEndPoint() {
        return points.get(points.size() - 1);
    }
    
    public Point getStartPoint() {
        return points.get(0); 
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

    public ArrayList<Path> findContinuations (double distance, ArrayList<Integer> directions, 
            ArrayList<Polygon> obstacles) {
        
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
    /*
    public double heuristicValue (Point target, double moveLength) {
        var pathLength = this.moveDirections.size() * moveLength;
        var distanceToDestination = Utils2D.distance(this.getEndPoint(), target);
        
        return pathLength + distanceToDestination;
    }*/
    
    public double weightedHeuristicValue (Point target, double moveLength, double weight) {
        var pathLength = this.moveDirections.size() * moveLength;
        var remainingDistance = Utils2D.distance(this.getEndPoint(), target);
        
        return pathLength + weight * remainingDistance;
    }
    /*
    public double greedyHeuristicValue (Point destination, double moveLength) {

        var distanceToDestination = Utils2D.distance(this.getEndPoint(), destination);
        
        return distanceToDestination;
    }*/

    public static Path chooseBestPath (ArrayList<Path> paths, Point target, double moveLength) {
        var bestPath = paths.get(0);
        var bestHeuristicValue = bestPath.weightedHeuristicValue(target, moveLength, 1.3);
        
        for (int i = 1; i < paths.size(); i++) {
            var currentPath = paths.get(i);
            var currentHeuristicValue = currentPath.weightedHeuristicValue(target, moveLength, 1.3);

            if (currentHeuristicValue < bestHeuristicValue) {
                bestPath = currentPath;
                bestHeuristicValue = currentHeuristicValue;        
            }
        }
        return bestPath;
    }
    
    public static Path findPathToPoint(Point start, Point target, double range, double moveLength, 
            ArrayList<Integer> directions, ArrayList<Polygon> obstacles) {

        var startingPath = new Path(start);
        var continuations = startingPath.findContinuations(moveLength, directions, obstacles);

        while (continuations.size() > 0) {

            var path = chooseBestPath(continuations, target, moveLength);

            System.out.println("search space size = " + continuations.size());

            if (Utils2D.distance(path.getEndPoint(), target) < range) {
                return path;
            }
            continuations.addAll(path.findContinuations(moveLength, directions, obstacles));
            continuations.remove(path);
        }
        return null;
    } 
}
