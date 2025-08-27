package com.divudi.bean.lab;

import com.divudi.core.data.lab.TestHistoryType;
import com.divudi.core.entity.Staff;
import com.divudi.core.entity.WebUser;
import java.util.Date;
import java.util.Objects;

/**
 * @author  H.K. Damith Deshan <hkddrajapaksha@gmail.com>
 */
public class LabTestHistoryLight {

    private Long id;
    private TestHistoryType historyType;
    private Date historyAt;
    private String institutionName;
    private String departmentName;
    private Staff transporter;
    private WebUser user;
    private String comment;

    public LabTestHistoryLight() {
        
    }

    public LabTestHistoryLight(Long id, TestHistoryType historyType, Date historyAt, String institutionName, String departmentName,Staff transporter, WebUser user, String comment) {
        this.id = id;
        this.historyType = historyType;
        this.historyAt = historyAt;
        this.institutionName = institutionName;
        this.departmentName = departmentName;
        this.transporter = transporter;
        this.user = user;
        this.comment = comment;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LabTestHistoryLight other = (LabTestHistoryLight) obj;
        return Objects.equals(this.id, other.id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getHistoryAt() {
        return historyAt;
    }

    public void setHistoryAt(Date historyAt) {
        this.historyAt = historyAt;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public WebUser getUser() {
        return user;
    }

    public void setUser(WebUser user) {
        this.user = user;
    }

    public TestHistoryType getHistoryType() {
        return historyType;
    }

    public void setHistoryType(TestHistoryType historyType) {
        this.historyType = historyType;
    }

    public Staff getTransporter() {
        return transporter;
    }

    public void setTransporter(Staff transporter) {
        this.transporter = transporter;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
