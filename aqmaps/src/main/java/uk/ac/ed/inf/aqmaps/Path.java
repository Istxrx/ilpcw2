package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Encapsulates sequence of moves as both-ways linked list structure. Provides functionality to
 * search for a path to a target point in 2D space.
 */
public class Path {
    
    /**
     * Used as a weight in heuristic function
     */
    private static final double EPSILON = 2.0;
    
    private Point point;
    private Integer cameFromDirection;
    private double length;

    private Path previous;
    private Path next;

    private Path(Point point, Integer moveDirection, double length, Path previous,
            Path next) {

        this.point = point;
        this.cameFromDirection = moveDirection;
        this.length = length;
        this.previous = previous;
        this.next = next;
    }

    public Path(Point startPoint) {
        this(startPoint, null, 0, null, null);
    }

    /**
     * Assumes this is reference to start of the this and lists all move directions used in it
     * onwards.
     * 
     * @return list of move directions that in this path
     */
    public ArrayList<Integer> getMoveDirections() {
        
        var moveDirections = new ArrayList<Integer>();
        var path = this;

        while (path.next != null) {
            path = path.next;
            moveDirections.add(path.cameFromDirection);
        }
        return moveDirections;
    }

    /**
     * Assumes this is reference to start of the this and lists all points in it onwards.
     * 
     * @return list of points that in this path
     */
    public ArrayList<Point> getPoints() {
        
        var points = new ArrayList<Point>();
        var path = this;

        while (true) {
            points.add(path.point);
            if (path.next != null) {
                path = path.next;
            } else {
                break;
            }
        }
        return points;
    }

    /**
     * @return the first point in this path
     */
    public Point getStartPoint() {
        
        var path = this;
        while (path.previous != null) {
            path = path.previous;
        }
        return path.point;
    }

    /**
     * Extends the end of this path by a new move. Does not keep track of length.
     *
     * @param end the new end point of the path
     * @param direction the direction that was used to get to this point
     */
    public void addMove(Point end, int direction) {
        
        var path = this;
        while (path.next != null) {
            path = path.next;
        }
        var next = new Path(end, direction, 0, this, null);
        path.next = next;
    }

    /**
     * @return line string feature representing the current path from here onwards
     */
    public Feature toFeature() {
        
        var lineString = LineString.fromLngLats(this.getPoints());
        var feature = Feature.fromGeometry((Geometry) lineString);

        return feature;
    }

    /**
     * Reconstructs the path from end to start.
     * 
     * @return the reference to the start of this path
     */
    private Path getStart() {
        
        var path = this;
        while (path.previous != null) {
            path.previous.next = path;
            path = path.previous;
        }
        return path;
    }

    /**
     * Finds legal continuations of this path. Legal continuation can not cross obstacle and is
     * result of moving this point attribute by move length in a one of directions.
     * 
     * @param moveLength the length of each move in each path
     * @param directions the list of directions in degrees that will be explored
     * @param obstacles  the list of polygons that should not be crossed
     * @return list of legal continuations
     */
    private ArrayList<Path> findBranches(double moveLength, ArrayList<Integer> directions,
            ArrayList<Polygon> obstacles) {
        
        var branches = new ArrayList<Path>();
        var start = this.point;

        for (Integer direction : directions) {
            var end = Utils2D.movePoint(start, moveLength, direction);

            if (!Utils2D.lineIntersectPolygons(start, end, obstacles)) {
                var branch = new Path(end, direction, this.length + moveLength, this, null);
                branches.add(branch);
            }
        }
        return branches;
    }

    /**
     * Assigns value based on length of this path and estimated distance to target point.
     *
     * @param target the point that the path should lead to
     * @param epsilon the weight, epsilon > 1 favors path length over distance to target
     * @return heuristic value = path length + epsilon * distance to target
     */
    private double weightedHeuristicValue(Point target, double epsilon) {
        
        var pathLength = this.length;
        var remainingDistance = Utils2D.distance(this.point, target);

        return pathLength + epsilon * remainingDistance;
    }

    /**
     * @param paths the paths to choose from
     * @param target the point that each path should lead to
     * @return the path from the list that has the lowest (best) heuristic value
     */
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

    /**
     * Performs an variation of A* search.
     *
     * @param start the start point of the path
     * @param target target the point that the path should lead to
     * @param range the range around the target within which the path should end
     * @param moveLength the length of each move in the path
     * @param directions the list of allowed directions for the moves in degrees
     * @param obstacles the list of polygons that should not be crossed
     * @return path to the target point within range while avoiding obstacles
     */
    public static Path findPathToPoint(Point start, Point target, double range, double moveLength,
            ArrayList<Integer> directions, ArrayList<Polygon> obstacles) {

        var startingPath = new Path(start);
        var paths = startingPath.findBranches(moveLength, directions, obstacles);

        while (paths.size() > 0) {
            var path = chooseBestPath(paths, target);

            if (Utils2D.distance(path.point, target) < range) {
                return path.getStart();
            }
            paths.addAll(path.findBranches(moveLength, directions, obstacles));
            paths.remove(path);
        }
        return null;
    }
}
