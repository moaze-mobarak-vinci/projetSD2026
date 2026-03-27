package be.vinci.resilience;

import java.util.*;

public class Graph {
    private final Map<Long, Localisation> localisations = new HashMap<>();
    private final Map<Long, List<Route>> adjacence = new HashMap<>();

    public Graph(String fichierNoeuds, String fichierArcs) {
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
            List<Route> routes = adjacence.getOrDefault(courant.getId(), Collections.emptyList());

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

        Map<Long, Double> distances = new HashMap<>();
        Map<Long, Long> parent = new HashMap<>();

        class NodeDistance {
            long id;
            double distance;

            NodeDistance(long id, double distance) {
                this.id = id;
                this.distance = distance;
            }
        }

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));

        for (Long id : localisations.keySet()) {
            distances.put(id, Double.POSITIVE_INFINITY);
        }

        distances.put(depart, 0.0);
        pq.add(new NodeDistance(depart, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance courantEtat = pq.poll();
            long courant = courantEtat.id;

            if (courantEtat.distance > distances.get(courant)) {
                continue;
            }

            if (courant == destination) {
                break;
            }

            List<Route> routes = adjacence.getOrDefault(courant, Collections.emptyList());

            for (Route route : routes) {
                long voisin = route.getIdDestination();

                if (idsInondes.contains(voisin)) {
                    continue;
                }

                if (!localisations.containsKey(voisin)) {
                    continue;
                }

                double nouvelleDistance = distances.get(courant) + route.getDistance();

                if (nouvelleDistance < distances.get(voisin)) {
                    distances.put(voisin, nouvelleDistance);
                    parent.put(voisin, courant);
                    pq.add(new NodeDistance(voisin, nouvelleDistance));
                }
            }
        }

        if (distances.get(destination) == Double.POSITIVE_INFINITY) {
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
}