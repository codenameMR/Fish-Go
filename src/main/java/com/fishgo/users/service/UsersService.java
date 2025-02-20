package com.fishgo.users.service;

import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.UsersDto;
import com.fishgo.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Users registerUser(UsersDto usersDto){
        if(usersRepository.existsByUserId(usersDto.getUserId())){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(usersDto.getPassword());
        Users user = Users.builder()
                .userId(usersDto.getUserId())
                .name(usersDto.getName())
                .password(encodedPassword)
                .role("USER")
                .build();

        Users saveUser = usersRepository.save(user);

        createUserDir(saveUser.getUserId());

        return saveUser;
    }

    public Users loginUser(UsersDto usersDto) {
        Users user = usersRepository.findByUserId(usersDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if(!passwordEncoder.matches(usersDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    private void createUserDir(String userName) {
        String dir = System.getProperty("user.dir");
        String path = dir + "/uploads/users/" + userName;
        new File(path + "/profile").mkdirs();
        new File(path + "/posts").mkdirs();
    }
}
