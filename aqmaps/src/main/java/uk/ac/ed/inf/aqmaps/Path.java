package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Path {

    private Point point;
    private Integer moveDirection;
    private double length;

    private Path previous;
    private ArrayList<Path> branches;

    private static final double EPSILON = 2.0;

    private Path(Point point, Integer moveDirection, double length, Path previous,
            ArrayList<Path> branches) {

        this.point = point;
        this.moveDirection = moveDirection;
        this.length = length;
        this.previous = previous;
        this.branches = branches;
    }

    public Path(Point startPoint) {
        this(startPoint, null, 0, null, null);
    }

    public ArrayList<Integer> getMoveDirections() {
        var moveDirections = new ArrayList<Integer>();
        var path = this;

        while (path.branches != null) {
            path = path.branches.get(0);
            moveDirections.add(path.moveDirection);
        }
        return moveDirections;
    }

    public ArrayList<Point> getPoints() {
        var points = new ArrayList<Point>();
        var path = this;

        while (true) {
            points.add(path.point);
            if (path.branches != null) {
                path = path.branches.get(0);
            } else {
                break;
            }
        }
        return points;
    }

    public Point getStartPoint() {
        var path = this;
        while (path.previous != null) {
            path = path.previous;
        }
        return path.point;
    }

    public void addMove(Point end, int direction) {
        var path = this;
        while (path.branches != null) {
            path = path.branches.get(0);
        }
        path.branches = new ArrayList<>();
        var branch = new Path(end, direction, 0, this, null);
        path.branches.add(branch);
    }

    public Feature toFeature() {
        var lineString = LineString.fromLngLats(this.getPoints());
        var feature = Feature.fromGeometry((Geometry) lineString);

        return feature;
    }

    private Path trimToStart() {
        var path = this;
        while (path.previous != null) {
            path.previous.branches = new ArrayList<>(List.of(path));
            path = path.previous;
        }
        return path;
    }

    private ArrayList<Path> findBranches(double moveLength, ArrayList<Integer> directions,
            ArrayList<Polygon> obstacles) {
        this.branches = new ArrayList<>();
        var start = this.point;

        for (Integer direction : directions) {
            var end = Utils2D.movePoint(start, moveLength, direction);

            if (!Utils2D.lineIntersectPolygons(start, end, obstacles)) {
                var branch = new Path(end, direction, this.length + moveLength, this, null);
                this.branches.add(branch);
            }
        }
        return new ArrayList<>(this.branches);
    }

    private double weightedHeuristicValue(Point target, double weight) {
        var pathLength = this.length;
        var remainingDistance = Utils2D.distance(this.point, target);

        return pathLength + weight * remainingDistance;
    }

    private static Path chooseBestPath(ArrayList<Path> paths, Point target) {
        var bestPath = paths.get(0);
        var bestHeuristicValue = bestPath.weightedHeuristicValue(target, EPSILON);

        for (Path path : paths) {
            var heuristicValue = path.weightedHeuristicValue(target, EPSILON);

            if (heuristicValue < bestHeuristicValue) {
                bestPath = path;
                bestHeuristicValue = heuristicValue;
            }
        }
        return bestPath;
    }

    public static Path findPathToPoint(Point start, Point target, double range, double moveLength,
            ArrayList<Integer> directions, ArrayList<Polygon> obstacles) {

        var startingPath = new Path(start);
        var paths = startingPath.findBranches(moveLength, directions, obstacles);

        while (paths.size() > 0) {
            var path = chooseBestPath(paths, target);

            if (Utils2D.distance(path.point, target) < range) {
                return path.trimToStart();
            }
            paths.addAll(path.findBranches(moveLength, directions, obstacles));
            paths.remove(path);
        }
        return null;
    }
}
