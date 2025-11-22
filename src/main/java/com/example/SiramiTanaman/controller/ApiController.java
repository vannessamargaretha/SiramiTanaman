package com.example.SiramiTanaman.controller;

import com.example.SiramiTanaman.model.*;
import com.example.SiramiTanaman.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final DeviceService deviceService;
    private final SensorDataService sensorDataService;
    private final DeviceCommandService commandService;
    private final DeviceCustomSettingsService deviceCustomSettingsService;

    
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
        deviceService.updateDeviceStatus(deviceId, true);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Sensor data received");
        return response;
    }


    @GetMapping("/device/{id}/commands/latest")
    public Map<String, Object> getLatestCommands(@PathVariable Long id) {

        Device device = deviceService.getDevice(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        Optional<DeviceCommand> wateringCmd =
                commandService.getLatestCommand(device, CommandType.watering);

        Optional<DeviceCommand> lightingCmd =
                commandService.getLatestCommand(device, CommandType.lighting);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deviceId", id);

        Map<String, Object> watering = new HashMap<>();
        watering.put("override", wateringCmd.map(DeviceCommand::isOverrideMode).orElse(false));
        watering.put("durationSeconds",wateringCmd.map(DeviceCommand::getDurationSeconds).orElse(0));
        watering.put("value", wateringCmd.map(DeviceCommand::getValue).orElse(0f));

        Map<String, Object> lighting = new HashMap<>();
        lighting.put("override", lightingCmd.map(DeviceCommand::isOverrideMode).orElse(false));
        lighting.put("durationSeconds",lightingCmd.map(DeviceCommand::getDurationSeconds).orElse(0));
        lighting.put("value", lightingCmd.map(DeviceCommand::getValue).orElse(0f));

        response.put("watering", watering);
        response.put("lighting", lighting);

        return response;
    }


    @PostMapping("/device/{id}/command")
    public Map<String, Object> sendCommand(
            @PathVariable Long id,
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


    @GetMapping("/{deviceId}/next-command")
    public ResponseEntity<?> getNextCommand(@PathVariable Long deviceId) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/command/{commandId}/completed")
    public ResponseEntity<?> completeCommand(@PathVariable Long commandId) {
        return ResponseEntity.noContent().build();
    }

        @PostMapping("/device/{id}/command/clear")
        public Map<String, Object> clearOverride(
                @PathVariable Long id,
                @RequestParam CommandType type) {

                Device device = deviceService.getDevice(id)
                        .orElseThrow(() -> new RuntimeException("Device not found"));

                commandService.clearOverride(device, type);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "override cleared");
                return response;
        }


        @GetMapping("/device/{id}/settings")
        public Map<String, Object> getDeviceSettings(@PathVariable Long id) {

                Device device = deviceService.getDevice(id)
                        .orElseThrow(() -> new RuntimeException("Device not found"));

                Optional<DeviceCustomSettings> settingsOpt =
                        deviceCustomSettingsService.getLatestByDeviceId(id);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("deviceId", id);

                
                response.put("manualMode", device.getManualMode() != null ? device.getManualMode() : false);

                if (settingsOpt.isPresent()) {
                        DeviceCustomSettings settings = settingsOpt.get();
                        response.put("soilMin", settings.getSoilMin());
                        response.put("soilMax", settings.getSoilMax());
                        response.put("tempPump", settings.getTempPump());
                        response.put("tempLightOff", settings.getTempLightOff());
                        response.put("luxMin", settings.getLuxMin());
                        response.put("luxMax", settings.getLuxMax());
                        response.put("lightDuration", settings.getLightDuration());
                        response.put("lightInterval", settings.getLightInterval());
                } else {
                        response.put("soilMin", 20);
                        response.put("soilMax", 80);
                        response.put("tempPump", 35);
                        response.put("tempLightOff", 35);
                        response.put("luxMin", 100);
                        response.put("luxMax", 1000);
                        response.put("lightDuration", 3600);
                        response.put("lightInterval", 86400);
                }

                return response;
        }


        @PostMapping("/device/{id}/mode")
        public ResponseEntity<?> updateMode(
                @PathVariable Long id,
                @RequestBody Map<String, Boolean> body) {

                boolean manualMode = body.getOrDefault("manualMode", false);

                deviceService.updateMode(id, manualMode);

                return ResponseEntity.ok(Map.of(
                        "deviceId", id,
                        "manualMode", manualMode
                ));
        }



}
