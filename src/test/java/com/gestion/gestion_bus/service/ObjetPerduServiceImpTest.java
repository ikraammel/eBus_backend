package com.gestion.gestion_bus.service;

import com.gestion.gestion_bus.dto.request.ObjetPerduRequestDto;
import com.gestion.gestion_bus.enums.*;
import com.gestion.gestion_bus.models.*;
import com.gestion.gestion_bus.repository.*;
import com.gestion.gestion_bus.mappers.ObjetPerduMapper;
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
class ObjetPerduServiceImpTest {
    @Mock ObjetPerduRepository objetPerduRepository;
    @Mock UserRepository userRepository;
    @Mock LigneRepository ligneRepository;
    @Mock BusRepository busRepository;
    @Mock ObjetPerduMapper objetPerduMapper;
    @Mock FileStorageService fileStorageService;
    @InjectMocks ObjetPerduServiceImp service;

    @ParameterizedTest @EnumSource(value=StatutObjet.class,names={"EN_ATTENTE","EN_ATTENTE_RECUPERATION","RECUPERE"})
    void collectionRequiresAvailableObject(StatutObjet status) {
        ObjetPerdu o=new ObjetPerdu();o.setStatut(status);
        when(objetPerduRepository.findById(1L)).thenReturn(Optional.of(o));
        assertThatThrownBy(()->service.signalerRecupere(1L)).isInstanceOf(IllegalStateException.class);
        verify(objetPerduRepository,never()).save(any());
    }
    @Test void availableObjectMovesToAwaitingCollection() {
        ObjetPerdu o=new ObjetPerdu();o.setStatut(StatutObjet.DISPONIBLE);
        when(objetPerduRepository.findById(1L)).thenReturn(Optional.of(o));
        when(objetPerduRepository.save(o)).thenReturn(o);
        service.signalerRecupere(1L);
        assertThat(o.getStatut()).isEqualTo(StatutObjet.EN_ATTENTE_RECUPERATION);
        verify(objetPerduRepository).save(o);
    }
    @Test void missingObjectCannotBeCollected() {
        assertThatThrownBy(()->service.signalerRecupere(1L)).isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
        verify(objetPerduRepository,never()).save(any());
    }
    @Test void declarationDefaultsToPendingAndCurrentDate() {
        ObjetPerduRequestDto dto=ObjetPerduRequestDto.builder().nom("QA object").description("Synthetic").ligneId(1L).type(TypeAnnonce.PERTE).build();
        when(ligneRepository.findById(1L)).thenReturn(Optional.of(new Ligne()));
        when(objetPerduRepository.save(any())).thenAnswer(i->i.getArgument(0));
        LocalDateTime before=LocalDateTime.now();
        service.declare(dto,null);
        ArgumentCaptor<ObjetPerdu> saved=ArgumentCaptor.forClass(ObjetPerdu.class);
        verify(objetPerduRepository).save(saved.capture());
        assertThat(saved.getValue().getStatut()).isEqualTo(StatutObjet.EN_ATTENTE);
        assertThat(saved.getValue().getDateDeclaration()).isBetween(before,LocalDateTime.now());
        verifyNoInteractions(fileStorageService);
    }
    @Test void declarationRequiresExistingLineBeforePersistence() {
        ObjetPerduRequestDto dto=ObjetPerduRequestDto.builder().ligneId(999L).build();
        assertThatThrownBy(()->service.declare(dto,null)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(objetPerduRepository,fileStorageService);
    }
    @Test void deletionRequiresExistingObject() {
        assertThatThrownBy(()->service.delete(1L)).isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
        verify(objetPerduRepository,never()).deleteById(anyLong());
    }
    @Test void existingObjectCanBeDeleted() {
        when(objetPerduRepository.existsById(1L)).thenReturn(true);
        service.delete(1L);verify(objetPerduRepository).deleteById(1L);
    }
}
