package com.divudi.core.entity.lab;

import com.divudi.core.data.lab.Analyzer;
import com.divudi.core.data.lab.TestHistoryType;
import com.divudi.core.entity.AppEmail;
import com.divudi.core.entity.Category;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Sms;
import com.divudi.core.entity.Staff;
import com.divudi.core.entity.WebUser;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari@gmail.com> and Damith Deshan
 * 
 */
@Entity
public class LabTestHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    private PatientInvestigation patientInvestigation;

    @ManyToOne
    private PatientReport patientReport;

    @ManyToOne
    private PatientSample patientSample;

    @Enumerated(EnumType.STRING)
    private TestHistoryType testHistoryType;

    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @ManyToOne
    private WebUser createdBy;

    private boolean retired;

    @Temporal(TemporalType.TIMESTAMP)
    private Date retiredAt;

    @ManyToOne
    private WebUser retiredBy;

    private String retireComments;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Institution institution;

    @ManyToOne
    private Department fromDepartment;

    @ManyToOne
    private Department toDepartment;

    @ManyToOne
    private Category analyzer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Staff staff;
    
    @ManyToOne
    private Sms sms;
    
    @ManyToOne
    private AppEmail email;
    
    @ManyToOne
    private PatientSampleComponant sampleComponent;
    
    private String comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LabTestHistory)) {
            return false;
        }
        LabTestHistory other = (LabTestHistory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.core.entity.lab.LabTestHistory[ id=" + id + " ]";
    }

    public PatientInvestigation getPatientInvestigation() {
        return patientInvestigation;
    }

    public void setPatientInvestigation(PatientInvestigation patientInvestigation) {
        this.patientInvestigation = patientInvestigation;
    }

    public PatientReport getPatientReport() {
        return patientReport;
    }

    public void setPatientReport(PatientReport patientReport) {
        this.patientReport = patientReport;
    }

    public PatientSample getPatientSample() {
        return patientSample;
    }

    public void setPatientSample(PatientSample patientSample) {
        this.patientSample = patientSample;
    }

    public TestHistoryType getTestHistoryType() {
        return testHistoryType;
    }

    public void setTestHistoryType(TestHistoryType testHistoryType) {
        this.testHistoryType = testHistoryType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getFromDepartment() {
        return fromDepartment;
    }

    public void setFromDepartment(Department fromDepartment) {
        this.fromDepartment = fromDepartment;
    }

    public Department getToDepartment() {
        return toDepartment;
    }

    public void setToDepartment(Department toDepartment) {
        this.toDepartment = toDepartment;
    }

    public Category getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Category analyzer) {
        this.analyzer = analyzer;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public PatientSampleComponant getSampleComponent() {
        return sampleComponent;
    }

    public void setSampleComponant(PatientSampleComponant sampleComponent) {
        this.sampleComponent = sampleComponent;
    }

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public AppEmail getEmail() {
        return email;
    }

    public void setEmail(AppEmail email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
}
