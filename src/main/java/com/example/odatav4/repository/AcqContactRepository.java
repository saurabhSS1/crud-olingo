package com.example.odatav4.repository;

import com.example.odatav4.entity.AcqContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AcqContactRepository extends JpaRepository<AcqContact, String>, JpaSpecificationExecutor<AcqContact> {
}
