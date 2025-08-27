package com.divudi.core.data.dto;

import java.io.Serializable;

public class CollectingCentreItemFeeCountDTO implements Serializable {

    private Long institutionId;
    private String institutionName;
    private String institutionCode;
    private Long activeItemFeesCount;

    public CollectingCentreItemFeeCountDTO() {
    }

    public CollectingCentreItemFeeCountDTO(Long institutionId, String institutionName, String institutionCode, Long activeItemFeesCount) {
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.institutionCode = institutionCode;
        this.activeItemFeesCount = activeItemFeesCount;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public Long getActiveItemFeesCount() {
        return activeItemFeesCount;
    }

    public void setActiveItemFeesCount(Long activeItemFeesCount) {
        this.activeItemFeesCount = activeItemFeesCount;
    }

    @Override
    public String toString() {
        return institutionName + " (" + institutionCode + ") - " + activeItemFeesCount + " fees";
    }
}