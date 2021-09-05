package com.scnu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

public class StatusConstraintValidator implements ConstraintValidator<StatusValid,Integer> {

    private int[] value;

    @Override
    public void initialize(StatusValid constraintAnnotation) {
        this.value = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Integer val, ConstraintValidatorContext context) {
        for (int i = 0; i < this.value.length; i++) {
            if(val == value[i]){
                return true;
            }
        }
        return false;
    }
}
