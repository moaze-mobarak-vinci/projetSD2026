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

    private class EtatEau {
        long id;
        double temps;
        double vitesse;

        EtatEau(long id, double temps, double vitesse) {
            this.id = id;
            this.temps = temps;
            this.vitesse = vitesse;
        }
    }

    private class EtatTrajet {
        long id;
        double temps;

        EtatTrajet(long id, double temps) {
            this.id = id;
            this.temps = temps;
        }
    }

    public Localisation[] determinerZoneInondee(long[] idsDepart, double epsilon) {
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
            List<Route> routes = adjacence.getOrDefault(courant.getId(), new ArrayList<>());

            for (Route route : routes) {
                Localisation voisin = localisations.get(route.getIdDestination());

                if (voisin != null && !visites.contains(voisin.getId()) && voisin.getAltitude() <= courant.getAltitude() + epsilon) {
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
        Map<Long, Long> parents = new HashMap<>();

        class NoeudDistance {
            long id;
            double distance;

            NoeudDistance(long id, double distance) {
                this.id = id;
                this.distance = distance;
            }
        }

        PriorityQueue<NoeudDistance> filePriorite = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));

        for (Long id : localisations.keySet()) {
            distances.put(id, Double.MAX_VALUE);
        }

        distances.put(depart, 0.0);
        filePriorite.add(new NoeudDistance(depart, 0.0));

        while (!filePriorite.isEmpty()) {
            NoeudDistance etatCourant = filePriorite.poll();
            long courant = etatCourant.id;

            if (etatCourant.distance > distances.get(courant)) {
                continue;
            }

            if (courant == destination) {
                break;
            }

            List<Route> routes = adjacence.getOrDefault(courant, new ArrayList<>());

            for (Route route : routes) {
                long voisin = route.getIdDestination();

                if (idsInondes.contains(voisin) || !localisations.containsKey(voisin)) {
                    continue;
                }

                double nouvelleDist = distances.get(courant) + route.getDistance();

                if (nouvelleDist < distances.get(voisin)) {
                    distances.put(voisin, nouvelleDist);
                    parents.put(voisin, courant);
                    filePriorite.add(new NoeudDistance(voisin, nouvelleDist));
                }
            }
        }

        if (distances.get(destination) == Double.MAX_VALUE) {
            return chemin;
        }

        Long courant = destination;
        while (courant != null) {
            Localisation localisation = localisations.get(courant);
            if (localisation != null) {
                chemin.addFirst(localisation);
            }
            if (courant == depart) {
                break;
            }
            courant = parents.get(courant);
        }

        if (chemin.isEmpty() || chemin.peekFirst().getId() != depart) {
            return new ArrayDeque<>();
        }

        return chemin;
    }

    public Map<Localisation, Double> determinerChronologieDeLaCrue(long[] sources, double vWaterInit, double k) {
        Map<Localisation, Double> resultat = new LinkedHashMap<>();
        if (sources == null || sources.length == 0) return resultat;

        Map<Long, Double> meilleursTemps = new HashMap<>();
        PriorityQueue<EtatEau> filePriorite = new PriorityQueue<>(Comparator.comparingDouble(e -> e.temps));

        for (long idSource : sources) {
            if (localisations.containsKey(idSource)) {
                meilleursTemps.put(idSource, 0.0);
                filePriorite.add(new EtatEau(idSource, 0.0, vWaterInit));
            }
        }

        while (!filePriorite.isEmpty()) {
            EtatEau etat = filePriorite.poll();
            double tempsConnu = meilleursTemps.getOrDefault(etat.id, Double.MAX_VALUE);

            if (etat.temps > tempsConnu) {
                continue;
            }

            Localisation courant = localisations.get(etat.id);
            if (courant == null) continue;

            List<Route> routes = adjacence.getOrDefault(etat.id, new ArrayList<>());
            for (Route route : routes) {
                Localisation voisin = localisations.get(route.getIdDestination());
                if (voisin == null || route.getDistance() <= 0) continue;

                double pente = (courant.getAltitude() - voisin.getAltitude()) / route.getDistance();
                double vitesseSuivante = etat.vitesse + (k * pente);

                if (vitesseSuivante <= 0) continue;

                double tempsTrajet = route.getDistance() / vitesseSuivante;
                double nouveauTemps = etat.temps + tempsTrajet;
                long idVoisin = voisin.getId();
                double ancienTemps = meilleursTemps.getOrDefault(idVoisin, Double.MAX_VALUE);

                if (nouveauTemps < ancienTemps) {
                    meilleursTemps.put(idVoisin, nouveauTemps);
                    filePriorite.add(new EtatEau(idVoisin, nouveauTemps, vitesseSuivante));
                }
            }
        }

        List<Map.Entry<Long, Double>> entrees = new ArrayList<>(meilleursTemps.entrySet());
        entrees.sort((a, b) -> {
            int comparaisonTemps = Double.compare(a.getValue(), b.getValue());
            if (comparaisonTemps != 0) return comparaisonTemps;
            return Long.compare(a.getKey(), b.getKey());
        });

        for (Map.Entry<Long, Double> entree : entrees) {
            Localisation loc = localisations.get(entree.getKey());
            if (loc != null) {
                resultat.put(loc, entree.getValue());
            }
        }

        return resultat;
    }

    public Deque<Localisation> trouverCheminDEvacuationLePlusCourt(long depart, long destination, double vVehicule, Map<Localisation, Double> tFlood) {
        Deque<Localisation> chemin = new ArrayDeque<>();
        if (vVehicule <= 0) return chemin;

        Localisation locDepart = localisations.get(depart);
        Localisation locDestination = localisations.get(destination);
        if (locDepart == null || locDestination == null) return chemin;

        Map<Long, Double> tempsCrueParId = new HashMap<>();
        if (tFlood != null) {
            for (Map.Entry<Localisation, Double> entree : tFlood.entrySet()) {
                if (entree.getKey() != null && entree.getValue() != null) {
                    tempsCrueParId.put(entree.getKey().getId(), entree.getValue());
                }
            }
        }

        Double tempsCrueDepart = tempsCrueParId.get(depart);
        if (tempsCrueDepart != null && tempsCrueDepart <= 0.0) {
            return chemin;
        }

        Map<Long, Double> meilleursTemps = new HashMap<>();
        Map<Long, Long> parents = new HashMap<>();
        PriorityQueue<EtatTrajet> filePriorite = new PriorityQueue<>(Comparator.comparingDouble(e -> e.temps));

        meilleursTemps.put(depart, 0.0);
        filePriorite.add(new EtatTrajet(depart, 0.0));

        while (!filePriorite.isEmpty()) {
            EtatTrajet etat = filePriorite.poll();
            double tempsConnu = meilleursTemps.getOrDefault(etat.id, Double.MAX_VALUE);

            if (etat.temps > tempsConnu) {
                continue;
            }

            if (etat.id == destination) {
                break;
            }

            List<Route> routes = adjacence.getOrDefault(etat.id, new ArrayList<>());
            for (Route route : routes) {
                if (route.getDistance() <= 0) continue;

                long idVoisin = route.getIdDestination();
                if (!localisations.containsKey(idVoisin)) continue;

                double tempsArrivee = etat.temps + (route.getDistance() / vVehicule);
                Double tempsCrue = tempsCrueParId.get(idVoisin);

                if (tempsCrue != null && tempsArrivee >= tempsCrue) {
                    continue;
                }

                double ancienTemps = meilleursTemps.getOrDefault(idVoisin, Double.MAX_VALUE);
                if (tempsArrivee < ancienTemps) {
                    meilleursTemps.put(idVoisin, tempsArrivee);
                    parents.put(idVoisin, etat.id);
                    filePriorite.add(new EtatTrajet(idVoisin, tempsArrivee));
                }
            }
        }

        if (!meilleursTemps.containsKey(destination)) {
            return chemin;
        }

        Long courant = destination;
        while (courant != null) {
            Localisation loc = localisations.get(courant);
            if (loc != null) {
                chemin.addFirst(loc);
            }
            if (courant == depart) {
                break;
            }
            courant = parents.get(courant);
        }

        if (chemin.isEmpty() || chemin.peekFirst().getId() != depart) {
            return new ArrayDeque<>();
        }

        return chemin;
    }
}