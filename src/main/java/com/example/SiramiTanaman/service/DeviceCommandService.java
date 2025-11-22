package com.example.SiramiTanaman.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.SiramiTanaman.model.CommandType;
import com.example.SiramiTanaman.model.CommandStatus;
import com.example.SiramiTanaman.model.Device;
import com.example.SiramiTanaman.model.DeviceCommand;
import com.example.SiramiTanaman.repository.DeviceCommandRepository;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;



@Service
@RequiredArgsConstructor
public class DeviceCommandService {

    private final DeviceCommandRepository deviceCommandRepository;

    public DeviceCommand createCommand(Device device, CommandType type, Float value, boolean override) {

        DeviceCommand cmd = DeviceCommand.builder()
            .device(device)
            .commandType(type)
            .value(value)
            .overrideMode(override)
            .durationSeconds(60)  
            .sent(false)
            .createdAt(LocalDateTime.now())
            .build();


        return deviceCommandRepository.save(cmd);
    }


    public Optional<DeviceCommand> getLatestCommand(Device device, CommandType type) {
        return deviceCommandRepository
                .findTop1ByDeviceAndCommandTypeOrderByCreatedAtDesc(device, type);
    }


    public List<DeviceCommand> getAllCommands(Device device) {
        return deviceCommandRepository.findByDevice(device);
    }


    public void markAsSent(Long commandId) {
        deviceCommandRepository.findById(commandId).ifPresent(cmd -> {
            cmd.setSent(true);
            deviceCommandRepository.save(cmd);
        });
    }

    public void clearOverride(Device device, CommandType type) {
    deviceCommandRepository
            .findTop1ByDeviceAndCommandTypeOrderByCreatedAtDesc(device, type)
            .ifPresent(cmd -> {
                cmd.setOverrideMode(false);
                deviceCommandRepository.save(cmd);
            });
}



    public List<DeviceCommand> getLastCommands(Device device, CommandType type, int n) {

        if (n == 10) {
            return deviceCommandRepository
                    .findTop10ByDeviceAndCommandTypeOrderByCreatedAtDesc(device, type);
        }

        return deviceCommandRepository.findByDevice(device).stream()
                .filter(c -> c.getCommandType() == type)   
                .sorted(Comparator.comparing(DeviceCommand::getCreatedAt).reversed())
                .limit(n)
                .toList();
    }
}
