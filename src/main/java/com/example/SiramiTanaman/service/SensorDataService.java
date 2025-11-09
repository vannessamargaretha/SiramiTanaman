package com.example.SiramiTanaman.service;

import com.example.SiramiTanaman.model.SensorData;
import com.example.SiramiTanaman.model.Device;
import com.example.SiramiTanaman.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorDataRepository sensorDataRepository;

    public SensorData saveSensorData(SensorData data) {
        return sensorDataRepository.save(data);
    }

    public List<SensorData> getLatestSensorData(Device device) {
        return sensorDataRepository.findTop10ByDeviceOrderByTimestampDesc(device);
    }

    public SensorData getLatestReading(Device device) {
        return sensorDataRepository.findTop1ByDeviceOrderByTimestampDesc(device);
    }
}
