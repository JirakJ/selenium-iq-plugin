package com.jakubjirak.seleniumiq.actions;

import com.jakubjirak.seleniumiq.models.LocatorSuggestion;
import com.jakubjirak.seleniumiq.util.LocatorParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionLogicTest {

    // ---- Generate Test Template --------------------------------------------------------------

    @Test
    void templatesCompileShapedForEachFramework() {
        String junit5 = GenerateTestTemplateAction.buildTemplate("JUnit 5", " LoginTest ");
        assertNotNull(junit5);
        assertTrue(junit5.contains("public class LoginTest {"));
        assertTrue(junit5.contains("import org.junit.jupiter.api.Test;"));
        assertTrue(junit5.contains("@BeforeEach") && junit5.contains("@AfterEach"));
        assertTrue(junit5.contains("driver.quit();"));
        assertFalse(junit5.contains("Thread.sleep(") , "the template must not use Thread.sleep");

        String junit4 = GenerateTestTemplateAction.buildTemplate("JUnit 4", "A");
        assertTrue(junit4.contains("import org.junit.Before;") && junit4.contains("@After\n"));

        String testng = GenerateTestTemplateAction.buildTemplate("TestNG", "B");
        assertTrue(testng.contains("import org.testng.annotations.BeforeMethod;"));
        assertTrue(testng.contains("import static org.testng.Assert.assertTrue;"));
    }

    @Test
    void templateRejectsBadInput() {
        assertNull(GenerateTestTemplateAction.buildTemplate("JUnit 5", null));
        assertNull(GenerateTestTemplateAction.buildTemplate("JUnit 5", "  "));
        assertNull(GenerateTestTemplateAction.buildTemplate("JUnit 5", "1Bad"));
        assertNull(GenerateTestTemplateAction.buildTemplate("JUnit 5", "has space"));
        assertNull(GenerateTestTemplateAction.buildTemplate("Spock", "Fine"));
        assertEquals(3, GenerateTestTemplateAction.FRAMEWORKS.length);
    }

    // ---- Generate Page Object ----------------------------------------------------------------

    private static Element el(String html) {
        return Jsoup.parseBodyFragment(html).body().child(0);
    }

    @Test
    void fieldPrefersDataTestIdThenIdThenNameThenAria() {
        GeneratePageObjectAction.FieldDef byTestId = GeneratePageObjectAction.toField(el("<input data-testid='email' id='x' type='text'>"));
        assertEquals("cssSelector", byTestId.how());
        assertEquals("[data-testid='email']", byTestId.using());
        assertEquals("emailField", byTestId.fieldName());

        GeneratePageObjectAction.FieldDef byId = GeneratePageObjectAction.toField(el("<button id='save-btn'>Save</button>"));
        assertEquals("id", byId.how());
        assertEquals("saveBtnButton", byId.fieldName());

        GeneratePageObjectAction.FieldDef byName = GeneratePageObjectAction.toField(el("<select id='a12345678' name='country'></select>"));
        assertEquals("name", byName.how());
        assertEquals("countryDropdown", byName.fieldName());

        GeneratePageObjectAction.FieldDef byAria = GeneratePageObjectAction.toField(el("<a aria-label='Home page'>x</a>"));
        assertEquals("[aria-label='Home page']", byAria.using());
        assertEquals("homePageLink", byAria.fieldName());
    }

    @Test
    void fieldFallsBackToCompoundCssOrIsSkipped() {
        GeneratePageObjectAction.FieldDef css = GeneratePageObjectAction.toField(el("<input type='checkbox' placeholder='Agree'>"));
        assertEquals("input[type='checkbox'][placeholder='Agree']", css.using());
        assertEquals("agreeCheckbox", css.fieldName());
        assertNull(GeneratePageObjectAction.toField(el("<div></div>")));
        assertNull(GeneratePageObjectAction.toField(el("<span>Some very long text that is definitely not a name</span>")));
    }

    @Test
    void namingHelpers() {
        assertEquals("loginForm", GeneratePageObjectAction.toCamelCase("Login form"));
        assertEquals("userEmailAddress", GeneratePageObjectAction.toCamelCase("user_email-address"));
        assertEquals("el2fa", GeneratePageObjectAction.toCamelCase("2FA"));
        assertEquals("Button", GeneratePageObjectAction.typeSuffix("input", "SUBMIT"));
        assertEquals("Radio", GeneratePageObjectAction.typeSuffix("input", "radio"));
        assertEquals("Field", GeneratePageObjectAction.typeSuffix("input", ""));
        assertEquals("TextArea", GeneratePageObjectAction.typeSuffix("textarea", ""));
        assertEquals("Element", GeneratePageObjectAction.typeSuffix("div", ""));
        assertTrue(GeneratePageObjectAction.isDynamic("item-123456"));
        assertTrue(GeneratePageObjectAction.isDynamic("deadbeef-1234-x"));
        assertTrue(GeneratePageObjectAction.isDynamic("generated-id"));
        assertFalse(GeneratePageObjectAction.isDynamic("submit"));
    }

    @Test
    void buildSourceEmitsFindByFieldsConstructorAndGetters() {
        List<GeneratePageObjectAction.FieldDef> fields = GeneratePageObjectAction.templateFields();
        String src = GeneratePageObjectAction.buildSource("com.example.pages", "LoginPage", fields);
        assertTrue(src.contains("package com.example.pages;"));
        assertTrue(src.contains("@FindBy(id = \"username\")"));
        assertTrue(src.contains("public LoginPage(WebDriver driver) {"));
        assertTrue(src.contains("public WebElement getSubmitButton() {"));
        assertFalse(src.contains("AI-Powered"));
        String commented = GeneratePageObjectAction.buildSource("p", "P",
                List.of(new GeneratePageObjectAction.FieldDef("WebElement", "x", null, null, "// TODO: add @FindBy")));
        assertTrue(commented.contains("// TODO: add @FindBy"));
        assertFalse(commented.contains("getX()"));
    }

    // ---- Heal Broken Locators -----------------------------------------------------------------

    private static List<String> healed(String code) {
        LocatorParser.ParsedLocator parsed = LocatorParser.parse(code);
        assertNotNull(parsed, code);
        return HealLocatorsAction.buildHealingSuggestions(parsed).stream()
                .map(LocatorSuggestion::getLocatorValue).collect(Collectors.toList());
    }

    @Test
    void dynamicIdGetsAttributeBasedReplacements() {
        List<String> values = healed("By.id(\"user-1234567\")");
        assertTrue(values.contains("[data-testid='user-']"), values.toString());
        assertTrue(values.contains("[id*='user-']"));
    }

    @Test
    void absoluteXpathBecomesRelative() {
        List<String> values = healed("By.xpath(\"/html/body/div[2]/button\")");
        assertTrue(values.contains("//button"), values.toString());
        assertTrue(values.contains("button[data-testid]"));
    }

    @Test
    void stableLocatorsOnlyGetTheDataTestIdRecommendation() {
        List<String> values = healed("By.id(\"login\")");
        assertEquals(List.of("[data-testid='login']"), values);
        assertEquals(1, healed("By.cssSelector(\"#main [role='nav']\")").size());
    }

    @Test
    void classNameAndBareCssAreAnchored() {
        assertTrue(healed("By.className(\"btn primary\")").contains(".btn"));
        assertTrue(healed("By.cssSelector(\"div.item\")").contains("div.item[data-testid]"));
        assertTrue(healed("By.name(\"field_99999\")").contains("[data-testid='field']"));
    }
}
