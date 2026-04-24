package com.creatio.autotest.utils;

import com.codeborne.selenide.SelenideElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public final class JavaScriptUtils {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptUtils.class);

    private JavaScriptUtils() {}

    // ══════════════════════════════════════
    // CLICK
    // ══════════════════════════════════════

    public static void click(SelenideElement element) {
        log.debug("JS click");
        executeJavaScript("arguments[0].click();", element);
    }

    // ══════════════════════════════════════
    // SCROLL
    // ══════════════════════════════════════

    public static void scrollIntoView(SelenideElement element) {
        log.debug("JS scrollIntoView");
        executeJavaScript(
                "arguments[0].scrollIntoView({block: 'center'});",
                element
        );
    }

    public static void scrollToTop() {
        executeJavaScript("window.scrollTo(0, 0);");
    }

    public static void scrollToBottom() {
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    // ══════════════════════════════════════
    // VALUE
    // ══════════════════════════════════════

    public static void setValue(SelenideElement element, String value) {
        log.debug("JS setValue: {}", value);
        executeJavaScript(
                "arguments[0].value = arguments[1];",
                element, value
        );
    }

    public static String getValue(SelenideElement element) {
        return (String) executeJavaScript(
                "return arguments[0].value;",
                element
        );
    }

    // ══════════════════════════════════════
    // VISIBILITY
    // ══════════════════════════════════════

    public static boolean isVisible(SelenideElement element) {
        Object result = executeJavaScript(
                "var el = arguments[0];" +
                        "return el.offsetParent !== null;",
                element
        );
        return Boolean.TRUE.equals(result);
    }

    // ══════════════════════════════════════
    // PAGE STATE
    // ══════════════════════════════════════

    public static boolean isPageReady() {
        Object result = executeJavaScript(
                "return document.readyState === 'complete';"
        );
        return Boolean.TRUE.equals(result);
    }

    public static String getCurrentUrl() {
        return (String) executeJavaScript("return window.location.href;");
    }

    // ══════════════════════════════════════
    // CREATIO SPECIFIC
    // ══════════════════════════════════════

    public static void fireChangeEvent(SelenideElement element) {
        log.debug("JS fireChangeEvent");
        executeJavaScript(
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                element
        );
    }

    public static void fireBlurEvent(SelenideElement element) {
        log.debug("JS fireBlurEvent");
        executeJavaScript(
                "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
                element
        );
    }

    public static void fixFileApiService() {
        log.debug("JS fix FileApiService");
        executeJavaScript(
                "const originalOpen = XMLHttpRequest.prototype.open;" +
                        "XMLHttpRequest.prototype.open = function(method, url) {" +
                        "  if (url.includes('FileApiService')) {" +
                        "    url = url.replace(/(?:Tsi)*FileApiService/, 'TsiFileApiService');" +
                        "  }" +
                        "  return originalOpen.apply(this, arguments);" +
                        "};"
        );
    }
}