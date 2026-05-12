package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.BankCardDto;
import com.gestion.gestion_bus.models.BankCard;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.BankCardRepository;
import com.gestion.gestion_bus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class BankCardController {

    private final BankCardRepository cardRepository;
    private final UserRepository userRepository;

    // ── GET /cards/user/{userId}
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BankCardDto>> getCards(@PathVariable Long userId) {
        List<BankCard> cards = cardRepository.findByUserId(userId);
        return ResponseEntity.ok(cards.stream().map(this::toDto).collect(Collectors.toList()));
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<BankCardDto> addCard(@RequestBody BankCardDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));


        if (dto.isMain()) {
            unsetMainForUser(dto.getUserId());
        }


        List<BankCard> existing = cardRepository.findByUserId(dto.getUserId());
        boolean forceMain = existing.isEmpty();

        BankCard card = new BankCard();
        card.setLast4(dto.getLast4());
        card.setHolder(dto.getHolder());
        card.setExpiry(dto.getExpiry());
        card.setColorIndex(dto.getColorIndex());
        card.setMain(dto.isMain() || forceMain);
        card.setUser(user);

        BankCard saved = cardRepository.save(card);
        return ResponseEntity.ok(toDto(saved));
    }

    // ── PUT /cards/{id} ───────────────────────────────────────────────────────
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<BankCardDto> updateCard(@PathVariable Long id,
                                                  @RequestBody BankCardDto dto) {
        BankCard card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carte introuvable"));

        if (dto.isMain() && !card.isMain()) {
            unsetMainForUser(card.getUser().getId());
        }

        card.setHolder(dto.getHolder());
        card.setExpiry(dto.getExpiry());
        card.setColorIndex(dto.getColorIndex());
        card.setMain(dto.isMain());

        return ResponseEntity.ok(toDto(cardRepository.save(card)));
    }

    // ── PATCH /cards/{id}/set-main
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/set-main")
    public ResponseEntity<Void> setMain(@PathVariable Long id) {
        BankCard card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carte introuvable"));
        unsetMainForUser(card.getUser().getId());
        card.setMain(true);
        cardRepository.save(card);
        return ResponseEntity.ok().build();
    }

    // ── DELETE /cards/{id}
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        BankCard card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carte introuvable"));
        Long userId = card.getUser().getId();
        boolean wasMain = card.isMain();
        cardRepository.delete(card);

        // Si on supprime la carte principale, en attribuer une autre automatiquement
        if (wasMain) {
            List<BankCard> remaining = cardRepository.findByUserId(userId);
            if (!remaining.isEmpty()) {
                remaining.get(0).setMain(true);
                cardRepository.save(remaining.get(0));
            }
        }
        return ResponseEntity.ok().build();
    }



    private void unsetMainForUser(Long userId) {
        cardRepository.findByUserIdAndIsMainTrue(userId).ifPresent(c -> {
            c.setMain(false);
            cardRepository.save(c);
        });
    }

    private BankCardDto toDto(BankCard c) {
        BankCardDto dto = new BankCardDto();
        dto.setId(c.getId());
        dto.setLast4(c.getLast4());
        dto.setHolder(c.getHolder());
        dto.setExpiry(c.getExpiry());
        dto.setColorIndex(c.getColorIndex());
        dto.setMain(c.isMain());
        dto.setUserId(c.getUser().getId());
        return dto;
    }
}
