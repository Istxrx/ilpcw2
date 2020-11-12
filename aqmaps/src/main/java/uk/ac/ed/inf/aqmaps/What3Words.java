package uk.ac.ed.inf.aqmaps;

import com.google.gson.Gson;
import com.mapbox.geojson.Point;

public class What3Words {

    private String words;
    private Coordinates coordinates;
    
    private static class Coordinates {
        private double lng;
        private double lat;
    }
    
    public What3Words(String words) {
     
        words = words.replaceAll(".", "/");
        var url = ("http://localhost:80/words/" + words + "/details.json");
        var what3words = loadFromUrl(url);
        
        this.coordinates = what3words.coordinates;
        this.words = what3words.words;
    }
       
    @Override
    public String toString() {
        return(words +" longitude:" + coordinates.lng + " latitude:" + coordinates.lat);
    }
    
    public Point toPoint() {
        return(Point.fromLngLat(coordinates.lng, coordinates.lat));
    }
       
    public double getLatitude() {
        return this.coordinates.lat;
    }
    
    public double getLongitude() {
        return this.coordinates.lng;
    }
    
    public static What3Words loadFromUrl(String url) {
        
        var jsonString = App.readStringFromURL(url);
        var what3words = new Gson().fromJson(jsonString, What3Words.class);
   
        return what3words;
    }
}
