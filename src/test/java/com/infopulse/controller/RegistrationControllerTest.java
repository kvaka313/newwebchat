package com.infopulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infopulse.Application;
import com.infopulse.dto.UserDTO;
import com.infopulse.repository.BanRepository;
import com.infopulse.repository.MessageRepository;
import com.infopulse.repository.WebChatUserRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml" })
@Sql("/test-data.sql")
@DirtiesContext
public class RegistrationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private WebChatUserRepository webChatUserRepository;

    @Autowired
    private BanRepository banRepository;

    @Autowired
    private MessageRepository messageRepository;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testRegistration_UserCreated() throws Exception {
       UserDTO userDTO = new UserDTO();
       userDTO.setName("Ivan");
       userDTO.setLogin("Ivan");
       userDTO.setPassword("password");

       String url = "/registration";

       String json = mapper.writeValueAsString(userDTO);

       MockHttpServletRequestBuilder msb = post(url)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON);

       MockMvc mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(get("/"))
                .build();

       ResultActions resultActions = mvc.perform(msb);

       resultActions.andExpect(status().isAccepted());
    }

    @Test
    public void testRegistration_LoginAlreadyExist() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Ivan");
        userDTO.setLogin("Kolya");
        userDTO.setPassword("password");

        String url = "/registration";

        String json = mapper.writeValueAsString(userDTO);

        MockHttpServletRequestBuilder msb = post(url)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON);

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(get("/"))
                .build();

        ResultActions resultActions = mvc.perform(msb);

        resultActions.andExpect(status().isBadRequest())
       .andExpect(jsonPath("$.message", is("User already exist.")));
    }

    @After
    @Transactional
    public void clear(){
        banRepository.deleteAll();
        messageRepository.deleteAll();
        webChatUserRepository.deleteAll();

    }

}
