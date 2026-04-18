package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.autocomplete.AutocompleteSearchRequest;
import com.tranhuudat.prms.service.AutoCompleteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/autocomplete")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoCompleteController {
    AutoCompleteService autoCompleteService;

    @PostMapping("/users")
    public ResponseEntity<BaseResponse> users(@RequestBody AutocompleteSearchRequest request) {
        return ResponseEntity.ok(autoCompleteService.users(request));
    }

    @PostMapping("/projects")
    public ResponseEntity<BaseResponse> projects(@RequestBody AutocompleteSearchRequest request) {
        return ResponseEntity.ok(autoCompleteService.projects(request));
    }
}

