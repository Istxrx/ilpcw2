package uk.ac.ed.inf.aqmaps;

public class What3Words {

    private String words;
    private Coordinates coordinates;
    
    private static class Coordinates {
        public double lng;
        public double lat;
    }
    
    public What3Words(String words) {
        words = words.replaceAll(".", "/");
        var url = ("http://localhost:80/words/" + words + "/details.json");
        var what3words = App.loadWhat3WordsFromUrl(url);
        this.coordinates = what3words.getCoordinates();
        this.words = what3words.getWords();
    }
    
    @Override
    public String toString() {
        return(words +" longitude:" + coordinates.lng + " latitude:" + coordinates.lat);
    }
    
    public String getWords() {
        return this.words;
    }
    
    public Coordinates getCoordinates() {
        return this.coordinates;
    }
    
}
