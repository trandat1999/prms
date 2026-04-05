package com.tranhuudat.prms.util.anotation;

import com.tranhuudat.prms.util.validator.RequiredConditionGOETValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author DatNuclear 20/03/2024
 * @project store
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {RequiredConditionGOETValidator.class})
public @interface RequiredConditionGOET { //greater than or equal to
    String fieldName();
    String dependField();
    long value();
    String message() default "{app.validation.NotNull}";
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
