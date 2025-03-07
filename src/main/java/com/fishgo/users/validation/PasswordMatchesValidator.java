package com.fishgo.users.validation;

import com.fishgo.users.dto.SignupRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, SignupRequestDto> {

    @Override
    public boolean isValid(SignupRequestDto signupDto, ConstraintValidatorContext context) {
        if (signupDto.getPassword() == null || signupDto.getConfirmPassword() == null) {
            return false;
        }
        return signupDto.getPassword().equals(signupDto.getConfirmPassword());
    }
}
