package tn.esprit.recommendstyle.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Recommendation {
    public Map<String, Object> generateOutfit(String emotion, String weather) {
        Map<String, Object> outfit = new HashMap<>();
        outfit.put("title", "Look personnalisé");

        List<Map<String, String>> items = new ArrayList<>();

        if ("Happy".equals(emotion) && "Clear".equals(weather)) {
            items.add(Map.of("icon", "👕", "name", "T-shirt", "description", "Couleurs vives"));
            items.add(Map.of("icon", "🩳", "name", "Short", "description", "Coton léger"));
            items.add(Map.of("icon", "👟", "name", "Baskets", "description", "Respirantes"));
        } else if ("Sad".equals(emotion) || "Fear".equals(emotion)) {
            items.add(Map.of("icon", "🧥", "name", "Pull", "description", "En laine douce"));
            items.add(Map.of("icon", "👖", "name", "Jean", "description", "Confortable"));
            items.add(Map.of("icon", "🥿", "name", "Chaussures", "description", "Faciles à porter"));
        } else if ("Rain".equals(weather)) {
            items.add(Map.of("icon", "🧥", "name", "Imperméable", "description", "Résistant à l’eau"));
            items.add(Map.of("icon", "👢", "name", "Bottes", "description", "Anti-glisse"));
            items.add(Map.of("icon", "☂️", "name", "Parapluie", "description", "Compact et coloré"));
        } else {
            items.add(Map.of("icon", "👕", "name", "Haut", "description", "Classique"));
            items.add(Map.of("icon", "👖", "name", "Bas", "description", "Passe-partout"));
            items.add(Map.of("icon", "👟", "name", "Chaussures", "description", "Standard"));
        }

        outfit.put("items", items);
        outfit.put("score", 85);
        return outfit;
    }
}
