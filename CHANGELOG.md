# Changelog

## [2026.8.0] - 2026-11-10

### Changed
- The plugin is now completely free — all features unlocked, no license needed.
- Source code is now open source under the Apache License 2.0: https://github.com/JirakJ/selenium-iq-plugin

## [2026.5.3] - 2026-05-19

### Changed
- Version bump and Marketplace re-publish to confirm all 2026.5.x changes
  are live (button fix + Heal Broken Locators + Generate Page Object).



### Added
- **Heal Broken Locators — fully implemented**: selecting a `By.*` expression in the editor and
  clicking Heal now performs static analysis and shows a ranked popup of stable replacements.
  Handles dynamic IDs (numeric/UUID suffixes), absolute XPath (`/html/body/...`), bare class names,
  and unstable CSS selectors. Choosing a suggestion replaces the selection in-place. No running
  browser or page HTML required.
- **Generate Page Object — fully implemented**: generates a real `.java` source file with
  `@FindBy` + `PageFactory` from either (a) interactive elements fetched from a live URL via Jsoup,
  or (b) a best-practice template when no URL is provided. The file is written into the project
  source tree and opened in the editor automatically. Field names and locator strategies are derived
  from `data-testid`, `id`, `name`, `aria-label`, and compound CSS in order of stability.
- **`LocatorParser` utility**: new `com.jakubjirak.seleniumiq.util.LocatorParser` class for
  parsing `By.*` expressions, detecting dynamic IDs, and detecting absolute XPath — shared by
  Heal Broken Locators and available for future features.
- **GitHub Copilot prompt** for the Visual Regression feature:
  `.github/copilot-prompts/visual-regression-feature.md` — detailed spec covering diff algorithm,
  baseline management, result display, settings persistence, and acceptance criteria.

### Tests
- 14 new unit tests for `LocatorParser` (parse, isDynamic, isAbsoluteXPath — all quote styles,
  edge cases, multi-locator snippets).



### Fixed
- **Dashboard buttons now work**: All five tool window buttons ("Generate AI Locator", "Heal Broken Locators",
  "Analyze Flakiness", "Generate Page Object", "Visual Regression Test") previously had empty action listeners
  and did nothing when clicked. Each button is now properly wired to its corresponding action logic.
- **"Generate Page Object" and "Visual Regression Test" buttons added** to the tool window dashboard
  (they existed in the menu but were missing from the panel).
- **Editor context menu actions fixed**: "Generate AI Locator", "Heal Broken Locators", and "Analyze Test Flakiness"
  were declared inside the `<extensions>` block in `plugin.xml` instead of the `<actions>` block, preventing them
  from appearing in the editor right-click context menu. Moved to `<actions>`.
- **Settings menu entry fixed**: "SeleniumIQ Settings" added to the SeleniumIQ top-level menu.
- **Graceful "no editor" feedback**: Dashboard buttons that require an open editor now show a clear info dialog
  instead of silently doing nothing.
- **Flakiness analysis uses caret position**: When the cursor is inside a test method, "Analyze Flakiness"
  now analyzes that specific method rather than the whole file.
- Replaced `JLabel` with `JBLabel` in the dashboard panel for proper IntelliJ theme support.


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
