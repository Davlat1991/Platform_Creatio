package com.creatio.autotest.core.api.files;

import com.creatio.autotest.core.api.session.BrowserSessionHelper;
import com.creatio.autotest.core.config.Config;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

/**
 * Загрузка файлов в Creatio через API (TsiFileApiService).
 * Авторизация — через cookie текущей сессии браузера (BrowserSessionHelper).
 * Base URL берётся из Config (multi-env через -Denv), не хардкод.
 */
public class FileUploadApiHelper {

    private static final Logger log =
            LoggerFactory.getLogger(FileUploadApiHelper.class);

    private static final String FILES_DIR = "src/test/resources/files/";

    @Step("API: загрузить '{fileName}' → parentColumnValue={parentColumnValue}")
    public static void uploadFile(String fileName, String parentColumnValue) {
        log.info("API upload: file='{}' parent='{}'", fileName, parentColumnValue);

        File file     = new File(FILES_DIR + fileName);
        long fileSize = file.length();
        String cookie  = BrowserSessionHelper.getCookieString();
        String bpmcsrf = BrowserSessionHelper.getBpmcsrf();
        String mime    = resolveMime(fileName);
        String baseUrl = Config.getInstance().getBaseUrl();

        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .header("Bpmcsrf",             bpmcsrf)
                .header("Cookie",              cookie)
                .header("X-Requested-With",    "XMLHttpRequest")
                .header("Content-Type",        mime)
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .header("Content-Range",       "bytes 0-" + (fileSize - 1) + "/" + fileSize)
                .queryParam("fileapi" + System.currentTimeMillis(), "")
                .queryParam("fileId",            UUID.randomUUID().toString())
                .queryParam("totalFileLength",   String.valueOf(fileSize))
                .queryParam("mimeType",          mime)
                .queryParam("columnName",        "Data")
                .queryParam("fileName",          fileName)
                .queryParam("parentColumnName",  "Document")
                .queryParam("parentColumnValue", parentColumnValue)
                .queryParam("entitySchemaName",  "DocumentFile")
                .body(file)
                .when()
                .post("/0/rest/TsiFileApiService/Upload");

        int status = response.statusCode();
        if (status != 200) {
            throw new AssertionError(
                    "Upload failed [" + status + "] file=" + fileName +
                            " parent=" + parentColumnValue +
                            " body=" + response.body().asString());
        }
        log.info("API upload OK: '{}'", fileName);
    }

    private static String resolveMime(String fileName) {
        if (fileName.endsWith(".pdf"))  return "application/pdf";
        if (fileName.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (fileName.endsWith(".jpg"))  return "image/jpeg";
        if (fileName.endsWith(".png"))  return "image/png";
        return "application/octet-stream";
    }
}