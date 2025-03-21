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
        ops.set(email, verificationCode, 10, TimeUnit.MINUTES); // 10분 유효
        return verificationCode;
    }

    private String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 1000000)); // 6자리 숫자
    }

    public void saveUserInfo(String email, SignupRequestDto usersDto) throws JsonProcessingException {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String userInfoJson = objectMapper.writeValueAsString(usersDto);
        ops.set(email + ":info", userInfoJson, 10, TimeUnit.MINUTES); // 10분 유효
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
            return false; // 코드가 만료되었거나 존재하지 않음
        }

        return code.equals(storedCode);
    }
}
