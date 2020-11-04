package uk.ac.ed.inf.aqmaps;

public class AirQualitySensor {
    
    private String location;
    private double battery;
    private double reading;
    
    @Override
    public String toString() {
        return(location + " " + battery + " " + reading);
    }
    
    
}
