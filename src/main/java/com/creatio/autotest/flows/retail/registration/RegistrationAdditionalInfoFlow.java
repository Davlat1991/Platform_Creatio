package com.creatio.autotest.flows.retail.registration;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.testdata.models.BaseScoringData;
import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import com.creatio.autotest.testdata.models.registration.EducationCareerData;
import com.creatio.autotest.testdata.models.registration.fields.EducationCareerField;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.creatio.autotest.utils.FieldUtils;

public class RegistrationAdditionalInfoFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationAdditionalInfoFlow.class);

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение базовых скоринговых данных")
    public void fillBaseScoringData(BaseScoringData data) {
        log.info("Fill base scoring data");

        lookup.select("Семейное положение", data.getMaritalStatus());

        setField("Количество иждивенцев (строка)", data.getDependentsCount());
        setField("Количество детей",               data.getChildrensCount());
        setField("Количество членов семьи",         data.getFamilyCount());
        setField("Общий стаж",                     data.getTotalExperience());
        setField("Общий стаж, лет",                data.getTotalExperienceYears());
        setField("Общий стаж, мес",                data.getTotalExperienceMonths());
        setField("Стаж на последнем месте, лет",   data.getLastJobExperienceYears());
        setField("Стаж на последнем месте, мес",   data.getLastJobExperienceMonths());
    }

    private void setField(String label, String value) {
        if (value == null || value.isEmpty()) return;
        lookup.selectByLabel(label, value);
    }

    @Step("Выбор типа занятости (Иной источник дохода)")
    public void selectEmploymentTypeOtherIncome(ClientProfileData profile) {
        log.info("selectEmploymentTypeOtherIncome: {}",
                profile.getEmploymentType().getUiName());

        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.EMPLOYMENT_TYPE.marker,
                profile.getEmploymentType().getUiName());

        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.REASON_FOR_NOT_WORKING.marker,
                profile.getEducationCareer().getReasonForNotWorking());

        // ROUND-TRIP (V3): тип занятости + причина отсутствия работы. Data-driven.
        VerificationCollector.check("Карьера — тип занятости",
                profile.getEmploymentType().getUiName(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.EMPLOYMENT_TYPE.marker));
        VerificationCollector.check("Карьера — причина отсутствия работы",
                profile.getEducationCareer().getReasonForNotWorking(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.REASON_FOR_NOT_WORKING.marker));
    }

    @Step("Выбор типа занятости (Работает в организации)")
    public void selectEmploymentTypeEmployee(ClientProfileData profile) {
        log.info("selectEmploymentTypeEmployee: {}",
                profile.getEmploymentType().getUiName());

        EducationCareerData ec = profile.getEducationCareer();

        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.EMPLOYMENT_TYPE.marker,
                profile.getEmploymentType().getUiName());

        // EMPLOYEE/SELF_EMPLOYED: причина отсутствия работы должна быть пустой
        lookup.clearByMarkerIfPresent(EducationCareerField.REASON_FOR_NOT_WORKING.marker);

        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.POSITION.marker, ec.getPosition());
        lookup.setTextByMarker(
                EducationCareerField.ORGANIZATION_NAME.marker, ec.getOrganizationName());
        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.ROLE.marker, ec.getRole());
        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.SOCIAL_STATUS.marker, ec.getSocialStatus());
        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.EDUCATION.marker, ec.getEducation());
        lookup.setTextByMarker(
                EducationCareerField.POSITION_FULL_NAME.marker, ec.getPositionFullName());
        lookup.selectByMarkerWithSoftValidation(
                EducationCareerField.BUSINESS_REPUTATION.marker, ec.getBusinessReputation());

        // ROUND-TRIP (V3): карьера EMPLOYEE. Все поля читаются value по маркеру (live-подтверждено).
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
        VerificationCollector.check("Карьера — соц.статус",
                ec.getSocialStatus(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.SOCIAL_STATUS.marker));
        VerificationCollector.check("Карьера — образование",
                ec.getEducation(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.EDUCATION.marker));
        VerificationCollector.check("Карьера — должность (полн.)",
                ec.getPositionFullName(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.POSITION_FULL_NAME.marker));
        VerificationCollector.check("Карьера — деловая репутация",
                ec.getBusinessReputation(),
                FieldUtils.getValueByMarkerSafe(EducationCareerField.BUSINESS_REPUTATION.marker));
    }



    @Step("Выбор типа занятости (Самозанятый)")
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

        // ROUND-TRIP (V3): карьера SELF_EMPLOYED. Тот же маркер-паттерн (LEGAL_FORM вместо соц.статуса).
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