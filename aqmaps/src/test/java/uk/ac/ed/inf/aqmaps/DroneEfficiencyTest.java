package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

public class DroneEfficiencyTest {
    

    private static void averageOfMostDays(String year) {

        var days = new ArrayList<String>(List.of("01", "02", "03", "04", "05", "06", "07", "08",
                "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
                "23", "24", "25", "26", "27", "28"));

        var months = new ArrayList<String>(
                List.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"));

        var moveSum = 0;
        var count = 0;
        var executionTimeSum = 0.0;

        for (String month : months) {
            for (String day : days) {
                var startTime = System.nanoTime() / 1000000;
                var drone = App.initiateDrone(day, month, year, 55.9444, -3.1878, "80");
                var stopTime = System.nanoTime() / 1000000;
                System.out.println(day + " " + month + " " + year + " Move count: "
                        + String.valueOf(drone.getMoveCount()));
                System.out.println(day + " " + month + " " + year + " Execution time: "
                        + String.valueOf(stopTime - startTime) + " miliseconds");
                moveSum += drone.getMoveCount();
                executionTimeSum += stopTime - startTime;
                count++;
            }
        }

        System.out.println("Average move count: " + moveSum / count);
        System.out.println("Average execution time: " + executionTimeSum / count + " miliseconds");
    }

    public static void main(String[] args) {
        averageOfMostDays("2021");
    }

}
