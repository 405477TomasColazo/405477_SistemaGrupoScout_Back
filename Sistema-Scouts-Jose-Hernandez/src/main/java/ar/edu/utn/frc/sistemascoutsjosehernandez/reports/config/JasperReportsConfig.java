package ar.edu.utn.frc.sistemascoutsjosehernandez.reports.config;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperReportsContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.Locale;
import java.util.TimeZone;

@Configuration
@Slf4j
public class JasperReportsConfig {

    private final ResourceLoader resourceLoader;

    public JasperReportsConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public JasperReportsContext jasperReportsContext() {
        DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
        
        // Set default timezone to Argentina
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        
        log.info("JasperReports context configured with timezone: America/Argentina/Buenos_Aires");
        
        return context;
    }

    public Resource getReportTemplate(String templateName) {
        String templatePath = "classpath:reports/templates/" + templateName;
        return resourceLoader.getResource(templatePath);
    }

    public String getTemplatesPath() {
        return "reports/templates/";
    }
}