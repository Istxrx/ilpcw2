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
    private double length;

    public Path(ArrayList<Point> points, ArrayList<Integer> moveDirections, double length) {
        this.points = points;
        this.moveDirections = moveDirections;
        this.length = length;
    }

    public Path(Point startPoint) {
        this(new ArrayList<>(List.of(startPoint)), new ArrayList<>(), 0);
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

    public ArrayList<Path> findContinuations(double moveLength, ArrayList<Integer> directions,
            ArrayList<Polygon> obstacles) {

        var paths = new ArrayList<Path>();
        var start = this.getEndPoint();

        for (Integer direction : directions) {
            var end = Utils2D.movePoint(start, moveLength, direction);

            if (!Utils2D.lineIntersectPolygons(start, end, obstacles)) {

                var newPoints = new ArrayList<>(this.points);
                newPoints.add(end);

                var newMoveDirections = new ArrayList<>(this.moveDirections);
                newMoveDirections.add(direction);

                paths.add(new Path(newPoints, newMoveDirections, this.length + moveLength));
            }
        }
        return paths;
    }

    public double weightedHeuristicValue(Point target, double weight) {
        var pathLength = this.length;
        var remainingDistance = Utils2D.distance(this.getEndPoint(), target);

        return pathLength + weight * remainingDistance;
    }

    public static Path chooseBestPath(ArrayList<Path> paths, Point target) {
        var bestPath = paths.get(0);
        var bestHeuristicValue = bestPath.weightedHeuristicValue(target, 1.3);

        for (int i = 1; i < paths.size(); i++) {
            var currentPath = paths.get(i);
            var currentHeuristicValue = currentPath.weightedHeuristicValue(target, 1.3);

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
            var path = chooseBestPath(continuations, target);

            if (Utils2D.distance(path.getEndPoint(), target) < range) {
                return path;
            }
            continuations.addAll(path.findContinuations(moveLength, directions, obstacles));
            continuations.remove(path);
        }
        return null;
    }
}
