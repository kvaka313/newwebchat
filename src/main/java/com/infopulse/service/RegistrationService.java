package com.infopulse.service;

import com.infopulse.converter.UserConverter;
import com.infopulse.dto.UserDTO;
import com.infopulse.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RegistrationService {

    private WebChatUserRepository webChatUserRepository;

    private UserConverter userConverter;

    public RegistrationService(WebChatUserRepository webChatUserRepository, UserConverter userConverter){
        this.webChatUserRepository = webChatUserRepository;
        this.userConverter = userConverter;
    }

    @Transactional
    public UserDTO save(UserDTO userDTO){
        User currentUser = userConverter.convertToEntity(userDTO);

        Optional<User> oldUser = webChatUserRepository.findByLogin(currentUser.getLogin());
        oldUser.ifPresent(entity-> throw new UserAlreadyException());
        //todo message
        return userConverter.convertToDto(webChatUserRepository.save(currentUser));
    }
}
