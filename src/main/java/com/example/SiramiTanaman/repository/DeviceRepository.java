package com.example.SiramiTanaman.repository;

import com.example.SiramiTanaman.model.Device;
import com.example.SiramiTanaman.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByOwner(User owner);
}