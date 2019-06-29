package com.infopulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infopulse.dto.ReceiveMessage;
import com.infopulse.dto.SendMessage;
import com.infopulse.entity.User;
import com.infopulse.service.data.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketController extends TextWebSocketHandler {

    private UserService userService;

    private WebSocketService webSocketService;

    private Map<String, WebSocketSession> activeUsers = new ConcurrentHashMap<>();

    private Validator validator;

    private ObjectMapper mapper = new ObjectMapper();

    public WebSocketController(UserService userService,
                               WebSocketService webSocketService,
                               Validator validator){

        this.userService = userService;
        this.webSocketService = webSocketService;
        this.validator = validator;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session){
     try {
         String login = (String) session.getAttributes().get("login");
         Optional<User> user = userService.findUserByLogin(login);
         if (user.isPresent()) {
             if (user.get().getBan() != null) {
                 sendErrorMessage(session, "You are banned.");
                 return;
             }

             activeUsers.put(user.get().getLogin(), session);
             sendActiveUsersList();
         } else {
             session.close();
         }
     } catch (IOException e){
         log.error(e.getMessage());
     }

    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message){
      try {
          String login = (String) session.getAttributes().get("login");
          Optional<User> user = userService
                  .findUserByLogin(login);

          if (user.isPresent() && user.get().getBan() != null) {
              session.close();
          }

          String jsonString = message.getPayload();
          ReceiveMessage receiveMessage = mapper.readValue(jsonString, ReceiveMessage.class);

          Set<ConstraintViolation<ReceiveMessage>> errors = validator.validate(receiveMessage);
          if(!errors.isEmpty()){
              sendErrorMessage(session, errors.iterator().next().getMessage());
              return;
          }

          switch(receiveMessage.getType()){
              case "PRIVATE":{
                  if(receiveMessage.getReceiver() == null){
                      sendErrorMessage(session, "receiver is required.");
                      return;
                  }
                  if(!userService.findUserByLogin(receiveMessage.getReceiver()).isPresent()){
                      sendErrorMessage(session, "receiver was not found.");
                      return;
                  }

                  if(receiveMessage.getMessage() == null){
                      sendErrorMessage(session, "message is required");
                      return;
                  }
                  WebSocketSession receiverSession = activeUsers.get(receiveMessage.getReceiver());
                  if(receiverSession == null){
                      webSocketService.saveMessage(receiveMessage);
                      return;
                  }

                  sendPrivateMessage(receiverSession,
                          login,
                          receiveMessage.getMessage());
                  break;
              }
              case "BROADCAST":{
                  if(receiveMessage.getMessage() == null){
                      sendErrorMessage(session, "message is required");
                      return;
                  }
                  sendBroadcastMessage(login, receiveMessage.getMessage());
                  break;
              }
              case "LOGOUT":{
                  activeUsers.remove(login);
                  sendActiveUsersList();
                  session.close();
                  break;
              }
          }

      } catch (IOException e){
          log.error(e.getMessage());
      }
    }

    private void sendErrorMessage(WebSocketSession session, String message){
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setMessage(message);
            sendMessage.setSender("system");
            sendMessage.setType("PRIVATE");
            TextMessage textMessage = new TextMessage(mapper.writeValueAsString(sendMessage));
            session.sendMessage(textMessage);
        } catch(IOException e){
            log.error(e.getMessage());
        }
    }

    private void sendActiveUsersList(){
        try {
            Set<String> activeUsersLogin = activeUsers.keySet();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setType("LIST");
            sendMessage.setSender("system");
            sendMessage.setMessage(null);
            sendMessage.setLogins(activeUsersLogin);
            TextMessage textMessage = new TextMessage(mapper.writeValueAsString(sendMessage));
            sendAll(textMessage);
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private void sendAll(TextMessage textMessage){

        activeUsers.entrySet().stream()
                .map(entry->entry.getValue())
                .forEach(session -> {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                });
    }
}
