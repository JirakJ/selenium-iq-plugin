# Copilot Prompt — Implement Visual Regression Testing Feature

## Context

This is a JetBrains IntelliJ Platform plugin written in Java/Kotlin targeting IntelliJ IDEA 2024.3+ (build 243).
The plugin root is `selenium-iq-plugin/`, base package `com.jakubjirak.seleniumiq`.

The feature scaffold already exists:
- `src/main/kotlin/com/jakubjirak/seleniumiq/features/VisualRegressionAction.kt` — currently just shows an info dialog
- `src/main/kotlin/com/jakubjirak/seleniumiq/features/VisualTestingService.kt` — currently empty project service
- `src/main/resources/META-INF/plugin.xml` — action `SeleniumIQ.VisualRegression` is already registered

## What to implement

Replace the placeholder dialog in `VisualRegressionAction` with a working visual regression workflow:

### Step 1 — Capture mode (baseline)

1. Show a dialog asking for:
   - **Test name** (used as the snapshot identifier, e.g. `login-page-initial`)
   - **Screenshot source**: one of:
     a. **File** — pick an existing PNG/JPEG from disk via `FileChooserDescriptorFactory.createSingleFileDescriptor("png","jpg","jpeg")`
     b. **URL** — enter a URL; the plugin fetches a screenshot using a headless approach (see note below)
   - **Mode**: `Save as baseline` or `Compare with baseline`

2. If **Save as baseline**: save the image to `.idea/seleniumiq-baselines/<testName>.png` inside the project.
   Use `ProjectUtil.guessProjectDir(project)` to locate the project root.
   Create the directory if it does not exist.

3. If **Compare with baseline**: load the stored baseline PNG and compare pixel-by-pixel with the new screenshot.

### Step 2 — Diff algorithm

Implement pixel-level comparison in `VisualTestingService`:

```kotlin
fun compare(baseline: BufferedImage, current: BufferedImage): DiffResult
```

- Resize both images to the same dimensions before comparing (use `java.awt.Image.getScaledInstance`)
- For each pixel compute the CIEDE2000 colour distance OR a simpler RGB Euclidean distance
- A pixel is "different" if its distance exceeds a configurable threshold (default: 10 out of 255)
- Return a `DiffResult` containing:
  - `diffPixelCount: Int`
  - `diffPercentage: Double`
  - `diffImage: BufferedImage` — original image with different pixels highlighted in red (#FF0000)
  - `passed: Boolean` — true if `diffPercentage` is below a tolerance (default: 0.5%)

### Step 3 — Result display

Show the diff result in a dedicated IntelliJ tool window panel (reuse the existing `SeleniumIQ` tool window or create a new tab):

- Side-by-side: baseline | current | diff thumbnails (scale to max 400 px wide each)
- Summary text: "X pixels differ (Y%) — PASSED / FAILED"
- If FAILED, offer a button **Update baseline** that saves the current image as the new baseline
- Use `ImageIO.write` to save and `ImageIO.read` to load images
- Render thumbnails using a `JLabel` with `ImageIcon`

### Step 4 — Settings

Add two fields to `SeleniumIQConfigurable.java` (the existing settings panel):
- `diffThreshold` (int, 0–255, default 10) — per-pixel colour distance tolerance
- `passThreshold` (double, 0–100, default 0.5) — max allowed diff percentage

Persist settings using IntelliJ's `@State` / `PersistentStateComponent` on `VisualTestingService`.

### Step 5 — Screenshot from URL (optional, best-effort)

If the user picks the **URL** source option:
- Use `java.awt.Desktop.browse(URI)` to open the URL in the default browser and show a message
  "Take a screenshot manually, then pick the file with the File option."
- **Do NOT** attempt to launch a WebDriver session from within the plugin — this would require bundling
  browser drivers and is out of scope. Document this limitation clearly in a tooltip.

### Existing dependencies available (already in build.gradle.kts)

- Gson (for serialising metadata if needed)
- Jsoup (HTTP, not needed here)
- No new dependencies should be added — use JDK `ImageIO`, `BufferedImage`, `Graphics2D`

### Tests to write

In `src/test/kotlin/com/jakubjirak/seleniumiq/features/`:

```kotlin
class VisualTestingServiceTest {
    @Test fun `identical images produce zero diff`()
    @Test fun `single red pixel produces non-zero diff and marks pixel in diff image`()
    @Test fun `diff percentage below threshold reports passed`()
    @Test fun `diff percentage above threshold reports failed`()
    @Test fun `images of different sizes are normalised before comparison`()
}
```

Do NOT use IntelliJ Platform test infrastructure — instantiate `VisualTestingService` directly
(same pattern as `AILocatorServiceTest`).

### Coding conventions

- Kotlin for new `.kt` files, Java for modifications to existing `.java` files
- KDoc on all public classes and methods
- License header on every new file:
  ```
  /*
   * Copyright (c) 2025 Jakub Jirák. All rights reserved.
   * Licensed under the proprietary license. See LICENSE for details.
   */
  ```
- Use `JBLabel`, `JBScrollPane`, `JBUI.Borders` for all Swing components
- Never block the EDT — use `Task.Backgroundable` for image loading/comparison
- After implementation run `./gradlew test buildPlugin verifyPlugin`; the verifier report must be
  **zero errors, zero warnings, zero deprecated-API usages** against IC-243 and IC-251

### Acceptance criteria

- [ ] Baseline can be saved from a file picked in a dialog
- [ ] Comparison produces a diff image with changed pixels highlighted in red
- [ ] Pass/fail result is displayed in the tool window with percentage
- [ ] "Update baseline" button works
- [ ] Settings (thresholds) are persisted across IDE restarts
- [ ] All 5 unit tests pass
- [ ] Plugin verifier clean
