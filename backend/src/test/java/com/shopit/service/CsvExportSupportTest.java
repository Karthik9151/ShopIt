package com.shopit.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CsvExportSupportTest {

    @Test
    void prefixesSpreadsheetFormulaPrefixesAndQuotesCsvCells() {
        assertThat(CsvExportSupport.cell("=HYPERLINK(\"https://example.invalid\")"))
                .isEqualTo("\"'=HYPERLINK(\"\"https://example.invalid\"\")\"");
        assertThat(CsvExportSupport.cell("  +SUM(A1:A2)"))
                .isEqualTo("\"'  +SUM(A1:A2)\"");
        assertThat(CsvExportSupport.cell("normal \"quoted\" text"))
                .isEqualTo("\"normal \"\"quoted\"\" text\"");
    }

    @Test
    void writesCrLfDelimitedRowsAndEmptyCells() {
        assertThat(CsvExportSupport.row("id", null, 12))
                .isEqualTo("\"id\",\"\",\"12\"\r\n");
    }
}
