package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
public class PersonController {

    @Autowired
    private PersonRepository personRepository;

    // Page de login
    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        if ("admin".equals(username) && "1234".equals(password)) {
            model.addAttribute("personnes", personRepository.findAll());
            return "personnes";
        } else {
            model.addAttribute("error", "Identifiants incorrects");
            return "login";
        }
    }

    // Ajouter une personne
    @PostMapping("/ajouter")
    public String ajouterPersonne(@RequestParam String nom,
                                  @RequestParam String prenom,
                                  @RequestParam String email,
                                  @RequestParam String departement,
                                  @RequestParam String fonction,
                                  @RequestParam("date") String dateEmbauche,
                                  Model model) {
        LocalDate date = LocalDate.parse(dateEmbauche);
        personRepository.save(new Person(nom, prenom, email, departement, fonction, date));
        model.addAttribute("personnes", personRepository.findAll());
        return "personnes";
    }
}
