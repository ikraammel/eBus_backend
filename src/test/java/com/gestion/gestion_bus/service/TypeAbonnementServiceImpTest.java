package com.gestion.gestion_bus.service;

import com.gestion.gestion_bus.controller.TypeAbonnementController;
import com.gestion.gestion_bus.models.TypeAbonnement;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.TypeAbonnementRepository;
import com.gestion.gestion_bus.service.imp.TypeAbonnementServiceImp;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TypeAbonnementServiceImpTest {
    @Mock TypeAbonnementRepository repository;
    @InjectMocks TypeAbonnementServiceImp service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void expectPublicOffers() {
        TypeAbonnement offer = new TypeAbonnement();
        offer.setNom("Public test offer");
        when(repository.findByIsStudent(false)).thenReturn(List.of(offer));
        assertThat(service.getTypesForCurrentUser()).containsExactly(offer);
        verify(repository).findByIsStudent(false);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void anonymousPrincipalGetsPublicOffersWithoutCastingToUser() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
            "unit-test-key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        expectPublicOffers();
    }

    @Test
    void absentAuthenticationGetsPublicOffers() {
        expectPublicOffers();
    }

    @Test
    void unrelatedPrincipalCannotGetStudentOffers() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            "external-principal", null, AuthorityUtils.createAuthorityList("ROLE_USER")));
        expectPublicOffers();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void authenticatedNonStudentGetsPublicOffers(String cne) {
        User user = new User();
        user.setCne(cne);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
        expectPublicOffers();
    }

    @Test
    void authenticatedStudentKeepsExistingCatalogAccess() {
        User student = new User();
        student.setCne("SYNTHETIC-CNE");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            student, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
        TypeAbonnement publicOffer = new TypeAbonnement();
        TypeAbonnement studentOffer = new TypeAbonnement();
        studentOffer.setStudent(true);
        when(repository.findAll()).thenReturn(List.of(publicOffer, studentOffer));
        assertThat(service.getTypesForCurrentUser()).containsExactly(publicOffer, studentOffer);
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void unauthenticatedStudentPrincipalCannotGetStudentOffers() {
        User student = new User();
        student.setCne("SYNTHETIC-CNE");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(student, null));
        expectPublicOffers();
    }

    @Test
    void publicGetReturns200ThroughTheControllerWithAnonymousSecurityFilter() throws Exception {
        TypeAbonnement offer = new TypeAbonnement();
        offer.setNom("Public test offer");
        when(repository.findByIsStudent(false)).thenReturn(List.of(offer));
        MockMvcBuilders.standaloneSetup(new TypeAbonnementController(service))
            .addFilters(new AnonymousAuthenticationFilter("unit-test-key"))
            .build()
            .perform(get("/types-abonnement"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nom").value("Public test offer"))
            .andExpect(jsonPath("$[0].student").value(false));
        verify(repository).findByIsStudent(false);
        verifyNoMoreInteractions(repository);
    }
}
