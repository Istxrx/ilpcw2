package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;

public class Graph {

    private double[][] distanceMatrix;
    private int[] visitOrder;

    public Graph(ArrayList<Point> nodes) {

        this.distanceMatrix = new double[nodes.size()][nodes.size()];
        this.visitOrder = new int[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                this.distanceMatrix[i][j] = Utils2D.distance(nodes.get(i), nodes.get(j));
            }
            this.visitOrder[i] = i;
        }
    }

    public int[] getVisitOrder() {
        return this.visitOrder;
    }

    public void swapOrder(int startIndex, int endIndex) {

        for (int i = 0; i <= (endIndex - startIndex) / 2; i++) {
            var temp = this.visitOrder[startIndex + i];
            this.visitOrder[startIndex + i] = this.visitOrder[endIndex - i];
            this.visitOrder[endIndex - i] = temp;
        }
    }

    public void greedyOrder() {

        for (int i = 1; i < this.visitOrder.length; i++) {
            var current = visitOrder[i - 1];
            var nearest = visitOrder[i];
            var minDistance = this.distanceMatrix[current][nearest];

            for (int j = i + 1; j < this.distanceMatrix.length; j++) {
                var candidate = visitOrder[j];
                var distance = this.distanceMatrix[current][candidate];
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = candidate;
                    this.visitOrder[j] = this.visitOrder[i];
                    this.visitOrder[i] = nearest;
                }
            }
        }
    }

    public void swapOptimizeOrder(int numberOfTries) {

        var optimizable = true;
        int count = 0;
        while (count < numberOfTries && optimizable) {
            optimizable = false;
            for (int i = 1; i < this.visitOrder.length - 1; i++) {
                for (int j = i + 1; j < this.visitOrder.length; j++) {

                    var previousToStartNode = this.visitOrder[i - 1];
                    var startNode = this.visitOrder[i];
                    var endNode = this.visitOrder[j];
                    var nextToEndNode = this.visitOrder[(j + 1) % this.visitOrder.length];

                    var currentDistances = this.distanceMatrix[previousToStartNode][startNode]
                            + this.distanceMatrix[endNode][nextToEndNode];

                    var swappedDistances = this.distanceMatrix[previousToStartNode][endNode]
                            + this.distanceMatrix[startNode][nextToEndNode];

                    if (swappedDistances < currentDistances) {
                        this.swapOrder(i, j);
                        optimizable = true;
                    }
                }
            }
            count++;
        }
    }

}
