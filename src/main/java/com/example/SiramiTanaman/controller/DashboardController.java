package com.example.SiramiTanaman.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.SiramiTanaman.model.CommandType;
import com.example.SiramiTanaman.model.Device;
import com.example.SiramiTanaman.model.DeviceCommand;
import com.example.SiramiTanaman.model.DeviceStatus;
import com.example.SiramiTanaman.model.SensorData;
import com.example.SiramiTanaman.model.User;
import com.example.SiramiTanaman.service.DeviceCommandService;
import com.example.SiramiTanaman.service.DeviceService;
import com.example.SiramiTanaman.service.SensorDataService;
import com.example.SiramiTanaman.service.UserService;

import lombok.RequiredArgsConstructor;




@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final DeviceService deviceService;
    private final SensorDataService sensorDataService;
    private final DeviceCommandService deviceCommandService;

    
    @GetMapping
    public String viewDashboard(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "Guest";
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Device> devices = deviceService.getDevicesByOwner(user);
        model.addAttribute("user", user);
        model.addAttribute("devices", devices);

        
        Device device = devices.isEmpty() ? null : devices.get(0);
        model.addAttribute("firstDeviceName", device != null ? device.getName() : "Tanaman Buah Melon");

        
        ZoneId zone = ZoneId.of("Asia/Jakarta");
        Function<DeviceCommand, String> toIso =
                c -> c.getCreatedAt().atZone(zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        List<String> wateringTs = Collections.emptyList();
        List<String> lightingTs = Collections.emptyList();

        if (device != null) {
            wateringTs = deviceCommandService.getLastCommands(device, CommandType.watering, 10)
                                            .stream().map(toIso).toList();
            lightingTs = deviceCommandService.getLastCommands(device, CommandType.lighting, 10)
                                            .stream().map(toIso).toList();
        }

        
        if (wateringTs.isEmpty()) {
            ZonedDateTime base = ZonedDateTime.now(zone).withMinute(0).withSecond(0).withNano(0);
            wateringTs = List.of(
                    base.minusHours(4).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    base.minusHours(3).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    base.minusHours(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    base.minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    base.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );
        }
        if (lightingTs.isEmpty()) {
            ZonedDateTime base = ZonedDateTime.now(zone).withMinute(0).withSecond(0).withNano(0);
            lightingTs = List.of(
                    base.minusHours(6).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    base.minusHours(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    base.minusHours(4).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    base.minusHours(3).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    base.minusHours(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );
        }

        model.addAttribute("wateringTimestamps", wateringTs);
        model.addAttribute("lightingTimestamps", lightingTs);

        return "dashboard";
    }


    
    @GetMapping("/device/{id}")
    public String viewDevice(@PathVariable Long id, Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "Guest";
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);

        Device device = deviceService.getDevice(id)
                .orElseGet(() -> {
                    Device dummy = new Device();
                    dummy.setName("Device Tidak Ditemukan");
                    dummy.setLocation("Unknown");
                    dummy.setStatus(DeviceStatus.offline);
                    dummy.setCreatedAt(LocalDateTime.now());
                    return dummy;
                });

        
        SensorData latestData = sensorDataService.getLatestReading(device);
        List<SensorData> historyData = sensorDataService.getLatestSensorData(device);

        
        Collections.reverse(historyData);

        model.addAttribute("device", device);
        model.addAttribute("latestData", latestData);


        List<Map<String, Object>> sensorList = historyData.stream()
            .map(d -> {
                Map<String, Object> map = new HashMap<>();
                map.put("timestamp", d.getTimestamp());
                map.put("soilMoisture", d.getSoilMoisture());
                map.put("airHumidity", d.getAirHumidity());
                map.put("lightIntensity", d.getLightIntensity());
                return map;
            })
            .toList();



        model.addAttribute("sensorData", sensorList);


        
        model.addAttribute("commands", List.of());

        return "device-detail"; 
    }
    

    @GetMapping("/about")
    public String about(Principal principal, Model model) {
        
        String username = principal != null ? principal.getName() : "Guest";
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        model.addAttribute("user", user);

        return "tentang-kami";
    }

    
    @GetMapping("/device")
    public String listDevices(Model model, Principal principal) {
        
        String username = principal != null ? principal.getName() : "Guest";
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        model.addAttribute("user", user);

        var devices = deviceService.getAllDevices(); 
        model.addAttribute("devices", devices);
        return "device-list"; 
    }


    
    @PostMapping("/device/add")
    public String addDevice(@RequestParam String name,
                            @RequestParam String location) {

        Device newDevice = Device.builder()
                .name(name)
                .location(location)
                .status(DeviceStatus.offline)
                .build();

        deviceService.addDevice(newDevice);
        return "redirect:/dashboard";
    }

    
    @PostMapping("/device/{id}/delete")
    public String deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return "redirect:/dashboard";
    }
    
}
