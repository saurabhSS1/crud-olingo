package com.example.odatav4.controller;

import com.example.odatav4.entity.AcqAccount;
import com.example.odatav4.service.AcqAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/odata/acqAccounts")
public class AcqAccountController {

    @Autowired
    private AcqAccountService service;

    @GetMapping
    public List<AcqAccount> getAllAccounts(
            @RequestParam(value = "$filter", required = false) String filter,
            @RequestParam(value = "$orderby", required = false) String orderBy,
            @RequestParam(value = "$top", required = false) Integer top,
            @RequestParam(value = "$skip", required = false) Integer skip) {
        return service.getAllAccounts(filter, orderBy, top, skip);
    }

    @GetMapping("/{id}")
    public Optional<AcqAccount> getAccountById(@PathVariable String id) {
        return service.getAccountById(id);
    }

    @PostMapping
    public ResponseEntity<AcqAccount> createAccount(@RequestBody AcqAccount account) {
         service.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public AcqAccount updateAccount(@PathVariable String id, @RequestBody AcqAccount account) {
        return service.updateAccount(id, account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        service.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
