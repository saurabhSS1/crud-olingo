package com.example.odatav4.controller;

import com.example.odatav4.entity.AcqContact;
import com.example.odatav4.service.AcqContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/odata/acqContacts")
public class AcqContactController {

    @Autowired
    private AcqContactService service;

    @GetMapping
    public List<AcqContact> getAllContacts(
            @RequestParam(value = "$filter", required = false) String filter,
            @RequestParam(value = "$orderby", required = false) String orderBy,
            @RequestParam(value = "$top", required = false) Integer top,
            @RequestParam(value = "$skip", required = false) Integer skip) {
        return service.getAllContacts(filter, orderBy, top, skip);
    }

    @GetMapping("/{id}")
    public Optional<AcqContact> getContactById(@PathVariable String id) {
        return service.getContactById(id);
    }

    @PostMapping
    public ResponseEntity<AcqContact> createContact(@RequestBody AcqContact contact) {
         service.createContact(contact);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public AcqContact updateContact(@PathVariable String id, @RequestBody AcqContact contact) {
        return service.updateContact(id, contact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String id) {
        service.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}
