package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.StatutObjet;
import com.gestion.gestion_bus.enums.TypeAnnonce;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "objets_perdus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjetPerdu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nom;

	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne
	@JoinColumn(name = "ligne_id")
	private Ligne ligne;

	private String imageUrl;

	private String contact;

	@Enumerated(EnumType.STRING)
	@Builder.Default

	@Column(nullable = false, columnDefinition = "varchar(255) default 'EN_ATTENTE'")
	private StatutObjet statut = StatutObjet.EN_ATTENTE;

	@Enumerated(EnumType.STRING)
	@Builder.Default

	@Column(nullable = false, columnDefinition = "varchar(255)")
	private TypeAnnonce type = TypeAnnonce.PERTE;

	@Builder.Default
	@Column(nullable = false)
	private LocalDateTime dateDeclaration = LocalDateTime.now();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;


	@PrePersist
	protected void onCreate() {
		if (dateDeclaration == null) dateDeclaration = LocalDateTime.now();
		if (statut == null) statut = StatutObjet.EN_ATTENTE;
	}
}
