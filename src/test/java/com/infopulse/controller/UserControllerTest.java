package com.infopulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infopulse.Application;
import com.infopulse.entity.User;
import com.infopulse.repository.BanRepository;
import com.infopulse.repository.MessageRepository;
import com.infopulse.repository.WebChatUserRepository;
import com.infopulse.utils.TokenUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml"})
@DirtiesContext
public class UserControllerTest {

    @Resource
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private WebChatUserRepository webChatUserRepository;

    @Autowired
    private BanRepository banRepository;

    @Autowired
    private MessageRepository messageRepository;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    @Transactional
    public void createDatabase(){
        banRepository.deleteAll();
        messageRepository.deleteAll();
        webChatUserRepository.deleteAll();

        User user1 = new User();
        user1.setLogin("Kolya1");
        user1.setName("Kolya1");
        webChatUserRepository.save(user1);

        User user2 = new User();
        user2.setLogin("Vasya1");
        user2.setName("Vasya1");
        webChatUserRepository.save(user2);
    }

    @Test
    public void testFindUsers() throws Exception {
        String url = "/users";

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .defaultRequest(get("/"))
                .build();

        ResultActions resultActions = mvc.perform(get(url)
                .header("Authorization", "bearer "+
                        TokenUtils.createAccessJwtToken()));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.[0].name", is("Kolya1")))
                .andExpect(jsonPath("$.[0].login", is("Kolya1")))
                .andExpect(jsonPath("$.[1].name", is("Vasya1")))
                .andExpect(jsonPath("$.[1].login", is("Vasya1")))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @After
    @Transactional
    public void clear(){
        banRepository.deleteAll();
        messageRepository.deleteAll();
        webChatUserRepository.deleteAll();

    }
}
