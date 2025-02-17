package com.fishgo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectionTestController {

    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<?> test(){
        return ResponseEntity.ok("ok");
    }
}
