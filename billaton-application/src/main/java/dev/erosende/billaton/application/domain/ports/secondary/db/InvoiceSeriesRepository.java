package dev.erosende.billaton.application.domain.ports.secondary.db;

public interface InvoiceSeriesRepository {

    String getNextDocumentCode(Integer issuerId, String prefix, int year);
}
