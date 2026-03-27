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
        adjacence.putIfAbsent(route.getIdOrigine(), new ArrayList<>());
        adjacence.get(route.getIdOrigine()).add(route);

        Localisation origine = localisations.get(route.getIdOrigine());
        if (origine != null) {
            origine.ajouterRouteSortante(route);
        }
    } 

    // --- MÉTHODES REQUISES PAR LE TEST (LOT A & B) ---
    // On les laisse vides (return par défaut) pour que le test compile.

    public Localisation[] determinerZoneInondee(long[] idsDepart, double epsilon) {
        if (idsDepart == null) {
            throw new IllegalArgumentException("idsDepart null");
        }

        List<Localisation> resultat = new ArrayList<>();
        Set<Long> visites = new HashSet<>();
        Queue<Localisation> file = new ArrayDeque<>();

        for (long id : idsDepart) {
            Localisation depart = localisations.get(id);
            if (depart != null && visites.add(id)) {
                file.add(depart);
                resultat.add(depart);
            }
        }

        while (!file.isEmpty()) {
            Localisation courant = file.remove();

            List<Route> routes = adjacence.get(courant.getId());
            if (routes == null) {
                continue;
            }

            for (Route route : routes) {
                Localisation voisin = localisations.get(route.getIdDestination());

                if (voisin != null
                    && !visites.contains(voisin.getId())
                    && voisin.getAltitude() <= courant.getAltitude() + epsilon) {

                    visites.add(voisin.getId());
                    file.add(voisin);
                    resultat.add(voisin);
                }
            }
        }

        return resultat.toArray(new Localisation[0]);
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
        Map<Localisation, Double> tFlood = new HashMap<>();

        // TODO: Implémenter la logique de propagation (V_water, pente, etc.)

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
}