package uk.ac.ed.inf.aqmaps;

public class AirQualitySensor {
    
    private String location;
    private double battery;
    private double reading;
    
    @Override
    public String toString() {
        return(this.location + " " + this.battery + " " + this.reading);
    }

    public String getLocation() {
        return this.location;
    }

    public double getBattery() {
        return this.battery;
    }

    public double getReading() {
        return this.reading;
    }
}
