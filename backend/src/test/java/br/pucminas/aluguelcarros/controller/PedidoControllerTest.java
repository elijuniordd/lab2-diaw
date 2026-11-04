package br.pucminas.aluguelcarros.controller;

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
class PedidoControllerTest {

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

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
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

    /** Cliente com cadastro completo (necessário para criar pedidos). */
    private String clienteComCadastro() throws Exception {
        String auth = token("CLIENTE");
        mvc.perform(post("/clientes").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("rg", "MG-1", "cpf", cpfValido(), "nome", "Cliente Teste",
                        "endereco", "Rua A", "profissao", "Dev",
                        "rendimentos", List.of(Map.of("entidadeEmpregadora", "Acme", "valor", 4000))))))
                .andExpect(status().isCreated());
        return auth;
    }

    private static final java.util.concurrent.atomic.AtomicInteger SEQ = new java.util.concurrent.atomic.AtomicInteger();

    /** Placa sequencial (AAA1A00, AAB1A00, ...), sem risco de colisão entre testes. */
    private static String placaUnica() {
        int n = SEQ.getAndIncrement();
        return "" + (char) ('A' + n / 676 % 26) + (char) ('A' + n / 26 % 26) + (char) ('A' + n % 26) + "1A00";
    }

    private long automovel() throws Exception {
        String s = sufixo();
        String resp = mvc.perform(post("/automoveis").header("Authorization", token("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("matricula", "M" + s, "ano", 2022, "marca", "Fiat",
                        "modelo", "Argo", "placa", placaUnica()))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(resp).get("id").asLong();
    }

    private long pedido(String authCliente, long automovelId) throws Exception {
        String resp = mvc.perform(post("/pedidos").header("Authorization", authCliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("automovelId", automovelId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resp).get("id").asLong();
    }

    private String avaliacao(boolean aprovado) {
        return json.writeValueAsString(Map.of("aprovado", aprovado, "parecer", "Renda compatível"));
    }

    // ---------- automóveis ----------

    @Test
    void agenteCadastraAutomovelEClienteApenasConsulta() throws Exception {
        long id = automovel();
        mvc.perform(get("/automoveis").header("Authorization", token("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + id + ")]").exists());
        mvc.perform(post("/automoveis").header("Authorization", token("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("matricula", "X", "ano", 2020, "marca", "A", "modelo", "B", "placa", "ABC1D23"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void automovelComPlacaInvalidaOuDuplicadaFalha() throws Exception {
        String auth = token("AGENTE");
        Map<String, Object> base = Map.of("matricula", "DUP" + sufixo(), "ano", 2021, "marca", "VW", "modelo", "Polo", "placa", "ZZZ9Z99");
        mvc.perform(post("/automoveis").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("matricula", "m", "ano", 2021, "marca", "VW", "modelo", "Polo", "placa", "123"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.placa").exists());
        mvc.perform(post("/automoveis").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(base))).andExpect(status().isCreated());
        mvc.perform(post("/automoveis").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("matricula", "OUTRA" + sufixo(), "ano", 2021, "marca", "VW", "modelo", "Polo", "placa", "zzz-9z99"))))
                .andExpect(status().isConflict());
    }

    // ---------- pedidos ----------

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/pedidos")).andExpect(status().isUnauthorized());
    }

    @Test
    void clienteCriaPedidoPendenteEVeApenasOsSeus() throws Exception {
        String a = clienteComCadastro();
        long id = pedido(a, automovel());
        pedido(clienteComCadastro(), automovel());

        mvc.perform(get("/pedidos").header("Authorization", a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].automovel.marca").value("Fiat"))
                .andExpect(jsonPath("$[0].cliente.rendimentos.length()").value(1));
    }

    @Test
    void clienteSemCadastroNaoCriaPedido_agenteNaoCria() throws Exception {
        long carro = automovel();
        String corpo = json.writeValueAsString(Map.of("automovelId", carro));
        mvc.perform(post("/pedidos").header("Authorization", token("CLIENTE")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
        mvc.perform(post("/pedidos").header("Authorization", token("AGENTE")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void pedidoComAutomovelInexistenteRetorna404() throws Exception {
        mvc.perform(post("/pedidos").header("Authorization", clienteComCadastro()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("automovelId", 999999))))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteNaoVePedidoDeOutro_agenteVe() throws Exception {
        long id = pedido(clienteComCadastro(), automovel());
        mvc.perform(get("/pedidos/" + id).header("Authorization", clienteComCadastro())).andExpect(status().isForbidden());
        mvc.perform(get("/pedidos/" + id).header("Authorization", token("AGENTE"))).andExpect(status().isOk());
    }

    @Test
    void clienteModificaEnquantoPendente() throws Exception {
        String a = clienteComCadastro();
        long id = pedido(a, automovel());
        long outro = automovel();

        mvc.perform(put("/pedidos/" + id).header("Authorization", a).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("automovelId", outro))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automovel.id").value(outro))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void agenteAvaliaEAprova_clienteVeParecer() throws Exception {
        String a = clienteComCadastro();
        long id = pedido(a, automovel());

        mvc.perform(put("/pedidos/" + id + "/avaliar").header("Authorization", token("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON).content(avaliacao(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.parecerAgente").value("Renda compatível"))
                .andExpect(jsonPath("$.agente").isNotEmpty());
        mvc.perform(get("/pedidos/" + id).header("Authorization", a))
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    void agenteReprova_eNaoAvaliaDuasVezes() throws Exception {
        long id = pedido(clienteComCadastro(), automovel());
        String agente = token("AGENTE");
        mvc.perform(put("/pedidos/" + id + "/avaliar").header("Authorization", agente)
                .contentType(MediaType.APPLICATION_JSON).content(avaliacao(false)))
                .andExpect(jsonPath("$.status").value("REPROVADO"));
        mvc.perform(put("/pedidos/" + id + "/avaliar").header("Authorization", agente)
                .contentType(MediaType.APPLICATION_JSON).content(avaliacao(true)))
                .andExpect(status().isConflict());
    }

    @Test
    void avaliacaoExigeAgenteEParecer() throws Exception {
        String a = clienteComCadastro();
        long id = pedido(a, automovel());
        mvc.perform(put("/pedidos/" + id + "/avaliar").header("Authorization", a)
                .contentType(MediaType.APPLICATION_JSON).content(avaliacao(true)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/pedidos/" + id + "/avaliar").header("Authorization", token("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("aprovado", true, "parecer", " "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clienteNaoModificaPedidoJaAvaliado() throws Exception {
        String a = clienteComCadastro();
        long id = pedido(a, automovel());
        mvc.perform(put("/pedidos/" + id + "/avaliar").header("Authorization", token("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON).content(avaliacao(true))).andExpect(status().isOk());
        mvc.perform(put("/pedidos/" + id).header("Authorization", a).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("automovelId", automovel()))))
                .andExpect(status().isConflict());
    }

    @Test
    void agenteModificaPedidoEElePassaParaEmAnalise() throws Exception {
        long id = pedido(clienteComCadastro(), automovel());
        mvc.perform(put("/pedidos/" + id).header("Authorization", token("AGENTE")).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("automovelId", automovel()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }

    @Test
    void clienteCancelaPedido_eNaoCancelaDeNovoNemDeOutro() throws Exception {
        String a = clienteComCadastro();
        long id = pedido(a, automovel());

        mvc.perform(put("/pedidos/" + id + "/cancelar").header("Authorization", clienteComCadastro()))
                .andExpect(status().isForbidden());
        mvc.perform(put("/pedidos/" + id + "/cancelar").header("Authorization", token("AGENTE")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/pedidos/" + id + "/cancelar").header("Authorization", a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(put("/pedidos/" + id + "/cancelar").header("Authorization", a))
                .andExpect(status().isConflict());
    }

    @Test
    void clienteComPedidoNaoPodeSerRemovido() throws Exception {
        String a = clienteComCadastro();
        pedido(a, automovel());
        String resp = mvc.perform(get("/clientes").header("Authorization", a)).andReturn().getResponse().getContentAsString();
        long clienteId = json.readTree(resp).get(0).get("id").asLong();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/clientes/" + clienteId)
                .header("Authorization", a)).andExpect(status().isConflict());
    }
}
