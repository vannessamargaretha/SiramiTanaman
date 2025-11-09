package com.example.SiramiTanaman.controller;

import com.example.SiramiTanaman.model.*;
import com.example.SiramiTanaman.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final DeviceService deviceService;
    private final SensorDataService sensorDataService;
    private final DeviceCommandService commandService;

    // === IoT sends sensor data ===
    @PostMapping("/sensor/update")
    public Map<String, Object> receiveSensorData(@RequestBody Map<String, Object> payload) {
        Long deviceId = ((Number) payload.get("deviceId")).longValue();
        Float soil = ((Number) payload.get("soilMoisture")).floatValue();
        Float air = ((Number) payload.get("airHumidity")).floatValue();
        Float light = ((Number) payload.get("lightIntensity")).floatValue();

        Device device = deviceService.getDevice(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        SensorData data = SensorData.builder()
                .device(device)
                .soilMoisture(soil)
                .airHumidity(air)
                .lightIntensity(light)
                .build();

        sensorDataService.saveSensorData(data);

        // mark device online
        deviceService.updateDeviceStatus(deviceId, true);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Sensor data received");
        return response;
    }

    // === IoT fetches latest command ===
    @GetMapping("/device/{id}/commands/latest")
    public Map<String, Object> getLatestCommands(@PathVariable Long id) {
        Device device = deviceService.getDevice(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        Optional<DeviceCommand> wateringCmd = commandService.getLatestCommand(device, CommandType.watering);
        Optional<DeviceCommand> lightingCmd = commandService.getLatestCommand(device, CommandType.lighting);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deviceId", id);

        Map<String, Object> watering = new HashMap<>();
        watering.put("override", wateringCmd.map(DeviceCommand::getOverrideMode).orElse(false));
        watering.put("value", wateringCmd.map(DeviceCommand::getValue).orElse(0f));

        Map<String, Object> lighting = new HashMap<>();
        lighting.put("override", lightingCmd.map(DeviceCommand::getOverrideMode).orElse(false));
        lighting.put("value", lightingCmd.map(DeviceCommand::getValue).orElse(0f));

        response.put("watering", watering);
        response.put("lighting", lighting);

        return response;
    }

    // === Web UI sends control command ===
    @PostMapping("/device/{id}/command")
    public Map<String, Object> sendCommand(@PathVariable Long id,
                                           @RequestParam CommandType type,
                                           @RequestParam Float value,
                                           @RequestParam(defaultValue = "false") boolean override) {

        Device device = deviceService.getDevice(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        DeviceCommand cmd = commandService.createCommand(device, type, value, override);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "created");
        response.put("commandId", cmd.getId());
        return response;
    }
}
