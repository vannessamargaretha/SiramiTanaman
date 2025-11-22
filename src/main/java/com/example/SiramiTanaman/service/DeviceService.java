package com.example.SiramiTanaman.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.SiramiTanaman.model.Device;
import com.example.SiramiTanaman.model.User;
import com.example.SiramiTanaman.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public List<Device> getDevicesByOwner(User owner) {
        return deviceRepository.findByOwner(owner);
    }

    public Device addDevice(Device device) {
        device.setStatus(com.example.SiramiTanaman.model.DeviceStatus.offline);
        return deviceRepository.save(device);
    }

    public Optional<Device> getDevice(Long id) {
        return deviceRepository.findById(id);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public void updateDeviceStatus(Long id, boolean isOnline) {
        deviceRepository.findById(id).ifPresent(d -> {
            d.setStatus(isOnline ? com.example.SiramiTanaman.model.DeviceStatus.online : com.example.SiramiTanaman.model.DeviceStatus.offline);
            deviceRepository.save(d);
        });
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    public void updateMode(Long deviceId, boolean manualMode) {
    Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new RuntimeException("Device not found"));

    device.setManualMode(manualMode);
    deviceRepository.save(device);
}

}
