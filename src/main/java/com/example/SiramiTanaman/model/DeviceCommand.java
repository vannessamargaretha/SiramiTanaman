package com.example.SiramiTanaman.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_command")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type")
    private CommandType commandType;

    private Float value; // 0–100

    private Boolean overrideMode;

    private Boolean sent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (overrideMode == null) overrideMode = false;
        if (sent == null) sent = false;
    }
}
