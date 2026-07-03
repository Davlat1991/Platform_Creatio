package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.utils.AllureAttachments;
import io.qameta.allure.Step;

import java.io.File;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class PrintComponent extends BaseComponent {

    @Step("Выбор пункта печати: {name}")
    public void selectPrintOption(String name) {
        log.info("Select print option: '{}'", name);

        try {
            SelenideElement printBtn = $x(
                    "//*[@data-item-marker='PrintButton']"
            ).shouldBe(Condition.visible, Duration.ofSeconds(10));

            executeJavaScript("arguments[0].click();", printBtn);

            SelenideElement item = $x(
                    "//*[contains(@data-item-marker,'" + name + "')" +
                            " or contains(text(),'" + name + "')]"
            ).shouldBe(Condition.visible, Duration.ofSeconds(10));

            executeJavaScript("arguments[0].click();", item);

            log.info("Print option selected: '{}'", name);

            // Печать инициирует скачивание файла — ждём его завершения.
            waitForDownload();

        } catch (Throwable t) {
            AllureAttachments.attachScreenshot("Print failure");
            AllureAttachments.attachPageSource();
            throw t;
        }
    }

    // ══════════════════════════════════════
    // DOWNLOAD
    // Папка загрузок задаётся в DriverSetup: user.dir\downloads.
    // Ждём исчезновения временного .crdownload — признак завершённого скачивания.
    // ══════════════════════════════════════

    @Step("Ожидание завершения скачивания файла")
    public void waitForDownload() {
        log.info("Waiting for download to complete...");

        String downloadPath = System.getProperty("user.dir") + "\\downloads";
        File downloadDir = new File(downloadPath);
        downloadDir.mkdirs();

        // Печатная форма формируется на сервере 10-15с, потом стартует скачивание.
        // Ждём ПОЯВЛЕНИЯ готового файла (не .crdownload) до 60с — ожидание
        // исчезновения .crdownload ненадёжно: файл может ещё не начать качаться.
        int before = countReadyFiles(downloadDir);

        for (int i = 0; i < 60; i++) {
            File[] downloading = downloadDir.listFiles(
                    f -> f.getName().endsWith(".crdownload"));
            boolean inProgress = downloading != null && downloading.length > 0;

            int now = countReadyFiles(downloadDir);

            // Готовый файл появился И нет активной докачки — скачивание завершено
            if (now > before && !inProgress) {
                log.info("Download completed: файлов было {}, стало {}", before, now);
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        log.warn("Download НЕ завершился за 60 секунд");
    }

    private int countReadyFiles(File dir) {
        File[] files = dir.listFiles(
                f -> f.isFile() && !f.getName().endsWith(".crdownload"));
        return files == null ? 0 : files.length;
    }
}