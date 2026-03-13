package be.vinci.resilience;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResilienceGraph {

    // On utilise des Maps pour stocker les données : ID -> Objet
        // C'est efficace pour la recherche rapide parmi 1 000 000 de noeuds
        private Map<String, Localisation> localisations;
        private Map<String, List<Route>> adjacence;

        public ResilienceGraph() {
            this.localisations = new HashMap<>();
            this.adjacence = new HashMap<>();
        }

        /**
         * Méthode pour ajouter une localisation au graphe
         */
        public void addLocalisation(Localisation loc) {
            localisations.put(loc.getId(), loc);
            adjacence.putIfAbsent(loc.getId(), new ArrayList<>());
        }

        /**
         * Méthode pour ajouter une route au graphe
         */
        public void addRoute(Route route) {
            if (adjacence.containsKey(route.getIdOrigine())) {
                adjacence.get(route.getIdOrigine()).add(route);
            }
        }

        /**
         * ALGORITHME 3 : Chronologie de la crue (Consigne Page 4)
         * Calcule l'instant t_flood pour chaque nœud atteint par l'eau.
         */
        public Map<String, Double> determinerChronologieDeLaCrue(
                String noeudDepart,
                double altitudeInitiale,
                double debitInitial) {

            Map<String, Double> chronologie = new HashMap<>();

            // TODO: Implémenter la propagation de l'eau avec vitesse variable V_water
            // Indice pour plus tard : Utiliser une file de priorité (PriorityQueue)

            return chronologie;
        }

        /**
         * ALGORITHME 4 : Évacuation dynamique (Consigne Page 5)
         * Trouve le chemin le plus rapide vers la destination avant l'inondation.
         */
        public List<String> trouverCheminDEvacuationLePlusCourt(
                String depart,
                String destination,
                Map<String, Double> chronologieCrue) {

            List<String> chemin = new ArrayList<>();

            // TODO: Implémenter Dijkstra modifié
            // Condition critique : TempsArrivéeNoeud < chronologieCrue.get(noeud)

            return chemin;
        }
}
