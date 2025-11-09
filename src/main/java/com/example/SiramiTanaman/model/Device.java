package com.example.SiramiTanaman.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Bi-directional relations
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SensorData> sensorDataList;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeviceCommand> commands;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
