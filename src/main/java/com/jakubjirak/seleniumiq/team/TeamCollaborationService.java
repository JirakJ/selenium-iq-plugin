package com.jakubjirak.seleniumiq.team;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

public class TeamCollaborationService {
    private final Project project;
    private final Map<String, SharedTestAsset> sharedAssets = new HashMap<>();
    private final List<TeamMember> teamMembers = new ArrayList<>();

    public static class SharedTestAsset {
        private final String id;
        private final String name;
        private final String content;
        private final String type;
        private final String author;
        private final LocalDateTime created;
        private final LocalDateTime modified;
        private int usageCount;

        public SharedTestAsset(String id, String name, String content, String type, String author) {
            this.id = id;
            this.name = name;
            this.content = content;
            this.type = type;
            this.author = author;
            this.created = LocalDateTime.now();
            this.modified = LocalDateTime.now();
            this.usageCount = 0;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getContent() { return content; }
        public String getType() { return type; }
        public String getAuthor() { return author; }
        public LocalDateTime getCreated() { return created; }
        public LocalDateTime getModified() { return modified; }
        public int getUsageCount() { return usageCount; }
        
        public void incrementUsage() {
            usageCount++;
        }
    }

    public static class TeamMember {
        private final String id;
        private final String name;
        private final String email;
        private final String role;
        private int testsWritten;
        private int testsReviewed;
        private double averageTestQuality;

        public TeamMember(String id, String name, String email, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.testsWritten = 0;
            this.testsReviewed = 0;
            this.averageTestQuality = 0.0;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public int getTestsWritten() { return testsWritten; }
        public int getTestsReviewed() { return testsReviewed; }
        public double getAverageTestQuality() { return averageTestQuality; }
    }

    public TeamCollaborationService(@NotNull Project project) {
        this.project = project;
    }

    public boolean shareTestAsset(String name, String content, String type) {
        String id = UUID.randomUUID().toString();
        SharedTestAsset asset = new SharedTestAsset(id, name, content, type, "current-user");
        sharedAssets.put(id, asset);
        
        return true;
    }

    public List<SharedTestAsset> getSharedAssets() {
        return new ArrayList<>(sharedAssets.values());
    }

    public Optional<SharedTestAsset> getAssetById(String id) {
        return Optional.ofNullable(sharedAssets.get(id));
    }

    public Map<String, Object> getTeamAnalytics() {

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalSharedAssets", sharedAssets.size());
        analytics.put("totalTeamMembers", teamMembers.size());
        
        int totalUsage = sharedAssets.values().stream()
            .mapToInt(SharedTestAsset::getUsageCount)
            .sum();
        analytics.put("totalAssetUsage", totalUsage);
        
        List<SharedTestAsset> mostUsed = sharedAssets.values().stream()
            .sorted((a, b) -> Integer.compare(b.getUsageCount(), a.getUsageCount()))
            .limit(10)
            .toList();
        analytics.put("mostUsedAssets", mostUsed);
        
        int totalTestsWritten = teamMembers.stream()
            .mapToInt(TeamMember::getTestsWritten)
            .sum();
        analytics.put("totalTestsWritten", totalTestsWritten);
        
        double avgQuality = teamMembers.stream()
            .mapToDouble(TeamMember::getAverageTestQuality)
            .average()
            .orElse(0.0);
        analytics.put("averageTestQuality", avgQuality);
        
        return analytics;
    }

    public String generateTeamReport() {

        StringBuilder report = new StringBuilder();
        report.append("Team Analytics Report\n");
        report.append("=====================\n\n");
        
        Map<String, Object> analytics = getTeamAnalytics();
        
        report.append("Team Overview:\n");
        report.append("  • Team Members: ").append(analytics.get("totalTeamMembers")).append("\n");
        report.append("  • Shared Assets: ").append(analytics.get("totalSharedAssets")).append("\n");
        report.append("  • Total Tests Written: ").append(analytics.get("totalTestsWritten")).append("\n");
        report.append("  • Average Test Quality: ").append(String.format("%.1f%%", 
            (Double) analytics.get("averageTestQuality"))).append("\n\n");
        
        report.append("Asset Usage:\n");
        @SuppressWarnings("unchecked")
        List<SharedTestAsset> mostUsed = (List<SharedTestAsset>) analytics.get("mostUsedAssets");
        for (int i = 0; i < Math.min(5, mostUsed.size()); i++) {
            SharedTestAsset asset = mostUsed.get(i);
            report.append("  ").append(i + 1).append(". ")
                  .append(asset.getName())
                  .append(" (").append(asset.getUsageCount()).append(" uses)\n");
        }
        
        return report.toString();
    }

    public boolean enforceStandards(String testCode) {

        List<String> violations = new ArrayList<>();
        
        if (!testCode.matches(".*@Test\\s+public\\s+void\\s+test[A-Z].*")) {
            violations.add("Test method name should start with 'test' followed by uppercase letter");
        }
        
        if (testCode.contains("findElement") && !testCode.contains("@FindBy")) {
            violations.add("Consider using Page Object Model with @FindBy annotations");
        }
        
        if (testCode.contains("Thread.sleep")) {
            violations.add("Avoid Thread.sleep(), use explicit waits instead");
        }
        
        if (testCode.contains("@Test") && !testCode.matches(".*(assert|verify|expect).*")) {
            violations.add("Test should contain at least one assertion");
        }
        
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("Team Standards Violations Detected:\n\n");
            for (String violation : violations) {
                message.append("• ").append(violation).append("\n");
            }
            message.append("\nPlease fix these issues before committing.");
            
            Messages.showWarningDialog(project, message.toString(), "Team Standards Enforcement");
            return false;
        }
        
        return true;
    }

}
