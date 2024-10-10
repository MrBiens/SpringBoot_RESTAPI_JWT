package com.vn.sbit.idenfity_service.service;

import com.vn.sbit.idenfity_service.dto.request.UserCreationRequest;
import com.vn.sbit.idenfity_service.dto.request.UserUpdateRequest;
import com.vn.sbit.idenfity_service.dto.response.UserResponse;
import com.vn.sbit.idenfity_service.entity.User;
import com.vn.sbit.idenfity_service.exception.AppException;
import com.vn.sbit.idenfity_service.exception.ErrorCode;
import com.vn.sbit.idenfity_service.mapper.UserMapper;
import com.vn.sbit.idenfity_service.mapper.UserMapperImpl;
import com.vn.sbit.idenfity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//@RequiredArgsConstructor  - //sẽ tự động Injection dependency mà không ần @Autowired
//@FielDefaults(level=AccessLevel.PRIVATE, makeFilnal = true) -- những attribute nào không khai báo sẽ mặc định là private final NameAttribute;
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;


    public User createUser(UserCreationRequest request){
        if(userRepository.existsByUserName(request.getUserName())
        ) throw new AppException(ErrorCode.USER_EXISTED);

//        Builder - tham số nhất định ( thay thế constructor)
//        UserCreationRequest userCreationRequest = UserCreationRequest.builder().userName("").firstName("").lastName("").build();
        User user = userMapper.toUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassWord(passwordEncoder.encode(request.getPassWord()));

        return userRepository.save(user);
    }
    public List<User> getUsers(){
      return  userRepository.findAll();
    }

    public UserResponse getUser(String id){
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(()->new  RuntimeException("User not found")
        ));
    }


    public UserResponse updateUser(String userId,UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(()->new  RuntimeException("User not found"));
        userMapper.updateUser(user,request);
        return  userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId){
        userRepository.deleteById(userId);

    }
}
