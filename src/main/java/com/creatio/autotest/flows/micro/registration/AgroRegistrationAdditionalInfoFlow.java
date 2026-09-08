package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.testdata.models.BaseScoringData;
import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import com.creatio.autotest.testdata.models.registration.EducationCareerData;
import com.creatio.autotest.testdata.models.registration.fields.EducationCareerField;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Флоу «Дополнительная информация» стадии «Оформление» для продуктов
 * сегмента «Микро» (первый продукт — «Агро»).
 * <p>
 * Самостоятельный класс в ветке {@code flows.micro.registration},
 * изолированный от розничного {@code RegistrationAdditionalInfoFlow}.
 * На текущем этапе логика заполнения совпадает с розничной (подтверждено:
 * до стадии «Информирование клиента» процесс Агро идентичен рознице):
 * базовый скоринг + занятость. Для Агро-клиента актуальна ветка
 * {@code SELF_EMPLOYED} (фермер), остальные стратегии занятости заложены
 * по паттерну розницы для консистентности и будущего роста Микро.
 * <p>
 * Агро/Микро-специфичные поля ({@code AgroScoringData}: тип хозяйства,
 * земля, культура) в этом блоке пока НЕ заполняются — будут добавлены,
 * когда появятся на live DOM.
 */
public class AgroRegistrationAdditionalInfoFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationAdditionalInfoFlow.class);

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение базовых скоринговых данных (Агро/Микро)")
    public void fillBaseScoringData(BaseScoringData data) {
        log.info("Fill base scoring data (Agro/Micro)");

        lookup.select("Семейное положение", data.getMaritalStatus());

        setField("Количество иждивенцев (строка)",  data.getDependentsCount());
        setField("Количество детей",                data.getChildrensCount());
        setField("Количество членов семьи",         data.getFamilyCount());
        setField("Общий стаж",                      data.getTotalExperience());
        setField("Общий стаж, лет",                 data.getTotalExperienceYears());
        setField("Общий стаж, мес",                 data.getTotalExperienceMonths());
        setField("Стаж на последнем месте, лет",    data.getLastJobExperienceYears());
        setField("Стаж на последнем месте, мес",    data.getLastJobExperienceMonths());
    }

    private void setField(String label, String value) {
        if (value == null || value.isEmpty()) return;
        lookup.selectByLabel(label, value);
    }

    @Step("Выбор типа занятости (Самозанятый) — фермер Агро")
    public void selectEmploymentTypeSelfEmployed(ClientProfileData profile) {
        log.info("selectEmploymentTypeSelfEmployed: {}",
                profile.getEmploymentType().getUiName());

        EducationCareerData ec = profile.getEducationCareer();

        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.EMPLOYMENT_TYPE.marker,
                profile.getEmploymentType().getUiName());

        // SELF_EMPLOYED: причина отсутствия работы должна быть пустой
        lookup.clearByMarkerIfPresent(EducationCareerField.REASON_FOR_NOT_WORKING.marker);

        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.POSITION.marker, ec.getPosition());
        lookup.setTextByMarker(
                EducationCareerField.ORGANIZATION_NAME.marker, ec.getOrganizationName());
        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.ROLE.marker, ec.getRole());
        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.EDUCATION.marker, ec.getEducation());
        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.LEGAL_FORM.marker, ec.getLegalForm());
        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.BUSINESS_REPUTATION.marker, ec.getBusinessReputation());

        verifyCareerRoundTrip(profile);
    }

    /**
     * ROUND-TRIP карьеры SELF_EMPLOYED (V3-Агро). Маркеры (EducationCareerField.*)
     * и ридер (getValueByMarkerSafe — обычные inputs) подтверждены по рознице.
     * Набор из 7 заполняемых полей: тип занятости, должность, организация, роль,
     * образование, юр.форма, деловая репутация. flush — на уровне маршрута.
     */
    @Step("Round-trip карьеры (Самозанятый) — Агро/Микро")
    private void verifyCareerRoundTrip(ClientProfileData profile) {
        EducationCareerData ec = profile.getEducationCareer();
        VerificationCollector.check("Карьера — тип занятости",
                profile.getEmploymentType().getUiName(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.EMPLOYMENT_TYPE.marker));
        VerificationCollector.check("Карьера — должность",
                ec.getPosition(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.POSITION.marker));
        VerificationCollector.check("Карьера — организация",
                ec.getOrganizationName(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.ORGANIZATION_NAME.marker));
        VerificationCollector.check("Карьера — роль",
                ec.getRole(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.ROLE.marker));
        VerificationCollector.check("Карьера — образование",
                ec.getEducation(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.EDUCATION.marker));
        VerificationCollector.check("Карьера — юр.форма",
                ec.getLegalForm(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.LEGAL_FORM.marker));
        VerificationCollector.check("Карьера — деловая репутация",
                ec.getBusinessReputation(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.BUSINESS_REPUTATION.marker));
    }
}