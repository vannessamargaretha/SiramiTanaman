package com.example.SiramiTanaman.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SiramiTanaman.model.CommandType;
import com.example.SiramiTanaman.model.Device;
import com.example.SiramiTanaman.model.DeviceCommand;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {
    
    Optional<DeviceCommand> findTop1ByDeviceAndCommandTypeOrderByCreatedAtDesc(Device device, CommandType commandType);
    List<DeviceCommand> findByDevice(Device device);

     // NEW: fetch latest 10 per type
    List<DeviceCommand> findTop10ByDeviceAndCommandTypeOrderByCreatedAtDesc(Device device, CommandType commandType);
}
