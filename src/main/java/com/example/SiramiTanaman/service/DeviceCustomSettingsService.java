package com.example.SiramiTanaman.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.SiramiTanaman.model.*;
import com.example.SiramiTanaman.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@Service
public class DeviceCustomSettingsService {

    @Autowired
    private DeviceCustomSettingsRepository repository;

    @Autowired
    private DeviceRepository deviceRepository;

   
    public DeviceCustomSettings saveOrUpdate(Long deviceId, DeviceCustomSettings settings) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        settings.setDevice(device);

        
        Optional<DeviceCustomSettings> latestOpt = getLatestByDeviceId(deviceId);

        if (latestOpt.isPresent()) {
            DeviceCustomSettings latest = latestOpt.get();
            
            latest.setSoilMin(settings.getSoilMin());
            latest.setSoilMax(settings.getSoilMax());
            latest.setTempPump(settings.getTempPump());
            latest.setTempLightOff(settings.getTempLightOff());
            latest.setLuxMin(settings.getLuxMin());
            latest.setLuxMax(settings.getLuxMax());
            latest.setLightDuration(settings.getLightDuration());
            latest.setLightInterval(settings.getLightInterval());
            latest.setExpiresAt(settings.getExpiresAt()); 
            return repository.save(latest);
        } else {
            
            return repository.save(settings);
        }
    }

  
    public Optional<DeviceCustomSettings> getLatestByDeviceId(Long deviceId) {
        return repository.findTopByDeviceIdOrderByCreatedAtDesc(deviceId)
                         .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()));
    }
}
