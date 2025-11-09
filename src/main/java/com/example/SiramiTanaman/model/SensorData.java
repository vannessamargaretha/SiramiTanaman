package com.example.SiramiTanaman.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "soil_moisture")
    private Float soilMoisture;

    @Column(name = "air_humidity")
    private Float airHumidity;

    @Column(name = "light_intensity")
    private Float lightIntensity;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        timestamp = LocalDateTime.now();
    }
}
