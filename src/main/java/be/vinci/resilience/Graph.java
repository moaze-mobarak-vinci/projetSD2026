package be.vinci.resilience;

import java.util.*;

public class Graph {
    // Utilisation de Long pour les IDs pour correspondre au test
    private Map<Long, Localisation> localisations = new HashMap<>();
    private Map<Long, List<Route>> adjacence = new HashMap<>();

    /**
     * Constructeur utilisé par TestSimulator10
     * @param fichierNoeuds nom du fichier (ex: "nodes_10.csv")
     * @param fichierArcs nom du fichier (ex: "edges_10.csv")
     */
    public Graph(String fichierNoeuds, String fichierArcs) {
        // On appelle le loader. On suppose que les fichiers sont dans le dossier "data"
        GraphLoader.chargerGraphe(this, "data/" + fichierNoeuds, "data/" + fichierArcs);
    }

    public void addLocalisation(Localisation loc) {
        localisations.put(loc.getId(), loc);
        adjacence.putIfAbsent(loc.getId(), new ArrayList<>());
    }

    public void addRoute(Route route) {
        if (adjacence.containsKey(route.getIdOrigine())) {
            adjacence.get(route.getIdOrigine()).add(route);
        }
    }

    // --- MÉTHODES REQUISES PAR LE TEST (LOT A & B) ---
    // On les laisse vides (return par défaut) pour que le test compile.

    public Localisation[] determinerZoneInondee(long[] ids, double epsilon) {
        // TODO: À compléter si tu dois faire le Lot A
        return new Localisation[0];
    }

    public Deque<Localisation> trouverCheminLePlusCourtPourContournerLaZoneInondee(long depart, long destination, Localisation[] zone) {
        // TODO: À compléter si tu dois faire le Lot B
        return new ArrayDeque<>();
    }

    // --- TES MÉTHODES (LOT C - ANALYSE TEMPORELLE) ---

    /**
     * ALGORITHME 3 : Chronologie de la crue
     */
    public Map<Localisation, Double> determinerChronologieDeLaCrue(long[] sources, double t0, double k) {
        // Map finale ordonnée : associe une Localisation à son temps d'inondation
        Map<Localisation, Double> tFlood = new LinkedHashMap<>();

        // File de priorité pour Dijkstra (ordonnée par le temps le plus court)
        PriorityQueue<EtatInondation> queue = new PriorityQueue<>();

        // 1. Initialisation : l'eau commence aux sources au temps 0 avec la vitesse initiale t0
        for (long id : sources) {
            Localisation loc = localisations.get(id);
            if (loc != null) {
                queue.add(new EtatInondation(loc, 0.0, t0));
            }
        }

        // TODO: Ajouter la boucle de propagation

        return tFlood;
    }

    /**
     * ALGORITHME 4 : Évacuation dynamique
     */
    public Deque<Localisation> trouverCheminDEvacuationLePlusCourt(long depart, long destination, double vitesse, Map<Localisation, Double> tFlood) {
        Deque<Localisation> chemin = new ArrayDeque<>();

        // TODO: Implémenter Dijkstra modifié avec la contrainte de temps

        return chemin;
    }

    // Classe interne pour l'Algorithme 3 (Dijkstra pour la crue)
    private static class EtatInondation implements Comparable<EtatInondation> {
        final Localisation localisation;
        final double temps;
        final double vitesse;

        EtatInondation(Localisation localisation, double temps, double vitesse) {
            this.localisation = localisation;
            this.temps = temps;
            this.vitesse = vitesse;
        }

        @Override
        public int compareTo(EtatInondation autre) {
            return Double.compare(this.temps, autre.temps); // Priorité au temps le plus court
        }
    }
}