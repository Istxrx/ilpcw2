package uk.ac.ed.inf.aqmaps;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;



public class App {
    
    //Constant bounds for values of latitudes and longitudes with respect to cardinal directions
    private static final BigDecimal BOUND_LATITUDE_NORTH = new BigDecimal("55.946233");
    private static final BigDecimal BOUND_LATITUDE_SOUTH = new BigDecimal("55.942617");
    private static final BigDecimal BOUND_LONGITUDE_EAST = new BigDecimal("-3.184319");
    private static final BigDecimal BOUND_LONGITUDE_WEST = new BigDecimal("-3.192473");
    
    private static String readStringFromURL(String url) {
        
        // Create a new HttpClient with default settings.
        var client = HttpClient.newHttpClient();
        
        // HttpClient assumes that it is a GET request by default.
        var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        
        try {
            // The response object is of class HttpResponse<String>
            var response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());
            return(response.body());
            
        } catch (Exception e) {
            System.out.println("Fatal error: Unable to connect to " + url + ".");
            System.exit(1);
        }
        
        return null;
    }
    
    private static ArrayList<AirQualitySensor> loadAirQualitySensorsFromURL(String url) {
        
        var jsonString = readStringFromURL(url);
        Type listType = new TypeToken<ArrayList<AirQualitySensor>>(){}.getType();
        ArrayList<AirQualitySensor> sensors = new Gson().fromJson(jsonString, listType);
        
        return sensors;
    }
    
    private static ArrayList<Polygon> loadNoFlyZonesFromURL(String url) {
        
        var geoJsonString = readStringFromURL(url);
        var featureCollection = FeatureCollection.fromJson(geoJsonString);
        var features = featureCollection.features();
        var polygons = new ArrayList<Polygon>();
        
        for (var feature : features) {
            var geometry = feature.geometry();
            polygons.add((Polygon) geometry);
        }
        
        return polygons;
    }
    
    public static void main(String[] args) {
        // "http://localhost:80/buildings/no-fly-zones.geojson"
        //System.out.println(readStringFromURL("http://localhost:80/buildings/no-fly-zones.geojson"));
        var nfz = loadNoFlyZonesFromURL("http://localhost:80/buildings/no-fly-zones.geojson");
        System.out.println(nfz.get(0).toString());
        
        var aqsensors = loadAirQualitySensorsFromURL("http://localhost:80/maps/2020/01/01/air-quality-data.json");
        System.out.println(aqsensors.get(9).toString());
    }
}
