package com.example.odatav4.service;

import com.example.odatav4.entity.AcqAccount;
import com.example.odatav4.repository.AcqAccountRepository;
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
public class AcqAccountService {

    @Autowired
    private AcqAccountRepository repository;

    public List<AcqAccount> getAllAccounts(String filter, String orderBy, Integer top, Integer skip) {
        Specification<AcqAccount> spec = Specification.where(AcqAccountSpecifications.filterByAccountName(filter));

        Sort sort = Sort.unsorted();
        if (orderBy != null && !orderBy.isEmpty()) {
            sort = Sort.by(orderBy).ascending();
        }

        // Calculate the correct page size and number
        int pageSize = top != null ? top + (skip != null ? skip : 0) : 10;
        int pageNumber = 0;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        System.out.println("Fetching accounts with filter: " + filter + ", orderBy: " + orderBy + ", top: " + top + ", skip: " + skip);
        List<AcqAccount> accounts = repository.findAll(spec, pageable).getContent();

        // Apply skip logic after fetching the results
        if (skip != null && skip > 0 && accounts.size() > skip) {
            accounts = accounts.subList(skip, accounts.size());
        }

        // Limit the results to 'top' after applying skip logic
        if (top != null && accounts.size() > top) {
            accounts = accounts.subList(0, top);
        }

        System.out.println("Fetched accounts: " + accounts);
        return accounts;
    }

    public Optional<AcqAccount> getAccountById(String id) {
        return repository.findById(id);
    }

    public AcqAccount createAccount(AcqAccount account) {
        return repository.save(account);
    }

    public AcqAccount updateAccount(String id, AcqAccount account) {
        account.setId(id);
        return repository.save(account);
    }

    public void deleteAccount(String id) {
        repository.deleteById(id);
    }
}
