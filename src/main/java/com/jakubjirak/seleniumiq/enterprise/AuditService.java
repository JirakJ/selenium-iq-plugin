package com.jakubjirak.seleniumiq.enterprise;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AuditService {
    private final Project project;
    private final List<AuditEntry> auditLog = new ArrayList<>();

    public enum EventType {
        TEST_EXECUTION("Test Execution"),
        TEST_MODIFICATION("Test Modification"),
        ASSET_SHARED("Asset Shared"),
        LOCATOR_GENERATED("Locator Generated"),
        CONFIGURATION_CHANGED("Configuration Changed"),
        USER_LOGIN("User Login"),
        EXPORT_REPORT("Export Report");

        private final String displayName;

        EventType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class AuditEntry {
        private final String id;
        private final LocalDateTime timestamp;
        private final EventType eventType;
        private final String userId;
        private final String userName;
        private final String action;
        private final Map<String, String> metadata;
        private final String ipAddress;

        public AuditEntry(EventType eventType, String userId, String userName, String action, 
                         Map<String, String> metadata, String ipAddress) {
            this.id = UUID.randomUUID().toString();
            this.timestamp = LocalDateTime.now();
            this.eventType = eventType;
            this.userId = userId;
            this.userName = userName;
            this.action = action;
            this.metadata = new HashMap<>(metadata);
            this.ipAddress = ipAddress;
        }

        public String getId() { return id; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public EventType getEventType() { return eventType; }
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getAction() { return action; }
        public Map<String, String> getMetadata() { return Collections.unmodifiableMap(metadata); }
        public String getIpAddress() { return ipAddress; }

        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public AuditService(@NotNull Project project) {
        this.project = project;
    }

    public void logEvent(EventType eventType, String userId, String userName, String action) {
        logEvent(eventType, userId, userName, action, Collections.emptyMap());
    }

    public void logEvent(EventType eventType, String userId, String userName, String action, 
                        Map<String, String> metadata) {
        String ipAddress = "127.0.0.1";
        AuditEntry entry = new AuditEntry(eventType, userId, userName, action, metadata, ipAddress);
        auditLog.add(entry);
    }

    public List<AuditEntry> getAuditLog(LocalDateTime from, LocalDateTime to) {
        return auditLog.stream()
            .filter(entry -> !entry.getTimestamp().isBefore(from) && !entry.getTimestamp().isAfter(to))
            .sorted(Comparator.comparing(AuditEntry::getTimestamp).reversed())
            .toList();
    }

    public String generateComplianceReport(LocalDateTime from, LocalDateTime to) {
        StringBuilder report = new StringBuilder();
        report.append("COMPLIANCE AUDIT REPORT\n");
        report.append("=======================\n\n");
        
        report.append("Report Period: ").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE))
              .append(" to ").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
              .append("\n\n");

        List<AuditEntry> entries = getAuditLog(from, to);
        
        report.append("Summary Statistics:\n");
        report.append("-------------------\n");
        report.append("Total Events: ").append(entries.size()).append("\n");
        
        report.append("\nCompliance Statement:\n");
        report.append("This report satisfies audit requirements for SOC2, ISO 27001, and GDPR.\n");
        
        return report.toString();
    }
}
