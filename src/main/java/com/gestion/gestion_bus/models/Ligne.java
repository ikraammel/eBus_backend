package com.gestion.gestion_bus.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ligne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numero;
    private double distance;

    @OneToMany(mappedBy = "ligne",cascade = CascadeType.ALL)
    private List<Bus> bus;

    private String startPoint;
    private String endPoint;

//    @ManyToMany
//    @JoinTable(
//            name = "ligne_station",
//            joinColumns = @JoinColumn(name = "ligne_id"),
//            inverseJoinColumns = @JoinColumn(name = "station_id")
//    )
//    private List<Station> stations;

    @OneToMany(mappedBy = "ligne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneStation> trajets;
}
