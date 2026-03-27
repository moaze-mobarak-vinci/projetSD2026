package be.vinci.resilience;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Localisation {
    private final long id;
    private final double latitude;
    private final double longitude;
    private final String nom;
    private final double altitude;
    private final List<Route> routesSortantes;

    public Localisation(long id, double latitude, double longitude, String nom, double altitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nom = nom;
        this.altitude = altitude;
        this.routesSortantes = new ArrayList<>();
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

    public List<Route> getRoutesSortantes() {
        return routesSortantes;
    }

    public void ajouterRouteSortante(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("route null");
        }
        routesSortantes.add(route);
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
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Localisation{id=" + id + ", nom='" + nom + "', altitude=" + altitude + "}";
    }


}