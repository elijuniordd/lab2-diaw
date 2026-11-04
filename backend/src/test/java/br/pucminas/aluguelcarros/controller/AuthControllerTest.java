package br.pucminas.aluguelcarros.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private String body(String login, String senha, String role) throws Exception {
        var node = json.createObjectNode().put("login", login).put("senha", senha);
        if (role != null) {
            node.put("role", role);
        }
        return json.writeValueAsString(node);
    }

    private String registrar(String login, String senha, String role) throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body(login, senha, role)))
                .andExpect(status().isCreated());
        return login;
    }

    private String token(String login, String senha) throws Exception {
        String resp = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body(login, senha, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resp).get("token").asText();
    }

    private String novoLogin() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void fluxoRegistrarLogarEAcessarRotaProtegida() throws Exception {
        String login = registrar(novoLogin(), "senha123", null);

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token(login, "senha123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void registroNaoExpoeSenha() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body(novoLogin(), "senha123", "AGENTE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("AGENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void loginDuplicadoRetorna409() throws Exception {
        String login = registrar(novoLogin(), "senha123", null);
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body(login, "outra123", null)))
                .andExpect(status().isConflict());
    }

    @Test
    void senhaErradaRetorna401() throws Exception {
        String login = registrar(novoLogin(), "senha123", null);
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body(login, "errada", null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dadosInvalidosRetornam400() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body("ab", "123", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.login").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void rotaProtegidaSemTokenOuComTokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/auth/me").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }
}
