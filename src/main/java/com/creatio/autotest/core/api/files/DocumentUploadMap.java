package com.creatio.autotest.core.api.files;

import com.codeborne.selenide.Selenide;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Маппинг "тип документа → UUID записи" для API-загрузки.
 *
 * Надёжный data-driven подход: UUID берётся из контейнера записи
 * (data-item-marker="document-item-<UUID>"), тип определяется по тексту
 * названия документа внутри контейнера. Не зависит от порядка input'ов,
 * дублей DOM и количества участников. Скоуп — активная вкладка документов.
 */
public class DocumentUploadMap {

    private final Map<String, String> map = new LinkedHashMap<>();

    /**
     * Собирает маппинг тип→UUID внутри контейнера участника.
     * Тип определяется по тексту названия документа, parentColumnValue —
     * из ссылки doc-link (DocumentPageV2/edit/<UUID>), это UUID записи в БД.
     * @param scopeId id контейнера вкладки участника (напр. "DocBorrowerTab")
     */
    @SuppressWarnings("unchecked")
    public DocumentUploadMap collect(String scopeId) {
        Map<String, String> collected = (Map<String, String>) Selenide.executeJavaScript(
                "var scope = document.querySelector('#' + arguments[0]);" +
                        "if (!scope) return {};" +
                        "var rows = scope.querySelectorAll(\"[data-item-marker^='document-item-']\");" +
                        "var seen = {};" +
                        "var res = {};" +
                        "for (var i = 0; i < rows.length; i++) {" +
                        "  var marker = rows[i].getAttribute('data-item-marker');" +
                        "  if (seen[marker]) continue;" +
                        "  var text = (rows[i].innerText || '').replace(/\\n/g,' ');" +
                        "  var type = null;" +
                        "  if (text.indexOf('Справка о доходах') >= 0) type='Income';" +
                        "  else if (text.indexOf('ИНН') >= 0) type='INN';" +
                        "  else if (text.indexOf('Шиносномаи') >= 0) type='Passport';" +
                        "  else if (text.indexOf('FATCA') >= 0) type='FATCA';" +
                        "  if (!type) continue;" +
                        "  var link = rows[i].querySelector(\"a[href*='DocumentPageV2/edit/']\");" +
                        "  if (!link) continue;" +
                        "  var m = link.getAttribute('href').match(/DocumentPageV2\\/edit\\/([a-f0-9\\-]{36})/);" +
                        "  if (!m) continue;" +
                        "  seen[marker] = 1;" +
                        "  res[type] = m[1];" +
                        "}" +
                        "return res;",
                scopeId);

        if (collected != null) {
            map.putAll(collected);
        }
        return this;
    }

    public String get(String docType) {
        String value = map.get(docType);
        if (value == null) {
            throw new RuntimeException("parentColumnValue не найден для: " + docType);
        }
        return value;
    }

    public boolean has(String docType) {
        return map.containsKey(docType);
    }

    public int size() {
        return map.size();
    }
}