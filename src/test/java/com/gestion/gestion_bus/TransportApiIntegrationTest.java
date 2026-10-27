package com.gestion.gestion_bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.gestion_bus.models.*;
import com.gestion.gestion_bus.enums.StatusDossier;
import com.gestion.gestion_bus.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransportApiIntegrationTest extends AbstractDatabaseTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired LigneRepository lignes;
    @Autowired DossierRepository dossiers;
    @Autowired ObjetPerduRepository objets;
    @Autowired PasswordEncoder encoder;
    User user;
    Ligne line;

    @BeforeEach void syntheticFixtures() {
        Role role=new Role();role.setName("USER");role=roles.save(role);
        user=new User();user.setRole(role);user.setEmail("integration-"+UUID.randomUUID()+"@example.invalid");
        user.setNom("QA");user.setPrenom("Integration");user.setDateNaissance("1995-01-01");
        user.setPassword(encoder.encode("LocalQa4927!"));user.setStatusDossier(StatusDossier.VALIDE);
        user=users.saveAndFlush(user);
        Dossier dossier=new Dossier();dossier.setUser(user);dossier.setStatus(StatusDossier.VALIDE);dossiers.saveAndFlush(dossier);
        line=new Ligne();line.setNumero("QA-"+UUID.randomUUID());line.setStartPoint("Start");line.setEndPoint("End");
        line.setTrajets(new ArrayList<>());line.setBus(new ArrayList<>());line.setReclamations(new ArrayList<>());
        line=lignes.saveAndFlush(line);
    }
    String token() throws Exception {
        String response=mvc.perform(post("/users/login").contentType("application/json")
                .content(json.writeValueAsString(Map.of("email",user.getEmail(),"password","LocalQa4927!"))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token=json.readTree(response).path("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }
    @Test void healthNeedsNoToken() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(content().string("UP"));
    }
    @Test void anonymousAccountListIsDenied() throws Exception {
        mvc.perform(get("/users")).andExpect(status().isForbidden());
    }
    @Test void realJwtReadsPersistedOwnProfileAndDossier() throws Exception {
        String authorization="Bearer "+token();
        mvc.perform(get("/users/"+user.getId()).header("Authorization",authorization))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/users/me/dossier").param("id",user.getId().toString()).header("Authorization",authorization))
            .andExpect(status().isOk()).andExpect(jsonPath("$.statusDossier").value("VALIDE"));
    }
    @Test void declarationPersistsAndOwnDeletionRemovesIt() throws Exception {
        String authorization="Bearer "+token();
        String response=mvc.perform(multipart("/api/objets/declaration")
                .param("nom","QA bag").param("description","Synthetic integration object")
                .param("ligneId",line.getId().toString()).param("userId",user.getId().toString())
                .param("type","PERTE").param("contact","qa@example.invalid").header("Authorization",authorization))
            .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
            .andReturn().getResponse().getContentAsString();
        long id=json.readTree(response).path("id").asLong();
        assertThat(objets.findById(id)).isPresent();
        assertThat(objets.findById(id).orElseThrow().getUser().getId()).isEqualTo(user.getId());
        mvc.perform(delete("/api/objets/"+id).header("Authorization",authorization)).andExpect(status().isNoContent());
        assertThat(objets.existsById(id)).isFalse();
    }
}
