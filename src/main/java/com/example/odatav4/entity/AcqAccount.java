package com.example.odatav4.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.OffsetDateTime;

@Entity
public class AcqAccount {

    @Id
    private String id;
    private String accountName;
    private String accountType;
    private OffsetDateTime recordLastModifiedDate;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public OffsetDateTime getRecordLastModifiedDate() {
        return recordLastModifiedDate;
    }

    public void setRecordLastModifiedDate(OffsetDateTime recordLastModifiedDate) {
        this.recordLastModifiedDate = recordLastModifiedDate;
    }
}
