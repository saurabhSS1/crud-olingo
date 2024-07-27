package com.example.odatav4.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.OffsetDateTime;

@Entity
public class AcqContact {

    @Id
    private String id;
    private String contactName;
    private String acqAccountId;
    private String contactType;
    private OffsetDateTime recordLastModifiedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getAcqAccountId() {
        return acqAccountId;
    }

    public void setAcqAccountId(String acqAccountId) {
        this.acqAccountId = acqAccountId;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public OffsetDateTime getRecordLastModifiedDate() {
        return recordLastModifiedDate;
    }

    public void setRecordLastModifiedDate(OffsetDateTime recordLastModifiedDate) {
        this.recordLastModifiedDate = recordLastModifiedDate;
    }

    // Getters and Setters
}
