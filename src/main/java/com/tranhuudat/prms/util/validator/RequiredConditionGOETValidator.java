package com.tranhuudat.prms.util.validator;

import com.tranhuudat.prms.util.anotation.RequiredConditionGOET;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.beanutils.BeanUtils;

/**
 * @author DatNuclear 20/03/2024
 * @project store
 */
public class RequiredConditionGOETValidator implements ConstraintValidator<RequiredConditionGOET, Object> {
    private String fieldName;
    private String dependField;
    private long value = 1;
    @Override
    public void initialize(RequiredConditionGOET constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        fieldName = constraintAnnotation.fieldName();
        value = constraintAnnotation.value();
        dependField = constraintAnnotation.dependField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if(object==null || dependField==null) return true;
        try {
            Object fieldValue = BeanUtils.getProperty(object, fieldName);
            Object dependFieldValue = BeanUtils.getProperty(object, dependField);
            if(dependFieldValue==null) return true;
            if(fieldValue==null && value <= Long.parseLong(dependFieldValue.toString())){
                constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(fieldName).addConstraintViolation();
                constraintValidatorContext.disableDefaultConstraintViolation();
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
