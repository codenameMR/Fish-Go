package com.fishgo.users.validation;

import com.fishgo.users.dto.UsersDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UsersDto> {

    @Override
    public boolean isValid(UsersDto usersDto, ConstraintValidatorContext context) {
        if (usersDto.getPassword() == null || usersDto.getConfirmPassword() == null) {
            return false;
        }
        return usersDto.getPassword().equals(usersDto.getConfirmPassword());
    }
}
