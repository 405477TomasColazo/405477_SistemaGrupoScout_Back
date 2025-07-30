package ar.edu.utn.frc.sistemascoutsjosehernandez.reports.service;

import ar.edu.utn.frc.sistemascoutsjosehernandez.reports.config.JasperReportsConfig;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

@Service
@Slf4j
public class ReportService {

    private final JasperReportsConfig jasperReportsConfig;

    public ReportService(JasperReportsConfig jasperReportsConfig) {
        this.jasperReportsConfig = jasperReportsConfig;
    }

    public byte[] generateReport(String templateName, Map<String, Object> parameters, Collection<?> dataSource) {
        try {
            log.info("Generating report with template: {}", templateName);
            
            // Load and compile the report template
            JasperReport jasperReport = compileTemplate(templateName);
            
            // Create data source if collection is provided
            JRDataSource jrDataSource = null;
            if (dataSource != null && !dataSource.isEmpty()) {
                jrDataSource = new JRBeanCollectionDataSource(dataSource);
            }
            
            // Fill the report with data
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jrDataSource);
            
            // Export to PDF
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            
            log.debug("Report generated successfully. Pages: {}, Size: {} bytes", 
                     jasperPrint.getPages().size(), pdfBytes.length);
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Error generating report with template: {}", templateName, e);
            throw new RuntimeException("Error generating report: " + e.getMessage(), e);
        }
    }

    public byte[] generateReportWithoutDataSource(String templateName, Map<String, Object> parameters) {
        return generateReport(templateName, parameters, null);
    }

    private JasperReport compileTemplate(String templateName) throws JRException {
        try {
            log.debug("Loading template: {}", templateName);
            
            Resource templateResource = jasperReportsConfig.getReportTemplate(templateName);
            
            if (!templateResource.exists()) {
                throw new RuntimeException("Template not found: " + templateName);
            }
            
            try (InputStream templateStream = templateResource.getInputStream()) {
                // Compile the template
                JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
                log.debug("Template compiled successfully: {}", templateName);
                return jasperReport;
            }
            
        } catch (Exception e) {
            log.error("Error compiling template: {}", templateName, e);
            throw new JRException("Error compiling template: " + templateName, e);
        }
    }

    public boolean templateExists(String templateName) {
        try {
            Resource templateResource = jasperReportsConfig.getReportTemplate(templateName);
            return templateResource.exists();
        } catch (Exception e) {
            log.warn("Error checking template existence: {}", templateName, e);
            return false;
        }
    }
}