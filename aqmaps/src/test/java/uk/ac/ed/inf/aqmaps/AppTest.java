package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

public class AppTest {
    
    public static void appTime (String day, String month, String year) {

        var args = new String[] {day, month, year, "55.944425", "-3.188396", "5678", "80"};
        
        var startTime = System.nanoTime() / 1000000;
        App.main(args);
        var stopTime = System.nanoTime() / 1000000;
        System.out.println(stopTime - startTime + " miliseconds");
    }
    
    public static void runOn12Days() {
        var daysAndMonths = new ArrayList<String>(
                List.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"));

        for (String dayAndMonth : daysAndMonths) {
            var args = new String[] { dayAndMonth, dayAndMonth, "2020", "55.944425", "-3.188396",
                    "5678", "80" };
            App.main(args);
        }
    }
    
    public static void main(String[] args) {
        
        //runOn12Days();
        appTime("05", "05", "2021");

    }
}
