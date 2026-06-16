package com.example.mas_implementation.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterFormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private RegisterForm validForm() {
        RegisterForm f = new RegisterForm();
        f.setLogin("coolplayer");
        f.setPassword("Secret123");
        f.setName("Jan Kowalski");
        f.setEmail("jan@test.com");
        f.setPhoneNumber("+48600123456");
        f.setBirthdate(LocalDate.of(1995, 1, 1));
        return f;
    }

    @Test
    void validForm_hasNoViolations() {
        assertThat(validator.validate(validForm())).isEmpty();
    }

    @Test
    void blankLogin_causesViolation() {
        RegisterForm f = validForm();
        f.setLogin("");
        Set<ConstraintViolation<RegisterForm>> v = validator.validate(f);
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("login"));
    }

    @Test
    void loginTooShort_causesViolation() {
        RegisterForm f = validForm();
        f.setLogin("x");   // 1 char, min is 2
        Set<ConstraintViolation<RegisterForm>> v = validator.validate(f);
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("login"));
    }

    @Test
    void invalidEmail_causesViolation() {
        RegisterForm f = validForm();
        f.setEmail("not-an-email");
        Set<ConstraintViolation<RegisterForm>> v = validator.validate(f);
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("email"));
    }

    @Test
    void futureBirthdate_causesViolation() {
        RegisterForm f = validForm();
        f.setBirthdate(LocalDate.now().plusYears(1));
        Set<ConstraintViolation<RegisterForm>> v = validator.validate(f);
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("birthdate"));
    }

    @Test
    void shortPassword_causesViolation() {
        RegisterForm f = validForm();
        f.setPassword("Ab1");   // less than 8 chars
        Set<ConstraintViolation<RegisterForm>> v = validator.validate(f);
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("password"));
    }

    @Test
    void passwordWithoutUppercaseOrDigit_causesViolation() {
        RegisterForm f = validForm();
        f.setPassword("alllowercase");   // 8+ chars but no uppercase and no digit
        Set<ConstraintViolation<RegisterForm>> v = validator.validate(f);
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("password"));
    }

    @Test
    void blankName_causesViolation() {
        RegisterForm f = validForm();
        f.setName("   ");
        Set<ConstraintViolation<RegisterForm>> v = validator.validate(f);
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("name"));
    }
}
