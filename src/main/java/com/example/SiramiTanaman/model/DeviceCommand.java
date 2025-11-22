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
    @Column(name = "command_type", nullable = false)
    private CommandType commandType; 

    @Column(name = "value")
    private Float value;

    @Column(name = "override_mode", nullable = false)
    private boolean overrideMode;

    @Column(name = "sent")
    private boolean sent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

}
