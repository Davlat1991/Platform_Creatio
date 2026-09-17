package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.utils.AllureAttachments;
import io.qameta.allure.Step;
import org.openqa.selenium.support.ui.FluentWait;
import com.creatio.autotest.core.verify.VerificationCollector;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class PrintComponent extends BaseComponent {

    @Step("Выбор пункта печати: {name}")
    public void selectPrintOption(String name) {
        log.info("Select print option: '{}'", name);

        try {
            SelenideElement printBtn = $x(
                    "//*[@data-item-marker='PrintButton']"
            ).shouldBe(Condition.visible, Duration.ofSeconds(20));

            executeJavaScript("arguments[0].click();", printBtn);

            SelenideElement item = $x(
                    "//*[contains(@data-item-marker,'" + name + "')" +
                            " or contains(text(),'" + name + "')]"
            ).shouldBe(Condition.visible, Duration.ofSeconds(20));

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

  /*  @Step("Ожидание завершения скачивания файла")
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

   */

    @Step("Ожидание завершения скачивания файла")
    public void waitForDownload() {
        log.info("Waiting for download to complete...");

        File dir = downloadDir();
        int before = countReadyFiles(dir);

        try {
            new org.openqa.selenium.support.ui.FluentWait<>(dir)
                    .withTimeout(Duration.ofSeconds(60))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(d -> {
                        File[] inProgress = d.listFiles(f ->
                                f.getName().endsWith(".crdownload")
                                        || f.getName().endsWith(".tmp"));
                        boolean downloading = inProgress != null && inProgress.length > 0;
                        return (!downloading && countReadyFiles(d) > before) ? Boolean.TRUE : null;
                    });
            log.info("Download completed");
        } catch (org.openqa.selenium.TimeoutException e) {
            log.warn("Download НЕ завершился за 60 секунд");
        }
    }

    private int countReadyFiles(File dir) {
        File[] files = dir.listFiles(
                f -> f.isFile() && !f.getName().endsWith(".crdownload"));
        return files == null ? 0 : files.length;
    }


    /**
     * Снимок готовых файлов в папке загрузок. Делается ПЕРЕД кликом по печатной
     * форме — существующий waitForDownload() снимает счётчик уже после клика
     * и потому не замечает быстро скачавшийся файл.
     */
    public Set<String> snapshotDownloads() {
        File dir = downloadDir();

        File[] files = dir.listFiles(this::isReady);

        Set<String> names = files == null
                ? new HashSet<>()
                : Arrays.stream(files).map(File::getName)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        log.debug("Снимок папки загрузок: {} файлов", names.size());
        return names;
    }

    /**
     * Ждёт появления нового готового файла относительно снимка. В отличие от
     * waitForDownload() падает с понятным сообщением, а не пишет warn:
     * несостоявшееся скачивание — это провал шага, а не предупреждение.
     *
     * @return файл, появившийся после клика
     */
    @Step("Ожидание скачивания файла: {description}")
    public File waitForNewDownload(Set<String> before, String description) {
        log.info("Ожидание скачивания: '{}'", description);

        try {
            File downloaded = new FluentWait<>(downloadDir())
                    .withTimeout(Duration.ofSeconds(90))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(dir -> {
                        File[] inProgress = dir.listFiles(
                                f -> f.getName().endsWith(".crdownload")
                                        || f.getName().endsWith(".tmp"));
                        if (inProgress != null && inProgress.length > 0) {
                            return null;    // докачка не завершена
                        }
                        File[] files = dir.listFiles(this::isReady);
                        if (files == null) {
                            return null;
                        }
                        return Arrays.stream(files)
                                .filter(f -> !before.contains(f.getName()))
                                .findFirst()
                                .orElse(null);
                    });

            log.info("Скачано: '{}' ({} байт)",
                    downloaded.getName(), downloaded.length());
            return downloaded;

        } catch (org.openqa.selenium.TimeoutException e) {
            AllureAttachments.attachScreenshot("Download failure");
            throw new AssertionError(
                    "Файл не скачался за 90 с: '" + description + "'", e);
        }
    }

    /** Папка загрузок Chrome — задаётся в DriverSetup. */
    private File downloadDir() {
        File dir = new File(System.getProperty("user.dir") + "\\downloads");
        dir.mkdirs();
        return dir;
    }

    /** Готовый файл: не докачка Chrome (.crdownload) и не его временный файл (.tmp). */
    private boolean isReady(File f) {
        String name = f.getName();
        return f.isFile()
                && !name.endsWith(".crdownload")
                && !name.endsWith(".tmp");
    }

    /** Снимок загрузок + «Печать» + выбор формы в меню PrintButton. Возвращает снимок ДО клика. */
    @Step("Выбор печатной формы: {formName}")
    public Set<String> openPrintForm(String formName) {
        Set<String> before = snapshotDownloads();
        click.clickByName("Печать");
        $x("//ul[contains(@data-item-marker,'PrintButton')]" +
                "//li[normalize-space()='" + formName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .click();
        return before;
    }

    /** Печать формы со скачиванием без должностного лица. */
    @Step("Печать формы со скачиванием: {formName}")
    public File printFormAndDownload(String formName) {
        Set<String> before = openPrintForm(formName);
        return waitForNewDownload(before, formName);
    }

    /**
     * V7.1 ARTIFACT: строгое ожидание скачивания + проверка непустоты в коллектор.
     * Файл, вернувшийся из waitForNewDownload, гарантированно существует (иначе throw);
     * добавочный инвариант — size>0 (форма не пустая). Пишем именованный чек, который
     * флашится швом вызывающей стадии (нового flush тут НЕ делаем — во избежание утечки).
     */
    @Step("Проверка скачанной печатной формы: {formName}")
    public File verifyDownloaded(Set<String> before, String formName) {
        File f = waitForNewDownload(before, formName);
        VerificationCollector.checkTrue(
                "Печатная форма скачана (size>0): " + formName,
                f.exists() && f.length() > 0,
                "file=" + f.getName() + " bytes=" + (f.exists() ? f.length() : -1));
        return f;
    }
}