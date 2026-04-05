package com.tranhuudat.prms.util.validator;

import com.tranhuudat.prms.util.anotation.ExistValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.beanutils.BeanUtils;

/**
 * @author DatNuclear 22/02/2024
 * @project store
 */
public class ExistValueValidator implements ConstraintValidator<ExistValue, Object> {
    private String[] fieldName;
    private int numberExist = 1;
    @Override
    public void initialize(ExistValue constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        fieldName = constraintAnnotation.fieldName();
        numberExist = constraintAnnotation.numberExist();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if(object==null || fieldName==null || fieldName.length==0) return true;
        try {
            int rs = 0;
            for(String fieldName : fieldName){
                Object fieldValue       = BeanUtils.getProperty(object, fieldName);
                if(fieldValue !=null){
                    rs++;
                }
            }
            if(rs>=numberExist){
                return true;
            }
            for(String fieldName : fieldName){
                constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(fieldName).addConstraintViolation();
                constraintValidatorContext.disableDefaultConstraintViolation();
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return false;
    }
}
