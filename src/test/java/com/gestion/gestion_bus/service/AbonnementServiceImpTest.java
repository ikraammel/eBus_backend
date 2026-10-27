package com.gestion.gestion_bus.service;

import com.gestion.gestion_bus.enums.*;
import com.gestion.gestion_bus.models.*;
import com.gestion.gestion_bus.repository.*;
import com.gestion.gestion_bus.service.imp.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbonnementServiceImpTest {
    @Mock AbonnementRepository abonnementRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock UserRepository userRepository;
    @Mock TypeAbonnementRepository typeAbonnementRepository;
    @Mock StripeService stripeService;
    @InjectMocks AbonnementServiceImp service;
    User user;
    TypeAbonnement type;

    @BeforeEach void fixtures() {
        user = new User(); user.setId(2L); user.setEmail("qa@example.invalid"); user.setStatusDossier(StatusDossier.VALIDE);
        type = new TypeAbonnement(); type.setId(1L); type.setActif(true); type.setStripePriceId("price_qa");
    }
    Abonnement subscription(int dayOffset) {
        Abonnement a = new Abonnement(); a.setId(7L); a.setUser(user); a.setTypeAbonnement(type);
        a.setStatus(StatusAbonnement.ACTIF); a.setDateFin(LocalDateTime.now().plusDays(dayOffset));
        return a;
    }
    void eligible() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(typeAbonnementRepository.findById(1L)).thenReturn(Optional.of(type));
    }
    @ParameterizedTest @ValueSource(ints={-30,-1,0,1,30})
    void effectiveExpirationDoesNotMutateEntity(int dayOffset) {
        Abonnement a = subscription(dayOffset);
        assertThat(service.mapWithEffectiveStatus(a).getStatus()).isEqualTo(dayOffset<0?StatusAbonnement.EXPIRE:StatusAbonnement.ACTIF);
        assertThat(a.getStatus()).isEqualTo(StatusAbonnement.ACTIF);
        verifyNoInteractions(abonnementRepository);
    }
    @ParameterizedTest @EnumSource(value=StatusAbonnement.class,names={"EN_ATTENTE","REFUSE","EXPIRE"})
    void doesNotReplaceNonActiveStatus(StatusAbonnement status) {
        Abonnement a=subscription(-1); a.setStatus(status);
        assertThat(service.mapWithEffectiveStatus(a).getStatus()).isEqualTo(status);
    }
    @Test void nullEndDateIsNotExpired() {
        Abonnement a=subscription(1); a.setDateFin(null);
        assertThat(service.mapWithEffectiveStatus(a).getStatus()).isEqualTo(StatusAbonnement.ACTIF);
    }
    @ParameterizedTest @NullSource @EnumSource(value=StatusDossier.class,names={"EN_ATTENTE","REJETE"})
    void refusesIneligibleDossierBeforeStripe(StatusDossier status) {
        eligible(); user.setStatusDossier(status);
        assertThatThrownBy(()->service.createAbonnement(2L,1L)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(stripeService,abonnementRepository,paymentRepository);
    }
    @Test void missingUserDoesNotCallStripe() {
        assertThatThrownBy(()->service.createAbonnement(2L,1L)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(stripeService,typeAbonnementRepository);
    }
    @Test void missingTypeDoesNotCallStripe() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        assertThatThrownBy(()->service.createAbonnement(2L,1L)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(stripeService);
    }
    @Test void inactiveOfferDoesNotCallStripe() {
        eligible(); type.setActif(false);
        assertThatThrownBy(()->service.createAbonnement(2L,1L)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(stripeService);
    }
    @Test void eligibleAccountGetsCheckoutUrl() throws Exception {
        eligible(); when(stripeService.createCheckoutSession("price_qa",2L,1L,"qa@example.invalid")).thenReturn("https://checkout.stripe.com/test_fixture");
        assertThat(service.createAbonnement(2L,1L)).endsWith("test_fixture");
        verify(stripeService).createCheckoutSession("price_qa",2L,1L,"qa@example.invalid");
        verify(abonnementRepository,never()).save(any());
    }
    @ParameterizedTest @ValueSource(ints={0,1,30})
    void refusesAnExistingActiveSubscription(int days) {
        eligible(); when(abonnementRepository.findTopByUserIdAndStatusOrderByIdDesc(2L,StatusAbonnement.ACTIF)).thenReturn(Optional.of(subscription(days)));
        assertThatThrownBy(()->service.createAbonnement(2L,1L)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(stripeService);
    }
    @Test void expiredExistingSubscriptionIsPersistedBeforeNewCheckout() throws Exception {
        eligible(); Abonnement old=subscription(-1);
        when(abonnementRepository.findTopByUserIdAndStatusOrderByIdDesc(2L,StatusAbonnement.ACTIF)).thenReturn(Optional.of(old));
        service.createAbonnement(2L,1L);
        assertThat(old.getStatus()).isEqualTo(StatusAbonnement.EXPIRE);
        InOrder order=inOrder(abonnementRepository,stripeService);
        order.verify(abonnementRepository).findTopByUserIdAndStatusOrderByIdDesc(2L,StatusAbonnement.ACTIF);
        order.verify(abonnementRepository).save(old);
        order.verify(stripeService).createCheckoutSession("price_qa",2L,1L,"qa@example.invalid");
    }
    @Test void forbidsManualActivationWithoutAnyPersistence() {
        assertThatThrownBy(()->service.updateStatus(7L,StatusAbonnement.ACTIF)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(abonnementRepository,paymentRepository);
    }
    @ParameterizedTest @EnumSource(value=StatusAbonnement.class,names={"EN_ATTENTE","REFUSE","EXPIRE"})
    void statusAndPaymentStayConsistent(StatusAbonnement status) {
        Abonnement a=subscription(1); Payment p=new Payment();
        when(abonnementRepository.findById(7L)).thenReturn(Optional.of(a));
        when(paymentRepository.findByAbonnementId(7L)).thenReturn(Optional.of(p));
        assertThat(service.updateStatus(7L,status).getStatus()).isEqualTo(status);
        assertThat(p.getStatut()).isEqualTo(status.name());
        verify(abonnementRepository).save(a); verify(paymentRepository).save(p);
    }
    @Test void statusChangeWorksWithoutPayment() {
        when(abonnementRepository.findById(7L)).thenReturn(Optional.of(subscription(1)));
        service.updateStatus(7L,StatusAbonnement.REFUSE);
        verify(paymentRepository,never()).save(any());
    }
    @Test void missingSubscriptionCannotChangeStatus() {
        assertThatThrownBy(()->service.updateStatus(7L,StatusAbonnement.REFUSE)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(paymentRepository);
    }
    @Test void readsLatestSubscriptionWithEffectiveStatus() {
        when(abonnementRepository.findTopByUserIdOrderByIdDesc(2L)).thenReturn(Optional.of(subscription(-1)));
        assertThat(service.getAbonnementByUser(2L).getStatus()).isEqualTo(StatusAbonnement.EXPIRE);
    }
    @Test void missingLatestSubscriptionIsExplicitFailure() {
        assertThatThrownBy(()->service.getAbonnementByUser(2L)).isInstanceOf(RuntimeException.class);
    }
    @Test void readsSubscriptionById() {
        when(abonnementRepository.findById(7L)).thenReturn(Optional.of(subscription(1)));
        assertThat(service.getAbonnementById(7L).getId()).isEqualTo(7L);
    }
    @Test void missingSubscriptionByIdIsExplicitFailure() {
        assertThatThrownBy(()->service.getAbonnementById(7L)).isInstanceOf(RuntimeException.class);
    }
    @Test void listAndHistoryUseEffectiveExpiration() {
        when(abonnementRepository.findAll()).thenReturn(List.of(subscription(-1),subscription(1)));
        when(abonnementRepository.findAllByUserIdOrderByIdDesc(2L)).thenReturn(List.of(subscription(-1)));
        assertThat(service.getAllAbonnements()).extracting("status").containsExactly(StatusAbonnement.EXPIRE,StatusAbonnement.ACTIF);
        assertThat(service.getHistoriqueByUser(2L)).extracting("status").containsExactly(StatusAbonnement.EXPIRE);
    }
    @Test void deletionDelegatesToRepository() {service.deleteAbonnement(7L);verify(abonnementRepository).deleteById(7L);}
}
