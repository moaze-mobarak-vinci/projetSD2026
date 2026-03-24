package be.vinci.resilience;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GraphLoader {

    public static void chargerGraphe(Graph graphe, String fichierNoeuds, String fichierArcs) {

        // 1. Lecture des Noeuds (Localisations)
        try (BufferedReader br = new BufferedReader(new FileReader(fichierNoeuds))) {
            String ligne;
            br.readLine(); // On saute la ligne d'entête (id,name,lat,lon,alt)

            while ((ligne = br.readLine()) != null) {
                String[] v = ligne.split(",");

                // On convertit l'ID en long et les chiffres en double
                Localisation loc = new Localisation(
                        Long.parseLong(v[0]),     // ID (index 0)
                        Double.parseDouble(v[2]), // Latitude (index 2)
                        Double.parseDouble(v[3]), // Longitude (index 3)
                        v[1],                     // Nom (index 1)
                        Double.parseDouble(v[4])  // Altitude (index 4)
                );
                graphe.addLocalisation(loc);
            }
            System.out.println("Chargement des nœuds terminé.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erreur lors du chargement des nœuds : " + e.getMessage());
        }

        // 2. Lecture des Arcs (Routes)
        try (BufferedReader br = new BufferedReader(new FileReader(fichierArcs))) {
            String ligne;
            br.readLine(); // On saute la ligne d'entête (source,target,dist,street_name)

            while ((ligne = br.readLine()) != null) {
                String[] v = ligne.split(",");

                // On convertit les IDs source et target en long
                Route route = new Route(
                        Long.parseLong(v[0]),     // Source (index 0)
                        Long.parseLong(v[1]),     // Target (index 1)
                        Double.parseDouble(v[2]), // Distance (index 2)
                        v[3]                      // Nom de rue (index 3)
                );
                graphe.addRoute(route);
            }
            System.out.println("Chargement des arcs terminé.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erreur lors du chargement des arcs : " + e.getMessage());
        }
    }
}