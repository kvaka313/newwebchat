package com.infopulse.controller;

import com.infopulse.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class RegistrationController {

    private RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService){
        this.registrationService = registrationService;
    }

    @PostMapping(value = "\registration")
    public ResponseEntity registration(@Valid @RequestBody UserDTO userDTO){
        registrationService.save(userDTO);
        return ResponseEntity.accepted().build();
    }
}
