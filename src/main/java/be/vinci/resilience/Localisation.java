package be.vinci.resilience;

public class Localisation {

    private final String id;
    private final double latitude;
    private final double longitude;
    private final String nom;
    private final double altitude;

    public Localisation(String id, double latitude, double longitude, String nom, double altitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nom = nom;
        this.altitude = altitude;
    }

    // Getters obligatoires pour tes algorithmes
    public String getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getNom() { return nom; }
    public double getAltitude() { return altitude; }

    @Override
    public String toString() {
        return "Localisation{" + id + " - " + nom + " (alt: " + altitude + "m)}";
    }

}
