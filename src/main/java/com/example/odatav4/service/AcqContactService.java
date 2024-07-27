package com.example.odatav4.service;

import com.example.odatav4.entity.AcqContact;
import com.example.odatav4.repository.AcqContactRepository;
import com.example.odatav4.service.AcqAccountSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AcqContactService {

    @Autowired
    private AcqContactRepository repository;

    public List<AcqContact> getAllContacts(String filter, String orderBy, Integer top, Integer skip) {
        Specification<AcqContact> spec = Specification.where(AcqContactSpecifications.filterByContactName(filter));

        Sort sort = Sort.unsorted();
        if (orderBy != null && !orderBy.isEmpty()) {
            sort = Sort.by(orderBy).ascending();
        }

        // Calculate the correct page size and number
        int pageSize = top != null ? top + (skip != null ? skip : 0) : 10;
        int pageNumber = 0;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        System.out.println("Fetching contacts with filter: " + filter + ", orderBy: " + orderBy + ", top: " + top + ", skip: " + skip);
        List<AcqContact> contacts = repository.findAll(spec, pageable).getContent();

        // Apply skip logic after fetching the results
        if (skip != null && skip > 0 && contacts.size() > skip) {
            contacts = contacts.subList(skip, contacts.size());
        }

        // Limit the results to 'top' after applying skip logic
        if (top != null && contacts.size() > top) {
            contacts = contacts.subList(0, top);
        }

        System.out.println("Fetched contacts: " + contacts);
        return contacts;
    }

    public Optional<AcqContact> getContactById(String id) {
        return repository.findById(id);
    }

    public AcqContact createContact(AcqContact contact) {
        return repository.save(contact);
    }

    public AcqContact updateContact(String id, AcqContact contact) {
        contact.setId(id);
        return repository.save(contact);
    }

    public void deleteContact(String id) {
        repository.deleteById(id);
    }
}
