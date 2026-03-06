# Changelog

All notable changes to the SeleniumIQ plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-15

### 🎉 Initial Release

#### Added

**Core Features**
- AI-powered locator generation with stability scoring
- Self-healing locator detection and suggestions
- Flakiness prediction based on 25+ code metrics
- Cross-browser testing support (Chrome, Firefox, Safari, Edge)
- Intelligent wait strategy recommendations
- Advanced debugging with time-travel capabilities

**IDE Integration**
- Syntax highlighting for XPath and CSS selectors
- Code completion for Selenium WebDriver API
- Real-time code inspections for flaky patterns
- Locator stability annotations
- Tool window dashboard
- Settings panel

**Actions**
- Generate AI Locator (Ctrl+Alt+L)
- Heal Broken Locators
- Analyze Test Flakiness
- Generate Page Object
- Generate Test Template

**Inspections**
- Flaky test pattern detection
- Brittle locator identification (absolute XPath, dynamic IDs)
- Poor wait strategy detection (Thread.sleep)
- Locator stability warnings

**Services**
- AI Locator Service with heuristic-based generation
- Self-Healing Service with similarity matching
- Flakiness Analyzer with pattern detection

**User Experience**
- Welcome screen for first-time users
- In-app upgrade prompts at quota limits
- Detailed analytics and statistics
- Keyboard shortcuts for common actions

#### Technical Details

- IntelliJ Platform SDK 2023.2+
- Java 17 target
- Selenium 4.16.1 integration
- Jsoup 1.17.1 for HTML parsing
- Gradle 8.0 build system

#### Documentation

- Comprehensive README with market analysis
- API documentation (JavaDoc)
- User guide and tutorials
- Contributing guidelines
- Commercial license terms

---

## [Unreleased]

### Planned for v1.1.0 (Q1 2025)

#### Added
- [ ] Playwright adapter support
- [ ] Visual regression testing integration
- [ ] BrowserStack/Sauce Labs direct execution
- [ ] Enhanced team analytics dashboard
- [ ] Custom AI model training (Enterprise)

#### Improved
- [ ] AI locator accuracy to 85%+
- [ ] Self-healing confidence scoring
- [ ] Performance optimization for large projects
- [ ] Better error messages and diagnostics

#### Fixed
- [ ] Known issues with dynamic content detection
- [ ] Edge cases in XPath generation
- [ ] Memory usage with large test suites

---

## Version History

### Pre-release Versions

#### [0.9.0-beta] - 2024-12-15
- Beta testing with 50 early adopters
- Core AI locator generation
- Basic self-healing functionality
- Free tier validation

#### [0.8.0-alpha] - 2024-11-01
- Alpha release to 10 users
- Proof of concept for AI features
- Initial IDE integration
- Prototype UI

#### [0.1.0-dev] - 2024-09-01
- Project inception
- Market research and validation
- Technical architecture design
- Development environment setup

---

## Breaking Changes

### v1.0.0

No breaking changes - initial release.

---

## Deprecations

None yet.

---

## Security Updates

### v1.0.0

- No sensitive data storage in IDE

---

## Performance Improvements

### v1.0.0

- Asynchronous AI processing
- Lazy loading of features
- Incremental code analysis
- Optimized DOM parsing

---

## Known Issues

### v1.0.0

1. **Shadow DOM Support**
   - Limited support for shadow DOM elements
   - Workaround: Use JavaScript execution
   - Fix planned: v1.1.0

2. **Large Projects**
   - Performance degradation with 1000+ test files
   - Workaround: Disable real-time analysis
   - Optimization planned: v1.1.0

3. **Cross-Platform**
   - Some keyboard shortcuts conflict on macOS
   - Workaround: Customize in keymap settings
   - Fix planned: v1.0.1

---

## Support

For questions about this changelog:
- Email: support@seleniumiq.com
- Docs: https://seleniumiq.com/docs/changelog
- GitHub: https://github.com/jakubjirak/selenium-iq-plugin/releases

---

**Legend:**
- 🎉 Major release
- ✨ New feature
- 🐛 Bug fix
- 📝 Documentation
- 🔒 Security
- ⚡ Performance
- 💥 Breaking change
