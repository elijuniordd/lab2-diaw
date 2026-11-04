package br.pucminas.aluguelcarros.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
class ClienteControllerTest {

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

    // ---------- helpers ----------

    /** Gera um CPF com dígitos verificadores válidos. */
    private static String cpfValido() {
        int[] d = new int[11];
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < 9; i++) d[i] = r.nextInt(10);
        for (int t = 9; t < 11; t++) {
            int soma = 0;
            for (int i = 0; i < t; i++) soma += d[i] * (t + 1 - i);
            int resto = (soma * 10) % 11;
            d[t] = resto == 10 ? 0 : resto;
        }
        StringBuilder sb = new StringBuilder();
        for (int x : d) sb.append(x);
        return sb.toString();
    }

    private String token(String role) throws Exception {
        String login = "u" + UUID.randomUUID().toString().substring(0, 8);
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("login", login, "senha", "senha123", "role", role))))
                .andExpect(status().isCreated());
        String resp = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("login", login, "senha", "senha123"))))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(resp).get("token").asText();
    }

    private Map<String, Object> cliente(String cpf, int qtdRendimentos) {
        List<Map<String, Object>> rends = new java.util.ArrayList<>();
        for (int i = 0; i < qtdRendimentos; i++) {
            rends.add(Map.of("entidadeEmpregadora", "Empresa " + i, "valor", 1000 + i));
        }
        return Map.of("rg", "MG-12.345.678", "cpf", cpf, "nome", "Maria Silva",
                "endereco", "Rua A, 10", "profissao", "Engenheira", "rendimentos", rends);
    }

    private long criar(String auth, Map<String, Object> corpo) throws Exception {
        String resp = mvc.perform(post("/clientes").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resp).get("id").asLong();
    }

    private String body(Map<String, Object> corpo) {
        return json.writeValueAsString(corpo);
    }

    // ---------- testes ----------

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/clientes")).andExpect(status().isUnauthorized());
    }

    @Test
    void criaClienteComRendimentosENormalizaCpf() throws Exception {
        String auth = token("CLIENTE");
        String cpf = cpfValido();
        String mascarado = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);

        mvc.perform(post("/clientes").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(body(cliente(mascarado, 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.rendimentos.length()").value(3))
                .andExpect(jsonPath("$.rendimentos[0].id").exists());
    }

    @Test
    void maisDeTresRendimentosRetorna400() throws Exception {
        mvc.perform(post("/clientes").header("Authorization", token("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON).content(body(cliente(cpfValido(), 4))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.rendimentos").exists());
    }

    @Test
    void camposObrigatoriosECpfInvalidoRetornam400() throws Exception {
        mvc.perform(post("/clientes").header("Authorization", token("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("rg", "", "cpf", "111.111.111-11", "nome", "", "endereco", "x", "profissao", "y"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.rg").exists())
                .andExpect(jsonPath("$.campos.cpf").exists())
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void cpfDuplicadoRetorna409() throws Exception {
        String cpf = cpfValido();
        criar(token("CLIENTE"), cliente(cpf, 1));
        mvc.perform(post("/clientes").header("Authorization", token("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON).content(body(cliente(cpf, 1))))
                .andExpect(status().isConflict());
    }

    @Test
    void mesmoUsuarioNaoCriaDoisCadastros() throws Exception {
        String auth = token("CLIENTE");
        criar(auth, cliente(cpfValido(), 0));
        mvc.perform(post("/clientes").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(body(cliente(cpfValido(), 0))))
                .andExpect(status().isConflict());
    }

    @Test
    void agenteNaoCriaCliente() throws Exception {
        mvc.perform(post("/clientes").header("Authorization", token("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON).content(body(cliente(cpfValido(), 0))))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteListaSoOProprioEAgenteListaTodos() throws Exception {
        String a = token("CLIENTE");
        criar(a, cliente(cpfValido(), 1));
        criar(token("CLIENTE"), cliente(cpfValido(), 1));

        mvc.perform(get("/clientes").header("Authorization", a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/clientes").header("Authorization", token("AGENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void clienteNaoAcessaCadastroDeOutro_agenteConsulta() throws Exception {
        long id = criar(token("CLIENTE"), cliente(cpfValido(), 1));
        mvc.perform(get("/clientes/" + id).header("Authorization", token("CLIENTE")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/clientes/" + id).header("Authorization", token("AGENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        mvc.perform(get("/clientes/999999").header("Authorization", token("CLIENTE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizaDadosESubstituiRendimentos() throws Exception {
        String auth = token("CLIENTE");
        long id = criar(auth, cliente(cpfValido(), 3));

        Map<String, Object> novo = new java.util.HashMap<>(cliente(cpfValido(), 1));
        novo.put("nome", "Maria Souza");
        mvc.perform(put("/clientes/" + id).header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(body(novo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.rendimentos.length()").value(1));
        mvc.perform(get("/clientes/" + id).header("Authorization", auth))
                .andExpect(jsonPath("$.rendimentos.length()").value(1));
    }

    @Test
    void atualizarComCpfDeOutroRetorna409_eAgenteNaoAltera() throws Exception {
        String cpfOutro = cpfValido();
        criar(token("CLIENTE"), cliente(cpfOutro, 0));
        String auth = token("CLIENTE");
        long id = criar(auth, cliente(cpfValido(), 0));

        mvc.perform(put("/clientes/" + id).header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(body(cliente(cpfOutro, 0))))
                .andExpect(status().isConflict());
        mvc.perform(put("/clientes/" + id).header("Authorization", token("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON).content(body(cliente(cpfValido(), 0))))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeCliente() throws Exception {
        String auth = token("CLIENTE");
        long id = criar(auth, cliente(cpfValido(), 2));

        mvc.perform(delete("/clientes/" + id).header("Authorization", token("CLIENTE")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/clientes/" + id).header("Authorization", auth))
                .andExpect(status().isNoContent());
        mvc.perform(get("/clientes/" + id).header("Authorization", auth))
                .andExpect(status().isNotFound());
    }
}
