package be.vinci.resilience;

public class Localisation {
    private final long id; // On a changé String en long ici
    private final double latitude;
    private final double longitude;
    private final String nom;
    private final double altitude;

    public Localisation(long id, double latitude, double longitude, String nom, double altitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nom = nom;
        this.altitude = altitude;
    }

    public long getId() {
        return id;
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getNom() {
        return nom;
    }

    public double getAltitude() {
        return altitude;
    }

    @Override
    public String toString() {
        return id + " : altitude : " + altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Localisation)) return false;
        Localisation that = (Localisation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}