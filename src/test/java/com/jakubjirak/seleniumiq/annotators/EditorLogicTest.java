package com.jakubjirak.seleniumiq.annotators;

import com.jakubjirak.seleniumiq.inspections.BrittleLocatorInspection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorLogicTest {

    @Test
    void onlyByFactoriesAreTreatedAsLocators() {
        assertEquals("xpath", LocatorStabilityAnnotator.locatorTypeForCall("By", "xpath"));
        assertEquals("cssSelector", LocatorStabilityAnnotator.locatorTypeForCall("org.openqa.selenium.By", "cssSelector"));
        assertEquals("id", LocatorStabilityAnnotator.locatorTypeForCall("By", "id"));
        assertNull(LocatorStabilityAnnotator.locatorTypeForCall("By", "chained"));
        assertNull(LocatorStabilityAnnotator.locatorTypeForCall("Paths", "get"));
        assertNull(LocatorStabilityAnnotator.locatorTypeForCall("String", "format"));
        assertNull(LocatorStabilityAnnotator.locatorTypeForCall(null, "xpath"));
        assertNull(LocatorStabilityAnnotator.locatorTypeForCall("By", null));
    }

    @Test
    void stabilityMessagesCoverLowMiddleAndHighScores() {
        assertTrue(LocatorStabilityAnnotator.message(0.2).startsWith("Low stability locator (20%)"));
        assertNull(LocatorStabilityAnnotator.message(0.4));
        assertNull(LocatorStabilityAnnotator.message(0.79));
        assertEquals("Excellent locator stability (80%)", LocatorStabilityAnnotator.message(0.8));
        assertEquals("Excellent locator stability (100%)", LocatorStabilityAnnotator.message(1.0));
    }

    @Test
    void brittleLocatorReasons() {
        assertTrue(BrittleLocatorInspection.brittleReason("/html/body/div[1]").startsWith("Absolute XPath"));
        assertTrue(BrittleLocatorInspection.brittleReason("#item123456").startsWith("ID contains a long number"));
        assertTrue(BrittleLocatorInspection.brittleReason("//div[@id='row-982341']").startsWith("ID contains a long number"));
        assertTrue(BrittleLocatorInspection.brittleReason("[id='x1234567']").startsWith("ID contains a long number"));
        assertNull(BrittleLocatorInspection.brittleReason("order 123456 was paid"), "plain text with numbers is not a locator");
        assertNull(BrittleLocatorInspection.brittleReason("#login"));
        assertNull(BrittleLocatorInspection.brittleReason("//button[@data-testid='save']"));
    }
}
