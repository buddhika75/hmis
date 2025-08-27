package com.divudi.core.data.lab;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari@gmail.com> and H.K. Damith Deshan <hkddrajapaksha@gmail.com>
 *
 */

/**
 * Enum representing different stages of laboratory test history. ChatGPT contributed.
 */

public enum TestHistoryType {
    ORDERED,
    BARCODE_GENERATED,
    BARCODE_REGENERATED,
    BARCODE_PRINTED,
    BARCODE_PRINTED_INDIVIDUAL ,
    SAMPLE_COLLECTED,
    SAMPLE_RECOLLECT_REQUEST,
    SAMPLE_RECOLLECTED,
    SAMPLE_ACCEPTED,
    SAMPLE_SENT,
    SAMPLE_SENT_OUT_LAB,
    SAMPLE_REJECTED,
    SAMPLE_RECEIVED,
    SAMPLE_TRANSFERRED,
    SAMPLE_PUT_TO_ANALYZER,
    RESULT_RECEIVED_FROM_ANALYZER,
    REPORT_CREATED,
    DATA_ENTERED,
    REPORT_UPLOADED,
    REMOVED_UPLOADED_REPORT,
    REPORT_CALCULATED,
    REPORT_APPROVED,
    REPORT_APPROVED_CANCEL,
    REPORT_VIEWED,
    REPORT_PRINTED,
    SENT_SMS_MANUAL,
    SENT_SMS_AUTO,
    SENT_EMAIL;

    public String getLabel() {
        switch (this) {
            case ORDERED:
                return "Ordered";
            case BARCODE_GENERATED:
                return "Barcode Generated";
            case BARCODE_REGENERATED:
                return "Barcode Regenerated";
            case BARCODE_PRINTED:
                return "Barcode Printed";
            case BARCODE_PRINTED_INDIVIDUAL:
                return "Barcode Printed (Individual)";
            case SAMPLE_COLLECTED:
                return "Sample Collected";
            case SAMPLE_RECOLLECT_REQUEST:
                return "Sample Recollect Request";
            case SAMPLE_RECOLLECTED:
                return "Sample Recollected";
            case SAMPLE_ACCEPTED:
                return "Sample Accepted";
            case SAMPLE_SENT:
                return "Sample Sent";
            case SAMPLE_SENT_OUT_LAB:
                return "Sample Sent to External Lab";
            case SAMPLE_REJECTED:
                return "Sample Rejected";
            case SAMPLE_RECEIVED:
                return "Sample Received";
            case SAMPLE_TRANSFERRED:
                return "Sample Transferred";
            case SAMPLE_PUT_TO_ANALYZER:
                return "Sample Put to Analyzer";
            case RESULT_RECEIVED_FROM_ANALYZER:
                return "Result Received from Analyzer";
            case REPORT_CREATED:
                return "Report Created";
            case DATA_ENTERED:
                return "Data Entered";
            case REPORT_UPLOADED:
                return "Report Uploaded";
            case REMOVED_UPLOADED_REPORT:
                return "Uploaded Report Removed";
            case REPORT_CALCULATED:
                return "Report Corrected";
            case REPORT_APPROVED:
                return "Report Approved";
            case REPORT_APPROVED_CANCEL:
                return "Report Approval Canceled";
            case REPORT_VIEWED:
                return "Report Viewed";
            case REPORT_PRINTED:
                return "Report Printed";
            case SENT_SMS_MANUAL:
                return "SMS Sent (Manual)";
            case SENT_SMS_AUTO:
                return "SMS Sent (Auto)";
            case SENT_EMAIL:
                return "Email Sent";
            default:
                return this.toString();
        }
    }
    
}
