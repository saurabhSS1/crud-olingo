package com.example.odatav4.service;

import com.example.odatav4.entity.AcqAccount;
import org.springframework.data.jpa.domain.Specification;

public class AcqAccountSpecifications {

    public static Specification<AcqAccount> filterByAccountName(String filter) {
        return (root, query, builder) -> {
            if (filter != null && !filter.isEmpty()) {
                String accountName = extractAccountName(filter);
                System.out.println("Filtering by extracted accountName: " + accountName);
                return builder.like(builder.lower(root.get("accountName")), "%" + accountName.toLowerCase() + "%");
            }
            return null;
        };
    }

    private static String extractAccountName(String filter) {
        String prefix = "accountName eq '";
        String suffix = "'";
        if (filter.startsWith(prefix) && filter.endsWith(suffix)) {
            return filter.substring(prefix.length(), filter.length() - suffix.length());
        }
        return filter;
    }
}
