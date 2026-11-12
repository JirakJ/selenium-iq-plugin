# SeleniumIQ

[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE) ![Version](https://img.shields.io/badge/version-2026.9.1-green)

> **Free & open source.** Since 2026.8.0 the plugin is completely free — every feature is unlocked for everyone, no license needed — and the source code is available under the Apache License 2.0.

Selenium WebDriver helpers for Java tests in JetBrains IDEs: stable-locator suggestions, locator repair,
flaky-pattern detection, inspections and code generators. Everything runs locally and is rule-based — there is
no network service or machine-learning model behind the "stability" and "flakiness" scores.

## Features

- **Generate Locator** (Ctrl+Alt+L) — ranked locator suggestions (data-testid, id, name, CSS, XPath) with a stability score
- **Heal Broken Locators** — for the selected `By.xxx("...")`, proposes more stable replacements (e.g. dynamic id → `data-testid`/`aria-label`, absolute XPath → relative)
- **Analyze Test Flakiness** — scores a test method by flaky patterns (`Thread.sleep()`, hard-coded waits, missing explicit waits, brittle locators) with recommendations
- **Inspections** — flaky test patterns, brittle locators (absolute XPath, long numeric ids) and `Thread.sleep()` in tests
- **Locator stability annotations** — stability score on string literals passed to `By.id/xpath/cssSelector/...`
- **Completion** — common `driver.findElement(By...)` and WebDriver API entries
- **Generate Page Object** — fetches a page URL with Jsoup and creates a `@FindBy` Page Object (or a template without a URL)
- **Generate Test Template** — JUnit 5, JUnit 4 or TestNG test class with WebDriver setup/teardown, copied to the clipboard
- **Cloud Platform Setup** — RemoteWebDriver starter class for BrowserStack, Sauce Labs or LambdaTest, copied to the clipboard
- **Test Management Integration** — TestNG listener skeleton for Jira or TestRail, copied to the clipboard
- **SeleniumIQ tool window** — buttons for the actions above

## Installation

1. **JetBrains Marketplace**: Settings → Plugins → Marketplace → Search "SeleniumIQ"
2. **Manual**: Download from [Releases](https://github.com/JirakJ/selenium-iq-plugin/releases) → Settings → Plugins → ⚙️ → Install from disk

## Requirements

- IntelliJ IDEA 2024.3+ (build 243+) with Java support
- Selenium WebDriver in your project dependencies

## Usage

### Generate Locators
Right-click in your test code → **Generate Locator** (Ctrl+Alt+L) to get locator strategies ranked by stability score.

### Heal Broken Locators
Select a `By.xxx("...")` expression → right-click → **Heal Broken Locators** to see more stable replacements.

### Analyze Test Flakiness
Right-click inside a test method → **Analyze Test Flakiness** for a risk score with recommendations.

### Generate Page Objects
**SeleniumIQ → Generate Page Object**, enter a page URL (or leave it blank for a template) and a class name.

## Building from Source

```bash
./gradlew buildPlugin
```

## Contributing

Issues and pull requests are welcome at https://github.com/JirakJ/selenium-iq-plugin.

## License

Copyright 2026 Jakub Jirák. Licensed under the [Apache License, Version 2.0](LICENSE).
