package com.infopulse.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.infopulse.dto.ReceiveMessage;
import com.infopulse.entity.User;
import com.infopulse.service.UserControllerService;
import com.infopulse.service.data.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.imageio.IIOException;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class WebSocketController extends TextWebSocketHandler {

    private UserService userService;

    private ObjectMapper mapper = new ObjectMapper();

    public WebSocketController(UserService userService){
        this.userService = userService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message){
      try {
          String login = (String) session.getAttributes().get("login");
          Optional<User> user = userService.findUserByLogin(login);
          User u = user.orElse(null);
          if (u.getBan() != null) {
              session.close();
          }

          ReceiveMessage receiveMessage = mapper.rew

      } catch (IOException e){
          log.error(e.getMessage());
      }
    }
}
