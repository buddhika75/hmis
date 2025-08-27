/*
 * Open Hospital Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.core.entity.lab;

import com.divudi.core.entity.RetirableEntity;
import com.divudi.core.data.lab.PatientInvestigationStatus;
import com.divudi.core.data.lab.Priority;
import com.divudi.core.data.lab.SampleRequestType;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.Patient;
import com.divudi.core.entity.Staff;
import com.divudi.core.entity.WebUser;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author buddhika.ari@gmail.com
 *
 */
@Entity
public class PatientSample implements Serializable, RetirableEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long sampleId;

    @ManyToOne
    private Institution institution;
    @ManyToOne
    private Department department;

    @ManyToOne
    private Patient patient;
    @ManyToOne
    private PatientInvestigation patientInvestigation;
    @ManyToOne
    private Bill bill;
    //Created Properties
    @ManyToOne
    private WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    //Barcode Generation
    private Boolean barcodeGenerated;
    @ManyToOne
    private WebUser barcodeGenerator;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date barcodeGeneratedAt;
    @ManyToOne
    private Department barcodeGeneratedDepartment;
    @ManyToOne
    private Institution barcodeGeneratedInstitution;
    //Sample Collection
    private Boolean sampleCollected;
    @ManyToOne
    private WebUser sampleCollecter;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sampleCollectedAt;
    private Boolean sampleCollectedOutside;
    private String sampleCollectionComments;
    @ManyToOne
    private Department sampleCollectedDepartment;
    @ManyToOne
    private Institution sampleCollectedInstitution;
    //Sample Received at Lab
    //Sample Sent to Lab
    private Boolean sampleSent = false;
    @ManyToOne
    private WebUser sampleSentBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sampleSentAt;
    @ManyToOne
    private Staff sampleTransportedToLabByStaff;

    //outsourced
    private Boolean outsourced = false;
    @ManyToOne
    private Institution outsourceInstitution;
    @ManyToOne
    private Department outsourceDepartment;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date outsourcedAt;
    @ManyToOne
    private WebUser outsourceSentUser;
    @ManyToOne
    private Staff outsourceSampleTransporter;

    @ManyToOne
    private Department sampleSentToDepartment;
    @ManyToOne
    private Institution sampleSentToInstitution;

    @Enumerated
    private Priority priority;

    //Sent To Analyzer
    private Boolean readyTosentToAnalyzer;
    @Enumerated(EnumType.STRING)
    private SampleRequestType sampleRequestType;
    private Boolean sentToAnalyzer;
    @ManyToOne
    private WebUser sentToAnalyzerBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sentToAnalyzerAt;
    @Lob
    private String sentToAnalyzerComments;
    @ManyToOne
    private Department sentToAnalyzerDepartment;
    @ManyToOne
    private Institution sentToAnalyzerInstitution;
    //Sent To Analyzer
    private Boolean receivedFromAnalyzer;
    @ManyToOne
    private WebUser receivedFromAnalyzerBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date receivedFromAnalyzerAt;
    @Lob
    private String receivedFromAnalyzerComments;
    @ManyToOne
    private Department receivedFromAnalyzerDepartment;
    @ManyToOne
    private Institution receivedFromAnalyzerInstitution;
    @ManyToOne
    private PatientSample duplicatedFrom;
    @ManyToOne
    private PatientSample duplicatedTo;
    @ManyToOne
    private PatientSample divertedFrom;
    @ManyToOne
    private PatientSample divertedTo;

