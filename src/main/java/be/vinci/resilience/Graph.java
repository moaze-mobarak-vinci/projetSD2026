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

    private static class WaterState {
        long id;
        double time;
        double speed;

        WaterState(long id, double time, double speed) {
            this.id = id;
            this.time = time;
            this.speed = speed;
        }
    }

    private static class TravelState {
        long id;
        double time;

        TravelState(long id, double time) {
            this.id = id;
            this.time = time;
        }
    }

    public Localisation[] determinerZoneInondee(long[] ids, double epsilon) {
        if (ids == null || ids.length == 0) {
            return new Localisation[0];
        }

        Deque<Long> file = new ArrayDeque<>();
        Set<Long> visites = new HashSet<>();
        List<Localisation> ordreInondation = new ArrayList<>();

        for (long id : ids) {
            Localisation source = localisations.get(id);
            if (source != null && visites.add(id)) {
                file.add(id);
                ordreInondation.add(source);
            }
        }

        while (!file.isEmpty()) {
            long courantId = file.poll();
            Localisation courant = localisations.get(courantId);
            if (courant == null) {
                continue;
            }

            List<Route> routes = adjacence.getOrDefault(courantId, Collections.emptyList());
            for (Route route : routes) {
                long voisinId = route.getIdDestination();
                if (visites.contains(voisinId)) {
                    continue;
                }

                Localisation voisin = localisations.get(voisinId);
                if (voisin == null) {
                    continue;
                }

                if (voisin.getAltitude() <= courant.getAltitude() + epsilon) {
                    visites.add(voisinId);
                    file.add(voisinId);
                    ordreInondation.add(voisin);
                }
            }
        }

        return ordreInondation.toArray(new Localisation[0]);
    }

    public Deque<Localisation> trouverCheminLePlusCourtPourContournerLaZoneInondee(long depart, long destination, Localisation[] zone) {
        Deque<Localisation> chemin = new ArrayDeque<>();

        Localisation locDepart = localisations.get(depart);
        Localisation locDestination = localisations.get(destination);
        if (locDepart == null || locDestination == null) {
            return chemin;
        }

        Set<Long> idsInondes = new HashSet<>();
        if (zone != null) {
            for (Localisation loc : zone) {
                if (loc != null) {
                    idsInondes.add(loc.getId());
                }
            }
        }

        if (idsInondes.contains(depart) || idsInondes.contains(destination)) {
            return chemin;
        }

        if (depart == destination) {
            chemin.add(locDepart);
            return chemin;
        }

        Queue<Long> file = new ArrayDeque<>();
        Set<Long> visites = new HashSet<>();
        Map<Long, Long> parent = new HashMap<>();

        file.add(depart);
        visites.add(depart);

        boolean destinationAtteinte = false;
        while (!file.isEmpty() && !destinationAtteinte) {
            long courant = file.poll();
            List<Route> routes = adjacence.getOrDefault(courant, Collections.emptyList());

            for (Route route : routes) {
                long voisin = route.getIdDestination();
                if (visites.contains(voisin) || idsInondes.contains(voisin)) {
                    continue;
                }

                if (!localisations.containsKey(voisin)) {
                    continue;
                }

                visites.add(voisin);
                parent.put(voisin, courant);

                if (voisin == destination) {
                    destinationAtteinte = true;
                    break;
                }

                file.add(voisin);
            }
        }

        if (!destinationAtteinte) {
            return chemin;
        }

        Long courant = destination;
        while (courant != null) {
            Localisation localisation = localisations.get(courant);
            if (localisation == null) {
                return new ArrayDeque<>();
            }
            chemin.addFirst(localisation);
            if (courant == depart) {
                break;
            }
            courant = parent.get(courant);
        }

        if (chemin.isEmpty() || chemin.peekFirst().getId() != depart) {
            return new ArrayDeque<>();
        }

        return chemin;
    }

    public Map<Localisation, Double> determinerChronologieDeLaCrue(long[] sources, double vWaterInit, double k) {
        if (sources == null || sources.length == 0) {
            return new LinkedHashMap<>();
        }

        Map<Long, Double> bestTime = new HashMap<>();
        PriorityQueue<WaterState> pq = new PriorityQueue<>(Comparator.comparingDouble(s -> s.time));

        for (long sourceId : sources) {
            if (!localisations.containsKey(sourceId)) {
                continue;
            }
            double previous = bestTime.getOrDefault(sourceId, Double.POSITIVE_INFINITY);
            if (0.0 < previous) {
                bestTime.put(sourceId, 0.0);
                pq.add(new WaterState(sourceId, 0.0, vWaterInit));
            }
        }

        while (!pq.isEmpty()) {
            WaterState state = pq.poll();
            double known = bestTime.getOrDefault(state.id, Double.POSITIVE_INFINITY);
            if (state.time > known + 1e-12) {
                continue;
            }

            Localisation courant = localisations.get(state.id);
            if (courant == null) {
                continue;
            }

            List<Route> routes = adjacence.getOrDefault(state.id, Collections.emptyList());
            for (Route route : routes) {
                Localisation voisin = localisations.get(route.getIdDestination());
                if (voisin == null) {
                    continue;
                }
                if (route.getDistance() <= 0) {
                    continue;
                }

                double slope = (courant.getAltitude() - voisin.getAltitude()) / route.getDistance();
                double nextSpeed = state.speed + (k * slope);
                if (nextSpeed <= 0) {
                    continue;
                }

                double edgeTime = route.getDistance() / nextSpeed;
                double newTime = state.time + edgeTime;
                long voisinId = voisin.getId();
                double oldTime = bestTime.getOrDefault(voisinId, Double.POSITIVE_INFINITY);

                if (newTime + 1e-12 < oldTime) {
                    bestTime.put(voisinId, newTime);
                    pq.add(new WaterState(voisinId, newTime, nextSpeed));
                }
            }
        }

        List<Map.Entry<Long, Double>> entries = new ArrayList<>(bestTime.entrySet());
        entries.sort((a, b) -> {
            int byTime = Double.compare(a.getValue(), b.getValue());
            if (byTime != 0) {
                return byTime;
            }
            return Long.compare(a.getKey(), b.getKey());
        });

        Map<Localisation, Double> ordered = new LinkedHashMap<>();
        for (Map.Entry<Long, Double> entry : entries) {
            Localisation loc = localisations.get(entry.getKey());
            if (loc != null) {
                ordered.put(loc, entry.getValue());
            }
        }

        return ordered;
    }

    public Deque<Localisation> trouverCheminDEvacuationLePlusCourt(long depart, long destination, double vVehicule, Map<Localisation, Double> tFlood) {
        Deque<Localisation> chemin = new ArrayDeque<>();
        if (vVehicule <= 0) {
            return chemin;
        }

        Localisation locDepart = localisations.get(depart);
        Localisation locDestination = localisations.get(destination);
        if (locDepart == null || locDestination == null) {
            return chemin;
        }

        Map<Long, Double> tFloodById = new HashMap<>();
        if (tFlood != null) {
            for (Map.Entry<Localisation, Double> entry : tFlood.entrySet()) {
                Localisation loc = entry.getKey();
                Double time = entry.getValue();
                if (loc != null && time != null) {
                    tFloodById.put(loc.getId(), time);
                }
            }
        }

        Double tDepartFlood = tFloodById.get(depart);
        if (tDepartFlood != null && 0.0 >= tDepartFlood) {
            return chemin;
        }

        Map<Long, Double> bestTime = new HashMap<>();
        Map<Long, Long> parent = new HashMap<>();
        PriorityQueue<TravelState> pq = new PriorityQueue<>(Comparator.comparingDouble(s -> s.time));

        bestTime.put(depart, 0.0);
        pq.add(new TravelState(depart, 0.0));

        while (!pq.isEmpty()) {
            TravelState state = pq.poll();
            double known = bestTime.getOrDefault(state.id, Double.POSITIVE_INFINITY);
            if (state.time > known + 1e-12) {
                continue;
            }

            if (state.id == destination) {
                break;
            }

            List<Route> routes = adjacence.getOrDefault(state.id, Collections.emptyList());
            for (Route route : routes) {
                if (route.getDistance() <= 0) {
                    continue;
                }

                long voisinId = route.getIdDestination();
                if (!localisations.containsKey(voisinId)) {
                    continue;
                }

                double arrivalTime = state.time + (route.getDistance() / vVehicule);
                Double floodTime = tFloodById.get(voisinId);
                if (floodTime != null && arrivalTime >= floodTime) {
                    continue;
                }

                double old = bestTime.getOrDefault(voisinId, Double.POSITIVE_INFINITY);
                if (arrivalTime + 1e-12 < old) {
                    bestTime.put(voisinId, arrivalTime);
                    parent.put(voisinId, state.id);
                    pq.add(new TravelState(voisinId, arrivalTime));
                }
            }
        }

        if (!bestTime.containsKey(destination)) {
            return chemin;
        }

        Long courant = destination;
        while (courant != null) {
            Localisation loc = localisations.get(courant);
            if (loc == null) {
                return new ArrayDeque<>();
            }
            chemin.addFirst(loc);
            if (courant == depart) {
                break;
            }
            courant = parent.get(courant);
        }

        if (chemin.isEmpty() || chemin.peekFirst().getId() != depart) {
            return new ArrayDeque<>();
        }

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

