package com.example.SiramiTanaman.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "device_custom_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCustomSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    private Integer soilMin;
    private Integer soilMax;
    private Integer tempPump;
    private Integer tempLightOff;
    private Integer luxMin;
    private Integer luxMax;
    private Integer lightDuration;
    private Integer lightInterval;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
