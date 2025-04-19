package com.example.modbusapplication.Controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.time.Instant;

@RestController
@RequestMapping("/api")
public class ModbusController {
  // ⬇️ Accepts binary data (application/octet-stream)
    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<String> uploadBinaryData(@RequestBody byte[] data) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);

            long timestamp = buffer.getLong(); // Read 8-byte timestamp
            System.out.println("⏱️ Timestamp: " + Instant.ofEpochMilli(timestamp));

            while (buffer.hasRemaining()) {
                short registerValue = buffer.getShort(); // Read 2-byte register
                System.out.println("📥 Register Value: " + registerValue);
            }

            return ResponseEntity.ok("✅ Binary data received successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error processing binary data: " + e.getMessage());
        }
    }

}


// package com.example.modbusapplication.Controller;

// import java.util.List;
// import java.util.Map;

// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import com.example.modbusapplication.Service.ModbusService;

// @RestController
// @RequestMapping("/modbus")
// public class ModbusController {
//      private final ModbusService modbusService;

//     public ModbusController(ModbusService modbusService) {
//         this.modbusService = modbusService;
//     }
//        @GetMapping("/latest")
//     public Map<Integer, Integer> getLatestScheduledRegisters() {
//         return modbusService.getLatestScheduledValues();
//     }

//     /**
//      * 🔹 Read specific registers on demand
//      */
//     @PostMapping("/read")
//     public Map<Integer, Integer> readRegisters(@RequestBody List<Integer> registers) {
//         return modbusService.readRegisters(registers);
//     }
// }
// package com.example.modbusapplication.Controller;

// import com.example.modbusapplication.Service.ModbusService;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/modbus")
// public class ModbusController {

//     private final ModbusService modbusService;

//     public ModbusController(ModbusService modbusService) {
//         this.modbusService = modbusService;
//     }

//     /**
//      * 🔹 Read specific registers based on frontend request
//      */
//     @PostMapping("/read")
//     public Map<Integer, Integer> readRegisters(@RequestBody List<Integer> registers) {
//         return modbusService.readRegisters(registers);
//     }
// }