//
    @ManyToOne
    private Item investigationComponant;
    @ManyToOne
    private Item test;
    @ManyToOne
    private InvestigationTube tube;
    @ManyToOne
    private Machine machine;
    @ManyToOne
    private Sample sample;

    private Boolean sampleReceivedAtLab;

    @ManyToOne
    private WebUser sampleReceiverAtLab;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sampleReceivedAtLabAt;

    private String sampleReceivedAtLabComments;

    @ManyToOne
    private Department sampleReceivedAtLabDepartment;

    @ManyToOne
    private Institution sampleReceivedAtLabInstitution;
    @Enumerated(EnumType.ORDINAL)
    private PatientInvestigationStatus status;

    //Cancellation
    private Boolean cancelled = false;
    @ManyToOne
    private WebUser cancelledUser;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date cancelledAt;
    private String cancellComments;
    @ManyToOne
    private Department cancellDepartment;
    @ManyToOne
    private Institution cancellInstitution;

    private String sampleRejectionComment;
    private Boolean sampleRejected = false;
    @ManyToOne
    private WebUser sampleRejectedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sampleRejectedAt;
    
    private Boolean requestReCollected = false;

    //Retairing properties
    private boolean retired;
    @ManyToOne
    private WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;
    
    @ManyToOne
    private PatientSample referenceSample;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdStr() {
        String formatted = id + "";
        return formatted;
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
        if (!(object instanceof PatientSample)) {
            return false;
        }
        PatientSample other = (PatientSample) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.core.entity.lab.PatientSample[ id=" + id + " ]";
    }

    public PatientSample() {
        if (status == null) {
            status = PatientInvestigationStatus.SAMPLE_GENERATED;
        }
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public PatientInvestigation getPatientInvestigation() {
        return patientInvestigation;
    }

    public void setPatientInvestigation(PatientInvestigation patientInvestigation) {
        this.patientInvestigation = patientInvestigation;
    }

    public Item getInvestigationComponant() {
        return investigationComponant;
    }

    public void setInvestigationComponant(Item investigationComponant) {
        this.investigationComponant = investigationComponant;
    }

    public Item getTest() {
        return test;
    }

    public void setTest(Item test) {
        this.test = test;
    }

    public InvestigationTube getTube() {
        return tube;
    }

    public void setTube(InvestigationTube tube) {
        this.tube = tube;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Boolean getSampleCollected() {
        return sampleCollected;
    }

    public void setSampleCollected(Boolean sampleCollected) {
        this.sampleCollected = sampleCollected;
    }

    public WebUser getSampleCollecter() {
        return sampleCollecter;
    }

    public void setSampleCollecter(WebUser sampleCollecter) {
        this.sampleCollecter = sampleCollecter;
    }

    public Date getSampleCollectedAt() {
        return sampleCollectedAt;
    }

    public void setSampleCollectedAt(Date sampleCollectedAt) {
        this.sampleCollectedAt = sampleCollectedAt;
    }

    public Boolean getSampleCollectedOutside() {
        return sampleCollectedOutside;
    }

    public void setSampleCollectedOutside(Boolean sampleCollectedOutside) {
        this.sampleCollectedOutside = sampleCollectedOutside;
    }

    public String getSampleCollectionComments() {
        return sampleCollectionComments;
    }

    public void setSampleCollectionComments(String sampleCollectionComments) {
        this.sampleCollectionComments = sampleCollectionComments;
    }

    public Department getSampleCollectedDepartment() {
        return sampleCollectedDepartment;
    }

    public void setSampleCollectedDepartment(Department sampleCollectedDepartment) {
        this.sampleCollectedDepartment = sampleCollectedDepartment;
    }

    public Institution getSampleCollectedInstitution() {
        return sampleCollectedInstitution;
    }

    public void setSampleCollectedInstitution(Institution sampleCollectedInstitution) {
        this.sampleCollectedInstitution = sampleCollectedInstitution;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    public WebUser getCancelledUser() {
        return cancelledUser;
    }

    public void setCancelledUser(WebUser cancelledUser) {
        this.cancelledUser = cancelledUser;
    }

    public Date getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Date cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellComments() {
        return cancellComments;
    }

    public void setCancellComments(String cancellComments) {
        this.cancellComments = cancellComments;
    }

    public Department getCancellDepartment() {
        return cancellDepartment;
    }

    public void setCancellDepartment(Department cancellDepartment) {
        this.cancellDepartment = cancellDepartment;
    }

    public Institution getCancellInstitution() {
        return cancellInstitution;
    }

    public void setCancellInstitution(Institution cancellInstitution) {
        this.cancellInstitution = cancellInstitution;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getSentToAnalyzer() {
        return sentToAnalyzer;
    }

    public void setSentToAnalyzer(Boolean sentToAnalyzer) {
        this.sentToAnalyzer = sentToAnalyzer;
    }

    public WebUser getSentToAnalyzerBy() {
        return sentToAnalyzerBy;
    }

    public void setSentToAnalyzerBy(WebUser sentToAnalyzerBy) {
        this.sentToAnalyzerBy = sentToAnalyzerBy;
    }

    public Date getSentToAnalyzerAt() {
        return sentToAnalyzerAt;
    }

    public void setSentToAnalyzerAt(Date sentToAnalyzerAt) {
        this.sentToAnalyzerAt = sentToAnalyzerAt;
    }

    public String getSentToAnalyzerComments() {
        return sentToAnalyzerComments;
    }

    public void setSentToAnalyzerComments(String sentToAnalyzerComments) {
        this.sentToAnalyzerComments = sentToAnalyzerComments;
    }

    public Department getSentToAnalyzerDepartment() {
        return sentToAnalyzerDepartment;
    }

    public void setSentToAnalyzerDepartment(Department sentToAnalyzerDepartment) {
        this.sentToAnalyzerDepartment = sentToAnalyzerDepartment;
    }

    public Institution getSentToAnalyzerInstitution() {
        return sentToAnalyzerInstitution;
    }

    public void setSentToAnalyzerInstitution(Institution sentToAnalyzerInstitution) {
        this.sentToAnalyzerInstitution = sentToAnalyzerInstitution;
    }

    public Boolean getReceivedFromAnalyzer() {
        return receivedFromAnalyzer;
    }

    public void setReceivedFromAnalyzer(Boolean receivedFromAnalyzer) {
        this.receivedFromAnalyzer = receivedFromAnalyzer;
    }

    public WebUser getReceivedFromAnalyzerBy() {
        return receivedFromAnalyzerBy;
    }

    public void setReceivedFromAnalyzerBy(WebUser receivedFromAnalyzerBy) {
        this.receivedFromAnalyzerBy = receivedFromAnalyzerBy;
    }

    public Date getReceivedFromAnalyzerAt() {
        return receivedFromAnalyzerAt;
    }

    public void setReceivedFromAnalyzerAt(Date receivedFromAnalyzerAt) {
        this.receivedFromAnalyzerAt = receivedFromAnalyzerAt;
    }

    public String getReceivedFromAnalyzerComments() {
        return receivedFromAnalyzerComments;
    }

    public void setReceivedFromAnalyzerComments(String receivedFromAnalyzerComments) {
        this.receivedFromAnalyzerComments = receivedFromAnalyzerComments;
    }

    public Department getReceivedFromAnalyzerDepartment() {
        return receivedFromAnalyzerDepartment;
    }

    public void setReceivedFromAnalyzerDepartment(Department receivedFromAnalyzerDepartment) {
        this.receivedFromAnalyzerDepartment = receivedFromAnalyzerDepartment;
    }

    public Institution getReceivedFromAnalyzerInstitution() {
        return receivedFromAnalyzerInstitution;
    }

    public void setReceivedFromAnalyzerInstitution(Institution receivedFromAnalyzerInstitution) {
        this.receivedFromAnalyzerInstitution = receivedFromAnalyzerInstitution;
    }

    public Boolean getReadyTosentToAnalyzer() {
        return readyTosentToAnalyzer;
    }

    public void setReadyTosentToAnalyzer(Boolean readyTosentToAnalyzer) {
        this.readyTosentToAnalyzer = readyTosentToAnalyzer;
    }

    public SampleRequestType getSampleRequestType() {
        return sampleRequestType;
    }

    public void setSampleRequestType(SampleRequestType sampleRequestType) {
        this.sampleRequestType = sampleRequestType;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public PatientSample getDuplicatedFrom() {
        return duplicatedFrom;
    }

    public void setDuplicatedFrom(PatientSample duplicatedFrom) {
        this.duplicatedFrom = duplicatedFrom;
    }

    public PatientSample getDuplicatedTo() {
        return duplicatedTo;
    }

    public void setDuplicatedTo(PatientSample duplicatedTo) {
        this.duplicatedTo = duplicatedTo;
    }

    public PatientSample getDivertedFrom() {
        return divertedFrom;
    }

    public void setDivertedFrom(PatientSample divertedFrom) {
        this.divertedFrom = divertedFrom;
    }

    public PatientSample getDivertedTo() {
        return divertedTo;
    }

    public void setDivertedTo(PatientSample divertedTo) {
        this.divertedTo = divertedTo;
    }

    public Boolean getBarcodeGenerated() {
        return barcodeGenerated;
    }

    public void setBarcodeGenerated(Boolean barcodeGenerated) {
        this.barcodeGenerated = barcodeGenerated;
    }

    public WebUser getBarcodeGenerator() {
        return barcodeGenerator;
    }

    public void setBarcodeGenerator(WebUser barcodeGenerator) {
        this.barcodeGenerator = barcodeGenerator;
    }

    public Department getBarcodeGeneratedDepartment() {
        return barcodeGeneratedDepartment;
    }

    public void setBarcodeGeneratedDepartment(Department barcodeGeneratedDepartment) {
        this.barcodeGeneratedDepartment = barcodeGeneratedDepartment;
    }

    public Institution getBarcodeGeneratedInstitution() {
        return barcodeGeneratedInstitution;
    }

    public void setBarcodeGeneratedInstitution(Institution barcodeGeneratedInstitution) {
        this.barcodeGeneratedInstitution = barcodeGeneratedInstitution;
    }

    public Date getBarcodeGeneratedAt() {
        return barcodeGeneratedAt;
    }

    public void setBarcodeGeneratedAt(Date barcodeGeneratedAt) {
        this.barcodeGeneratedAt = barcodeGeneratedAt;
    }

    public Boolean getSampleReceivedAtLab() {
        return sampleReceivedAtLab;
    }

    public void setSampleReceivedAtLab(Boolean sampleReceivedAtLab) {
        this.sampleReceivedAtLab = sampleReceivedAtLab;
    }

    public WebUser getSampleReceiverAtLab() {
        return sampleReceiverAtLab;
    }

    public void setSampleReceiverAtLab(WebUser sampleReceiverAtLab) {
        this.sampleReceiverAtLab = sampleReceiverAtLab;
    }

    public Date getSampleReceivedAtLabAt() {
        return sampleReceivedAtLabAt;
    }

    public void setSampleReceivedAtLabAt(Date sampleReceivedAtLabAt) {
        this.sampleReceivedAtLabAt = sampleReceivedAtLabAt;
    }

    public String getSampleReceivedAtLabComments() {
        return sampleReceivedAtLabComments;
    }

    public void setSampleReceivedAtLabComments(String sampleReceivedAtLabComments) {
        this.sampleReceivedAtLabComments = sampleReceivedAtLabComments;
    }

    public Department getSampleReceivedAtLabDepartment() {
        return sampleReceivedAtLabDepartment;
    }

    public void setSampleReceivedAtLabDepartment(Department sampleReceivedAtLabDepartment) {
        this.sampleReceivedAtLabDepartment = sampleReceivedAtLabDepartment;
    }

    public Institution getSampleReceivedAtLabInstitution() {
        return sampleReceivedAtLabInstitution;
    }

    public void setSampleReceivedAtLabInstitution(Institution sampleReceivedAtLabInstitution) {
        this.sampleReceivedAtLabInstitution = sampleReceivedAtLabInstitution;
    }

    public PatientInvestigationStatus getStatus() {
        return status;
    }

    public void setStatus(PatientInvestigationStatus status) {
        this.status = status;
    }

    public Long getSampleId() {
        if (sampleId == null) {
            sampleId = id;
        }
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public Boolean getSampleSent() {
        return sampleSent;
    }

    public void setSampleSent(Boolean sampleSent) {
        this.sampleSent = sampleSent;
    }

    public WebUser getSampleSentBy() {
        return sampleSentBy;
    }

    public void setSampleSentBy(WebUser sampleSentBy) {
        this.sampleSentBy = sampleSentBy;
    }

    public Date getSampleSentAt() {
        return sampleSentAt;
    }

    public void setSampleSentAt(Date sampleSentAt) {
        this.sampleSentAt = sampleSentAt;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Boolean getSampleRejected() {
        return sampleRejected;
    }

    public void setSampleRejected(Boolean sampleRejected) {
        this.sampleRejected = sampleRejected;
    }

    public WebUser getSampleRejectedBy() {
        return sampleRejectedBy;
    }

    public void setSampleRejectedBy(WebUser sampleRejectedBy) {
        this.sampleRejectedBy = sampleRejectedBy;
    }

    public Date getSampleRejectedAt() {
        return sampleRejectedAt;
    }

    public void setSampleRejectedAt(Date sampleRejectedAt) {
        this.sampleRejectedAt = sampleRejectedAt;
    }

    public Staff getSampleTransportedToLabByStaff() {
        return sampleTransportedToLabByStaff;
    }

    public void setSampleTransportedToLabByStaff(Staff sampleTransportedToLabByStaff) {
        this.sampleTransportedToLabByStaff = sampleTransportedToLabByStaff;
    }

    public String getSampleRejectionComment() {
        return sampleRejectionComment;
    }

    public void setSampleRejectionComment(String sampleRejectionComment) {
        this.sampleRejectionComment = sampleRejectionComment;
    }

    public Boolean getOutsourced() {
        return outsourced;
    }

    public void setOutsourced(Boolean outsourced) {
        this.outsourced = outsourced;
    }

    public Institution getOutsourceInstitution() {
        return outsourceInstitution;
    }

    public void setOutsourceInstitution(Institution outsourceInstitution) {
        this.outsourceInstitution = outsourceInstitution;
    }

    public Department getOutsourceDepartment() {
        return outsourceDepartment;
    }

    public void setOutsourceDepartment(Department outsourceDepartment) {
        this.outsourceDepartment = outsourceDepartment;
    }

    public Date getOutsourcedAt() {
        return outsourcedAt;
    }

    public void setOutsourcedAt(Date outsourcedAt) {
        this.outsourcedAt = outsourcedAt;
    }

    public WebUser getOutsourceSentUser() {
        return outsourceSentUser;
    }

    public void setOutsourceSentUser(WebUser outsourceSentUser) {
        this.outsourceSentUser = outsourceSentUser;
    }

    public Staff getOutsourceSampleTransporter() {
        return outsourceSampleTransporter;
    }

    public void setOutsourceSampleTransporter(Staff outsourceSampleTransporter) {
        this.outsourceSampleTransporter = outsourceSampleTransporter;
    }
    
    public Department getSampleSentToDepartment() {
        return sampleSentToDepartment;
    }

    public void setSampleSentToDepartment(Department sampleSentToDepartment) {
        this.sampleSentToDepartment = sampleSentToDepartment;
    }

    public Institution getSampleSentToInstitution() {
        return sampleSentToInstitution;
    }

    public void setSampleSentToInstitution(Institution sampleSentToInstitution) {
        this.sampleSentToInstitution = sampleSentToInstitution;

    }

    public Boolean getRequestReCollected() {
        return requestReCollected;
    }

    public void setRequestReCollected(Boolean requestReCollected) {
        this.requestReCollected = requestReCollected;
    }

    public PatientSample getReferenceSample() {
        return referenceSample;
    }

    public void setReferenceSample(PatientSample referenceSample) {
        this.referenceSample = referenceSample;
    }

}
