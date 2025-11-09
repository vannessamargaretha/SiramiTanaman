package com.example.SiramiTanaman.repository;

import com.example.SiramiTanaman.model.SensorData;
import com.example.SiramiTanaman.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    List<SensorData> findTop10ByDeviceOrderByTimestampDesc(Device device);
    SensorData findTop1ByDeviceOrderByTimestampDesc(Device device);
}