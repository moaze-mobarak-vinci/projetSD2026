package be.vinci.resilience;

public class Route {
    private final long idOrigine;
    private final long idDestination;
    private final double distance;
    private final String nom;

    public Route(long idOrigine, long idDestination, double distance, String nom) {
        this.idOrigine = idOrigine;
        this.idDestination = idDestination;
        this.distance = distance;
        this.nom = nom;
    }

    // Getters
    public long getIdOrigine() { return idOrigine; }
    public long getIdDestination() { return idDestination; }
    public double getDistance() { return distance; }
    public String getNom() { return nom; }

    /**
     * Calcule la pente S entre deux points.
     * Formule du PDF : S = (Alt(X) - Alt(Y)) / distance
     */
    public double calculerPente(double altX, double altY) {
        if (this.distance == 0) return 0;
        return (altX - altY) / this.distance;
    }
}