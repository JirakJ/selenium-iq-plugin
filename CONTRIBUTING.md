# Contributing to SeleniumIQ

Thank you for your interest in contributing to SeleniumIQ! This document provides guidelines for contributing to the project.

## 🚀 Getting Started

### Prerequisites

- JDK 17 or higher
- IntelliJ IDEA 2023.2+
- Gradle 8.0+
- Git

### Development Setup

1. **Fork the repository**
   ```bash
   git clone https://github.com/jakubjirak/selenium-iq-plugin.git
   cd selenium-iq-plugin
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Run in development mode**
   ```bash
   ./gradlew runIde
   ```

## 📝 Contribution Guidelines

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods focused and concise
- Maximum line length: 120 characters

### Commit Messages

Use conventional commit format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test additions/changes
- `chore`: Build/tooling changes

Example:
```
feat(locator): add support for shadow DOM elements

- Implement shadow root traversal
- Add tests for shadow DOM locators
- Update documentation

Closes #123
```

### Pull Request Process

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write clean, maintainable code
   - Add unit tests
   - Update documentation

3. **Test thoroughly**
   ```bash
   ./gradlew test
   ./gradlew runIde  # Manual testing
   ```

4. **Submit pull request**
   - Provide clear description
   - Reference related issues
   - Ensure CI passes

### Testing

- Write unit tests for new features
- Maintain test coverage above 80%
- Test manually in IDE sandbox
- Test across multiple IntelliJ versions

## 🐛 Bug Reports

When reporting bugs, include:

1. **Environment**
   - IntelliJ IDEA version
   - Plugin version
   - Operating system
   - Java version

2. **Steps to reproduce**
   - Clear, numbered steps
   - Minimal code example
   - Expected vs actual behavior

3. **Screenshots/logs**
   - Error messages
   - Stack traces
   - IDE log excerpts

Use the bug report template: [.github/ISSUE_TEMPLATE/bug_report.md]

## 💡 Feature Requests

For feature requests, include:

1. **Problem statement**
   - What problem does this solve?
   - Who benefits from this feature?

2. **Proposed solution**
   - How should it work?
   - UI/UX considerations
   - Example use cases

3. **Alternatives**
   - Other approaches considered
   - Workarounds currently used

Use the feature request template: [.github/ISSUE_TEMPLATE/feature_request.md]

## 📚 Documentation

Documentation improvements are always welcome:

- Fix typos and grammar
- Add examples and tutorials
- Improve clarity and completeness
- Update screenshots

## 🎨 UI/UX Contributions

For UI changes:

- Follow IntelliJ UI guidelines
- Maintain consistency with IDE theme
- Test in light and dark themes
- Provide before/after screenshots

## 🔒 Security

Report security vulnerabilities privately to:
security@seleniumiq.com

Do not create public issues for security concerns.

## 📄 License

By contributing, you agree that your contributions will be licensed under the same license as the project (Apache License 2.0).

## 🤝 Code of Conduct

### Our Standards

- Be respectful and inclusive
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards others

### Unacceptable Behavior

- Harassment or discrimination
- Trolling or insulting comments
- Personal or political attacks
- Publishing others' private information

## 🏆 Recognition

Contributors are recognized in:
- CONTRIBUTORS.md file
- Release notes
- Project website (with permission)

## 📞 Questions?

- Email: dev@seleniumiq.com
- Discord: https://discord.gg/seleniumiq
- GitHub Discussions: Use for questions

## 🎯 Priority Areas

We especially welcome contributions in:

1. **AI/ML improvements**
   - Locator stability prediction
   - Self-healing accuracy
   - Flakiness detection

2. **Framework support**
   - Playwright adapter
   - Cypress integration
   - Mobile testing

3. **Integrations**
   - CI/CD platforms
   - Test management tools
   - Cloud testing services

4. **Documentation**
   - Tutorials and guides
   - Video content
   - Translation (i18n)

5. **Performance**
   - IDE responsiveness
   - Memory optimization
   - Startup time

Thank you for contributing to SeleniumIQ! 🚀
