package uk.ac.ed.inf.aqmaps;

import com.google.gson.Gson;
import com.mapbox.geojson.Point;

/**
 * Represents a unique combinations of 3 words that uniquely identify a locations with a resolution
 * of three meters anywhere in the world.
 */
public class What3Words {

    private String words;
    private Coordinates coordinates;

    private static class Coordinates {
        
        private double lng;
        private double lat;
    }

    /**
     * Loads the corresponding data of the specified what 3 words from a server
     * 
     * @param words the what 3 words separated by dots
     * @param port the port at which to connect to the server
     */
    public What3Words(String words, String port) {

        words = words.replaceAll("\\.", "/");
        var url = ("http://localhost:" + port + "/words/" + words + "/details.json");
        var what3words = loadFromUrl(url);

        this.coordinates = what3words.coordinates;
        this.words = what3words.words;
    }

    /**
     * @return corresponding coordinate location as a point
     */
    public Point toPoint() {
        return (Point.fromLngLat(coordinates.lng, coordinates.lat));
    }

    /**
     * @return what 3 words separated by dots
     */
    public String getWords() {
        return this.words;
    }

    /**
     * Loads the what 3 words from a .json file on a server
     * 
     * @param url an URL address of the server
     * @return what 3 words obtained from the server
     */
    public static What3Words loadFromUrl(String url) {

        var jsonString = App.readStringFromURL(url);
        var what3words = new Gson().fromJson(jsonString, What3Words.class);

        return what3words;
    }
}
