package com.divudi.core.data.lab;

/**
 * Enum representing the various statuses of a patient investigation.
 *
 * Author: M H B Ariyaratne
 */
public enum PatientInvestigationStatus {

    ORDERED("Ordered"),
    SAMPLE_GENERATED("Barcode Generated"),
    SAMPLE_COLLECTED("Sample Collected"),
    SAMPLE_SENT("Sample Sent"),
    SAMPLE_ACCEPTED("Sample Accepted"),
    SAMPLE_REJECTED("Sample Rejected"),
    SAMPLE_REVERTED("Sample Reverted"),
    SAMPLE_RESENT("Sample Resent"),
    SAMPLE_RECOLLECTED("Sample Recollected"),
    SAMPLE_INTERFACED("Sample Interfaced"),
    SAMPLE_APPROVED("Sample Approved"),
    SAMPLE_REPEATED("Sample Repeated"),
    SAMPLE_APPROVED_AND_REPEATED("Sample Approved and Repeated"),
    REPORT_PRINTED("Report Printed"),
    REPORT_DISTRIBUTED("Report Distributed"),
    REPORT_REACHED_COLLECTING_CENTRE("Report Reached Collecting Centre"),
    REPORT_HANDED_OVER("Report Handed Over"),
    REPORT_CREATED("Report Created"),
    REPORT_APPROVED("Report Approved"),
    SAMPLE_SENT_TO_OUTLAB("Sample Sent to OutLab"),
    SAMPLE_RECOLLECTION_REQUESTED("Sample Recollection Requested"),
    SAMPLE_RECOLLECTION_PENDING("Sample Recollection Pending"),
    SAMPLE_RECOLLECTION_COMPLETE("Sample Recollection Completed"),
    SAMPLE_REGENERATED("Barcode Regenerated");
    
    private final String label;

    PatientInvestigationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
