package com.vn.sbit.idenfity_service.controller;

import com.vn.sbit.idenfity_service.dto.ApiResponse;
import com.vn.sbit.idenfity_service.dto.request.UserCreationRequest;
import com.vn.sbit.idenfity_service.dto.request.UserUpdateRequest;
import com.vn.sbit.idenfity_service.dto.response.UserResponse;
import com.vn.sbit.idenfity_service.entity.User;
import com.vn.sbit.idenfity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController //RestAPI - GET-POST-PUT-DELETE
@RequestMapping("/users")
@RequiredArgsConstructor//sẽ tự động Injection dependency mà không ần @Autowired
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true) //những attribute nào không khai báo sẽ mặc định là private, final NameAttribute;
public class UserController {
//@RequiredArgsConstructor//sẽ tự động Injection dependency mà không ần @Autowired 
   UserService userService;

    @PostMapping
    //validation
    public ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest request){
        ApiResponse<User> userApiResponse = new ApiResponse<>();

        userApiResponse.setCode(1100);
        userApiResponse.setMessage("Success");
        userApiResponse.setResult(userService.createUser(request));
        return userApiResponse;
    }
    @GetMapping
    public List<User> getUsers(){
        return userService.getUsers();
    }
    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable("userId") String userId){
        return userService.getUser(userId);
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(@PathVariable("userId") String userId,@RequestBody UserUpdateRequest request){
        return userService.updateUser(userId,request);
    }
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable("userId") String userId){
         userService.deleteUser(userId);
         return "User has been deleted";

    }

}
