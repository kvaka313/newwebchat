package com.infopulse.service.data;

import com.infopulse.entity.Ban;
import com.infopulse.entity.User;
import com.infopulse.exception.UserAlreadyBannedException;
import com.infopulse.exception.UserNotFoundException;
import com.infopulse.repository.BanRepository;
import com.infopulse.repository.WebChatUserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private WebChatUserRepository webChatUserRepository;

    @Mock
    private BanRepository banRepository;

    @Test(expected = UserNotFoundException.class)
    public void testBanUser_UserNotFound(){
        String login = "Vasya";
        UserService userService = new UserService(webChatUserRepository, banRepository);
        when(webChatUserRepository.findByLogin(login)).thenReturn(Optional.empty());
        userService.banUser(login);
    }

    @Test(expected = UserAlreadyBannedException.class)
    public void testBanUser_UserAlreadyBanned(){
        String login = "Vasya";
        UserService userService = new UserService(webChatUserRepository, banRepository);

        User user = new User();
        user.setName("Vasya");
        user.setLogin(login);
        user.setBan(new Ban());
        when(webChatUserRepository.findByLogin(login)).thenReturn(Optional.of(user));
        userService.banUser(login);
    }

    @Test
    public void testBanUser(){
        String login = "Vasya";
        UserService userService = new UserService(webChatUserRepository, banRepository);

        User user = new User();
        user.setName("Vasya");
        user.setLogin(login);
        when(webChatUserRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(banRepository.save(any(Ban.class))).thenReturn(any(Ban.class));
        assertEquals(user, userService.banUser(login));
    }
}
