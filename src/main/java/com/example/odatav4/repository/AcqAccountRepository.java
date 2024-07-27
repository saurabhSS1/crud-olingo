package com.example.odatav4.repository;

import com.example.odatav4.entity.AcqAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AcqAccountRepository extends JpaRepository<AcqAccount, String>, JpaSpecificationExecutor<AcqAccount> {
}
