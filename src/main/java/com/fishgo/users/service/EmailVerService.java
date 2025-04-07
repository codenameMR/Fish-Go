package com.fishgo.users.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishgo.users.dto.SignupRequestDto;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class EmailVerService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public EmailVerService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateVerificationCode(String email) {
        String verificationCode = generateRandomCode(); // 랜덤 코드 생성 로직
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(email, verificationCode, 30, TimeUnit.MINUTES);
        return verificationCode;
    }

    private String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 1000000)); // 6자리 숫자
    }

    public void saveUserInfo(String email, SignupRequestDto usersDto) throws JsonProcessingException {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String userInfoJson = objectMapper.writeValueAsString(usersDto);
        ops.set(email + ":info", userInfoJson, 30, TimeUnit.MINUTES);
    }

    public SignupRequestDto getUserInfo(String email) throws JsonProcessingException {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String userInfoJson = ops.get(email + ":info");
        return objectMapper.readValue(userInfoJson, SignupRequestDto.class);
    }

    public void deleteUserInfo(String email) {
        redisTemplate.delete(email + ":info");
    }

    public void deleteVerificationCode(String email) {
        redisTemplate.delete(email);
    }

    public boolean verifyCode(String email, String code) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String storedCode = ops.get(email);

        if (storedCode == null) {
            return false; // 코드 없거나 만료
        }

        return code.equals(storedCode);
    }

    public String regenerateVerifyCode(String email) {
        // 기존 사용자 정보가 있는지 확인
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String userInfoJson = ops.get(email + ":info");

        if (userInfoJson == null) {
            throw new IllegalArgumentException("등록되지 않은 이메일입니다.");
        }

        // 새 인증 코드 생성 및 저장
        String newVerificationCode = generateRandomCode();
        ops.set(email, newVerificationCode, 30, TimeUnit.MINUTES);

        return newVerificationCode;
    }
}
