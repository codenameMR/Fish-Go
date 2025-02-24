package com.fishgo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Connection Test", description = "연결 확인용")
@RestController
public class ConnectionTestController {

    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<?> test(){
        return ResponseEntity.ok("ok");
    }
}
