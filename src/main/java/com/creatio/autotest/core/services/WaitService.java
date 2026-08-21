package com.creatio.autotest.core.services;

import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class WaitService {

    private static final Logger log = LoggerFactory.getLogger(WaitService.class);



    @Step("Wait for loader hidden")
    public void waitForLoader() {
        try {
            $x("//div[contains(@class,'ts-loader-mask')]")
                    .should(disappear, Duration.ofSeconds(15));
        } catch (Exception e) {
            log.debug("Loader not found or already hidden");
        }
    }


}