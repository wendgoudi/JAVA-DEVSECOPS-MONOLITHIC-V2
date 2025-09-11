package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class PersonController {

    private final List<Map<String, String>> personnes = new ArrayList<>();

    @PostMapping("/ajouter")
    public Map<String, Object> ajouterPersonne(@RequestBody Map<String, String> personne) {
        personnes.add(personne);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Personne ajoutée avec succès");
        response.put("personnes", personnes);
        return response;
    }

    @GetMapping("/liste")
    public List<Map<String, String>> getListe() {
        return personnes;
    }
}
