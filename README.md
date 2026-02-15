# SeleniumIQ

AI-powered Selenium test automation plugin for JetBrains IDEs that eliminates test flakiness with intelligent locator generation, self-healing capabilities, and comprehensive test maintenance tools.

## Features

- **AI-powered locator generation** — intelligent multi-attribute analysis generating stable locators (ID, CSS, XPath, data-testid) with stability scoring and dynamic ID filtering (Ctrl+Alt+L)
- **Self-healing locators** — auto-detects broken locators after UI changes, finds similar elements, and offers one-click replacement across the test suite
- **Flakiness prediction** — real-time scoring (0–100) across 25+ metrics with 86% prediction accuracy, detecting anti-patterns like `Thread.sleep()`, absolute XPath, and poor wait strategies
- **Intelligent wait strategies** — analyzes loading patterns and suggests explicit waits, auto-replaces `Thread.sleep()` with `WebDriverWait`, detects AJAX/SPA patterns (React, Angular, Vue)
- **Page Object generation** — auto-generates Page Object Model classes with type-specific methods, built-in explicit waits, and PageFactory integration
- **Code inspections** — real-time warnings for flaky test patterns, brittle locators, and poor wait strategies
- **Locator stability annotations** — inline code annotations showing locator reliability scores
- **Selenium code completion** — intelligent suggestions for Selenium API calls
- **Cross-browser testing** — support for Chrome, Firefox, Safari, and Edge with browser-specific optimization
- **Visual regression testing** — pixel-perfect screenshot comparison with baseline management and configurable thresholds (Premium)
- **Performance analysis** — real-time execution time tracking, slow locator detection, and optimization tips (Premium)
- **Data-driven testing** — load test data from CSV/JSON, auto-generate parameterized tests with type inference for 15+ data types (Premium)
- **CI/CD integration** — one-click pipeline generation for GitHub Actions, GitLab CI, Jenkins, CircleCI, Azure DevOps, and Travis CI (Premium)
- **Cloud platform integration** — BrowserStack, Sauce Labs, LambdaTest, Perfecto with automated RemoteWebDriver configuration (Premium)
- **Test management integration** — Jira/Zephyr, TestRail, qTest with automatic result updates and screenshot attachments (Premium)
- **Team analytics dashboard** — collaboration dashboard with shared metrics and insights
- **Time-travel debugging** — DOM snapshots with visual diff and step-backward execution (Premium)

## Installation

1. **JetBrains Marketplace**: Settings → Plugins → Marketplace → Search "SeleniumIQ"
2. **Manual**: Download from [Releases](https://github.com/JirakJ/selenium-iq-plugin/releases) → Settings → Plugins → ⚙️ → Install from disk

## Requirements

- IntelliJ IDEA 2023.2+ (build 232+)
- Any JetBrains IDE with Java support (IntelliJ IDEA, etc.)
- Selenium WebDriver in your project dependencies

## Configuration

Settings → Tools → SeleniumIQ

Configure AI locator preferences, inspection severity levels, and cloud platform credentials.

## Usage

### Generate AI Locators
Right-click in your test code → SeleniumIQ → **Generate AI Locator** (Ctrl+Alt+L). The AI analyzes the target element and generates multiple locator strategies ranked by stability score.

### Heal Broken Locators
When locators break after UI changes, right-click → SeleniumIQ → **Heal Broken Locators** to auto-detect and fix broken selectors across your test suite.

### Analyze Test Flakiness
Right-click → SeleniumIQ → **Analyze Test Flakiness** to get a reliability score with actionable recommendations for reducing test flakiness.

### Generate Page Objects
Menu → SeleniumIQ → **Generate Page Object** to auto-create a Page Object Model class from the current page structure.

## Building from Source

```bash
./gradlew buildPlugin
```

## License

Copyright © 2026 Jakub Jirák. All rights reserved.
