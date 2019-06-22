package com.infopulse.converter;

import com.infopulse.dto.UserDTO;
import com.infopulse.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public UserDTO convertToDto(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setName(user.getName());
        userDTO.setLogin(user.getLogin());
        return userDTO;
    }

    public User convertToEntity(UserDTO userDTO){
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setName(userDTO.getName());
        return user;
    }
}
