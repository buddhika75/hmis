package com.divudi.core.data.dto;

import com.divudi.core.entity.Category;
import java.io.Serializable;

public class CollectingCentreFeeAssignmentDTO implements Serializable {

    private Long institutionId;
    private String institutionName;
    private String institutionCode;
    private Category currentFeeListType;
    private boolean selected;

    public CollectingCentreFeeAssignmentDTO() {
    }

    public CollectingCentreFeeAssignmentDTO(Long institutionId, String institutionName, String institutionCode, Category currentFeeListType) {
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.institutionCode = institutionCode;
        this.currentFeeListType = currentFeeListType;
        this.selected = false;
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

    public Category getCurrentFeeListType() {
        return currentFeeListType;
    }

    public void setCurrentFeeListType(Category currentFeeListType) {
        this.currentFeeListType = currentFeeListType;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getFeeListDisplay() {
        return currentFeeListType != null ? currentFeeListType.getName() : "No Fee List";
    }

    @Override
    public String toString() {
        return institutionName + " (" + institutionCode + ") - " + getFeeListDisplay();
    }
}