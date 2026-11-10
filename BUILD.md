# SeleniumIQ Build & Deployment Guide

## 📦 Project Structure

```
selenium-iq-plugin/
├── build.gradle.kts              # Build configuration
├── settings.gradle.kts           # Gradle settings
├── gradle.properties             # Gradle properties
├── gradlew                       # Gradle wrapper (Unix)
├── gradlew.bat                   # Gradle wrapper (Windows)
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/com/jakubjirak/seleniumiq/
│   │   │   ├── models/          # 3 files - Data models
│   │   │   ├── services/        # 6 files - Business logic
│   │   │   ├── actions/         # 7 files - IDE actions
│   │   │   ├── inspections/     # 3 files - Code inspections
│   │   │   ├── completion/      # 1 file - Code completion
│   │   │   ├── annotators/      # 1 file - Annotations
│   │   │   ├── toolwindow/      # 1 file - Dashboard UI
│   │   │   ├── settings/        # 1 file - Settings UI
│   │   │   └── startup/         # 1 file - Welcome screen
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml   # Plugin configuration
│   │       └── icons/           # Plugin icons
│   └── test/
│       ├── java/                # Unit tests
│       └── resources/           # Test resources
├── README.md                     # Project documentation
├── LICENSE                       # Apache License 2.0
├── CONTRIBUTING.md               # Contribution guide
├── CHANGELOG.md                  # Version history
└── BUILD.md                      # This file

Total: 25 Java files, 1 plugin.xml, comprehensive documentation
```

## 🔨 Build Commands

### Prerequisites

```bash
# Java 17+ required
java -version

# Verify Gradle wrapper
./gradlew --version
```

### Development Build

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run plugin in sandbox IDE
./gradlew runIde

# Verify plugin
./gradlew verifyPlugin
```

### Production Build

```bash
# Build plugin distribution
./gradlew buildPlugin

# Output location: build/distributions/selenium-iq-plugin-1.0.0.zip
```

### Publishing

```bash
# Sign plugin (requires certificate)
export CERTIFICATE_CHAIN="..."
export PRIVATE_KEY="..."
export PRIVATE_KEY_PASSWORD="..."
./gradlew signPlugin

# Publish to JetBrains Marketplace
export PUBLISH_TOKEN="..."
./gradlew publishPlugin
```

## 🧪 Testing

### Unit Tests

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "AILocatorServiceTest"

# Run with coverage
./gradlew test jacocoTestReport
```

### Manual Testing

```bash
# Launch sandbox IDE
./gradlew runIde

# Test with specific IntelliJ version
./gradlew runIde -PideVersion=2023.3
```

### Integration Tests

```bash
# UI tests (if implemented)
./gradlew testUi

# Performance tests
./gradlew testPerformance
```

## 📋 Code Quality

### Linting

```bash
# CheckStyle
./gradlew checkstyleMain checkstyleTest

# SpotBugs
./gradlew spotbugsMain spotbugsTest
```

### Code Coverage

```bash
# Generate coverage report
./gradlew test jacocoTestReport

# View report: build/reports/jacoco/test/html/index.html
```

## 🚀 Deployment Checklist

### Pre-release

- [ ] Update version in build.gradle.kts
- [ ] Update CHANGELOG.md
- [ ] Run full test suite
- [ ] Verify plugin in sandbox IDE
- [ ] Check documentation accuracy
- [ ] Review security vulnerabilities
- [ ] Update screenshots

### Release

- [ ] Build production artifact
- [ ] Sign plugin
- [ ] Create Git tag
- [ ] Publish to JetBrains Marketplace
- [ ] Update website
- [ ] Send release notifications
- [ ] Monitor error reports

### Post-release

- [ ] Monitor download metrics
- [ ] Check user reviews
- [ ] Address critical issues
- [ ] Plan next version

## 🐛 Troubleshooting

### Build Fails

```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# Verbose output
./gradlew build --info --stacktrace
```

### Plugin Won't Load

```bash
# Check plugin.xml syntax
./gradlew verifyPlugin

# Check compatibility
./gradlew verifyPluginConfiguration
```

### Performance Issues

```bash
# Profile build
./gradlew build --profile

# Check dependency tree
./gradlew dependencies
```

## 📊 Metrics Collection

### Build Metrics

```bash
# Build time
./gradlew build --profile

# Output: build/reports/profile/
```

### Plugin Metrics

- Download count: JetBrains Marketplace
- Active users: Telemetry service
- Error rate: Sentry/error tracking
- Performance: IDE profiling

## 🔐 Security

### Dependency Scanning

```bash
# Check for vulnerabilities
./gradlew dependencyCheckAnalyze

# Update dependencies
./gradlew dependencyUpdates
```

### Code Scanning

```bash
# Security scan
./gradlew spotbugsMain

# OWASP dependency check
./gradlew dependencyCheckAnalyze
```

## 📦 Distribution

### Local Install

```bash
# Build plugin
./gradlew buildPlugin

# Install in IntelliJ IDEA:
# 1. Settings → Plugins → ⚙️ → Install Plugin from Disk
# 2. Select: build/distributions/selenium-iq-plugin-1.0.0.zip
```

### JetBrains Marketplace

1. Build and sign plugin
2. Upload to https://plugins.jetbrains.com/
3. Wait for verification (1-3 business days)
4. Plugin becomes available to all users

### Custom Distribution

```bash
# Build custom variant
./gradlew buildPlugin -PvariantName=enterprise

# Include custom configuration
./gradlew buildPlugin -Pconfig=custom-config.properties
```

## 🔄 CI/CD Integration

### GitHub Actions

```yaml
name: Build Plugin

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build
        run: ./gradlew build
      - name: Test
        run: ./gradlew test
      - name: Verify
        run: ./gradlew verifyPlugin
```

### Jenkins

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Publish') {
            when {
                branch 'main'
            }
            steps {
                sh './gradlew publishPlugin'
            }
        }
    }
}
```

## 📈 Performance Optimization

### Build Performance

```bash
# Parallel execution
./gradlew build --parallel

# Build cache
./gradlew build --build-cache

# Daemon
./gradlew build --daemon
```

### Runtime Performance

- Use asynchronous processing
- Lazy load features
- Incremental analysis
- Cache computed results
- Profile with IntelliJ profiler

## 🆘 Support

- Documentation: https://seleniumiq.com/docs/build
- Issues: https://github.com/jakubjirak/selenium-iq-plugin/issues
- Email: dev@seleniumiq.com

---

**Last Updated:** January 2025  
**Plugin Version:** 1.0.0  
**Build System:** Gradle 8.5  
**Target Platform:** IntelliJ IDEA 2023.2+
