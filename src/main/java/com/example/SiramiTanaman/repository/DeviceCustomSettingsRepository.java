package com.example.SiramiTanaman.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.example.SiramiTanaman.model.*;

@Repository
public interface DeviceCustomSettingsRepository extends JpaRepository<DeviceCustomSettings, Long> {

   
    Optional<DeviceCustomSettings> findTopByDeviceIdOrderByCreatedAtDesc(Long deviceId);

    
    @Query("SELECT d FROM DeviceCustomSettings d WHERE d.device.id = :deviceId AND (d.expiresAt IS NULL OR d.expiresAt > CURRENT_TIMESTAMP) ORDER BY d.createdAt DESC")
    Optional<DeviceCustomSettings> findTopByDeviceIdAndNotExpired(@Param("deviceId") Long deviceId);
}
