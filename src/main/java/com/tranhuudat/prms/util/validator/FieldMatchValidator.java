package com.tranhuudat.prms.util.validator;

import com.tranhuudat.prms.util.anotation.FieldMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author DatNuclear 19/01/2024
 * @project store-movie
 */
public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String fieldName;
    private String dependFieldName;
    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        fieldName = constraintAnnotation.fieldName();
        dependFieldName = constraintAnnotation.dependFieldName();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if(object==null) return true;
        try {
            Object fieldValue       = BeanUtils.getProperty(object, fieldName);
            Object dependFieldValue = BeanUtils.getProperty(object, dependFieldName);
            if(dependFieldValue!=null && fieldValue!=null && ObjectUtils.nullSafeEquals(fieldValue, dependFieldValue)){
                return true;
            }
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(fieldName).addConstraintViolation();
            constraintValidatorContext.disableDefaultConstraintViolation();
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return false;
    }
}
