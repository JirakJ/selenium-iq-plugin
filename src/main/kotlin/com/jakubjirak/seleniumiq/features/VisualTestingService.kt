package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Service(Service.Level.PROJECT)
class VisualTestingService(private val project: Project) {
    
    data class VisualDiff(
        val baselineImage: File,
        val currentImage: File,
        val diffImage: File?,
        val diffPercentage: Double,
        val pixelsDifferent: Int,
        val totalPixels: Int,
        val passed: Boolean
    )
    
    data class VisualTestConfig(
        val threshold: Double = 0.1,
        val ignoreAntialiasing: Boolean = true,
        val ignoreColors: Boolean = false,
        val ignoreRegions: List<Region> = emptyList()
    )
    
    data class Region(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )
    
    fun compareScreenshots(
        baselinePath: String,
        currentPath: String,
        config: VisualTestConfig = VisualTestConfig()
    ): VisualDiff {
        val baselineFile = File(baselinePath)
        val currentFile = File(currentPath)
        
        if (!baselineFile.exists() || !currentFile.exists()) {
            throw IllegalArgumentException("Baseline or current screenshot not found")
        }
        
        val baselineImg = ImageIO.read(baselineFile)
        val currentImg = ImageIO.read(currentFile)
        
        if (baselineImg.width != currentImg.width || baselineImg.height != currentImg.height) {
            throw IllegalArgumentException("Image dimensions do not match")
        }
        
        val diff = calculateDiff(baselineImg, currentImg, config)
        val diffPercentage = (diff.toDouble() / (baselineImg.width * baselineImg.height)) * 100
        
        val diffImage = if (diff > 0) {
            generateDiffImage(baselineImg, currentImg, config)
        } else null
        
        val diffFile = if (diffImage != null) {
            val diffPath = currentPath.replace(".png", "_diff.png")
            val file = File(diffPath)
            ImageIO.write(diffImage, "PNG", file)
            file
        } else null
        
        return VisualDiff(
            baselineImage = baselineFile,
            currentImage = currentFile,
            diffImage = diffFile,
            diffPercentage = diffPercentage,
            pixelsDifferent = diff,
            totalPixels = baselineImg.width * baselineImg.height,
            passed = diffPercentage <= config.threshold
        )
    }
    
    private fun calculateDiff(
        baseline: BufferedImage,
        current: BufferedImage,
        config: VisualTestConfig
    ): Int {
        var diff = 0
        
        for (y in 0 until baseline.height) {
            for (x in 0 until baseline.width) {
                if (isInIgnoreRegion(x, y, config.ignoreRegions)) {
                    continue
                }
                
                val baselineRgb = baseline.getRGB(x, y)
                val currentRgb = current.getRGB(x, y)
                
                if (baselineRgb != currentRgb) {
                    if (config.ignoreColors) {
                        val baselineGray = toGrayscale(baselineRgb)
                        val currentGray = toGrayscale(currentRgb)
                        if (Math.abs(baselineGray - currentGray) > 10) {
                            diff++
                        }
                    } else {
                        diff++
                    }
                }
            }
        }
        
        return diff
    }
    
    private fun generateDiffImage(
        baseline: BufferedImage,
        current: BufferedImage,
        config: VisualTestConfig
    ): BufferedImage {
        val diffImg = BufferedImage(baseline.width, baseline.height, BufferedImage.TYPE_INT_ARGB)
        
        for (y in 0 until baseline.height) {
            for (x in 0 until baseline.width) {
                val baselineRgb = baseline.getRGB(x, y)
                val currentRgb = current.getRGB(x, y)
                
                if (baselineRgb != currentRgb) {
                    diffImg.setRGB(x, y, 0xFFFF0000.toInt())
                } else {
                    diffImg.setRGB(x, y, currentRgb and 0x40FFFFFF.toInt())
                }
            }
        }
        
        return diffImg
    }
    
    private fun isInIgnoreRegion(x: Int, y: Int, regions: List<Region>): Boolean {
        return regions.any { region ->
            x >= region.x && x < region.x + region.width &&
            y >= region.y && y < region.y + region.height
        }
    }
    
    private fun toGrayscale(rgb: Int): Int {
        val r = (rgb shr 16) and 0xFF
        val g = (rgb shr 8) and 0xFF
        val b = rgb and 0xFF
        return (r + g + b) / 3
    }
    
    fun createBaseline(screenshotPath: String, baselinePath: String) {
        val source = File(screenshotPath)
        val target = File(baselinePath)
        
        source.copyTo(target, overwrite = true)
    }
    
    fun generateVisualTestReport(results: List<VisualDiff>): String {
        val passed = results.count { it.passed }
        val failed = results.count { !it.passed }
        
        return buildString {
            appendLine("# Visual Testing Report")
            appendLine()
            appendLine("**Summary:**")
            appendLine("- Total Tests: ${results.size}")
            appendLine("- Passed: $passed")
            appendLine("- Failed: $failed")
            appendLine("- Pass Rate: ${(passed.toDouble() / results.size * 100).format(2)}%")
            appendLine()
            appendLine("## Test Results")
            appendLine()
            
            results.forEachIndexed { index, diff ->
                appendLine("### Test ${index + 1}: ${if (diff.passed) "✓ PASSED" else "✗ FAILED"}")
                appendLine("- Baseline: `${diff.baselineImage.name}`")
                appendLine("- Current: `${diff.currentImage.name}`")
                appendLine("- Difference: ${diff.diffPercentage.format(2)}%")
                appendLine("- Pixels Different: ${diff.pixelsDifferent} / ${diff.totalPixels}")
                if (diff.diffImage != null) {
                    appendLine("- Diff Image: `${diff.diffImage.name}`")
                }
                appendLine()
            }
        }
    }
    
    private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
}
