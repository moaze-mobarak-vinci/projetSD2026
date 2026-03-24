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

            // 2. Boucle unique de propagation (Dijkstra)
            while (!queue.isEmpty()) {
                EtatInondation actuel = queue.poll();
                Localisation locActuelle = actuel.localisation;

                if (tFlood.containsKey(locActuelle)) {
                    continue;
                }

                tFlood.put(locActuelle, actuel.temps);

                // 3. Propagation vers les voisins
                List<Route> routes = adjacence.get(locActuelle.getId());
                if (routes != null) {
                    for (Route route : routes) {
                        Localisation voisin = localisations.get(route.getIdDestination());

                        if (voisin != null && !tFlood.containsKey(voisin)) {
                            double pente = route.calculerPente(locActuelle.getAltitude(), voisin.getAltitude());
                            double nouvelleVitesse = actuel.vitesse + (k * pente);

                            if (nouvelleVitesse > 0) {
                                double tempsParcours = route.getDistance() / nouvelleVitesse;
                                double tempsArriveeVoisin = actuel.temps + tempsParcours;

                                queue.add(new EtatInondation(voisin, tempsArriveeVoisin, nouvelleVitesse));
                            }
                        }
                    }
                }
            }

            return tFlood;
        }



    /**
     * ALGORITHME 4 : Évacuation dynamique
     */
    public Deque<Localisation> trouverCheminDEvacuationLePlusCourt(long depart, long destination, double vitesse, Map<Localisation, Double> tFlood) {
        Deque<Localisation> chemin = new ArrayDeque<>();
        PriorityQueue<EtatVehicule> queue = new PriorityQueue<>();

        // Map pour mémoriser le meilleur temps d'arrivée à un nœud pour le véhicule
        Map<Localisation, Double> meilleursTempsVehicule = new HashMap<>();

        Localisation locDepart = localisations.get(depart);
        if (locDepart == null) return chemin;

        // On commence au point de départ au temps t = 0 (sans parent)
        queue.add(new EtatVehicule(locDepart, 0.0, null));
        meilleursTempsVehicule.put(locDepart, 0.0);

        // TODO: Étape suivante : Boucle principale de déplacement du véhicule !

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

    // Classe interne pour l'Algorithme 4 (Dijkstra pour la voiture)
    private static class EtatVehicule implements Comparable<EtatVehicule> {
        final Localisation localisation;
        final double temps;
        final EtatVehicule parent; // Pour pouvoir reconstruire le chemin à la fin

        EtatVehicule(Localisation localisation, double temps, EtatVehicule parent) {
            this.localisation = localisation;
            this.temps = temps;
            this.parent = parent;
        }

        @Override
        public int compareTo(EtatVehicule autre) {
            return Double.compare(this.temps, autre.temps); // Priorité au véhicule le plus rapide
        }
    }

}