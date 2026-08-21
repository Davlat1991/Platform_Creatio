package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.testdata.models.BaseScoringData;
import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import com.creatio.autotest.testdata.models.registration.EducationCareerData;
import com.creatio.autotest.testdata.models.registration.fields.EducationCareerField;
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
    }
}