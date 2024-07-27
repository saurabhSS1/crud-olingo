package com.example.odatav4.service;

import com.example.odatav4.entity.AcqContact;
import org.springframework.data.jpa.domain.Specification;

public class AcqContactSpecifications {

    public static Specification<AcqContact> filterByContactName(String filter) {
        return (root, query, builder) -> {
            if (filter != null && !filter.isEmpty()) {
                String contactName = extractContactName(filter);
                System.out.println("Filtering by extracted contactName: " + contactName);
                return builder.like(builder.lower(root.get("contactName")), "%" + contactName.toLowerCase() + "%");
            }
            return null;
        };
    }

    private static String extractContactName(String filter) {
        String prefix = "contactName eq '";
        String suffix = "'";
        if (filter.startsWith(prefix) && filter.endsWith(suffix)) {
            return filter.substring(prefix.length(), filter.length() - suffix.length());
        }
        return filter;
    }
}
