package br.pucminas.aluguelcarros.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

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
class ContratoControllerTest {

    private static final AtomicInteger SEQ = new AtomicInteger();

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

    /** Registra e loga; {@code tipo} só vale para AGENTE (pode ser nulo). */
    private String token(String role, String tipo) throws Exception {
        String login = "c" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> corpo = new HashMap<>(Map.of("login", login, "senha", "senha123", "role", role));
        if (tipo != null) corpo.put("tipoAgente", tipo);
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(corpo))).andExpect(status().isCreated());
        String resp = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("login", login, "senha", "senha123"))))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(resp).get("token").asText();
    }

    private String clienteComCadastro() throws Exception {
        String auth = token("CLIENTE", null);
        mvc.perform(post("/clientes").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("rg", "MG-1", "cpf", cpfValido(), "nome", "Cliente Teste",
                        "endereco", "Rua A", "profissao", "Dev",
                        "rendimentos", List.of(Map.of("entidadeEmpregadora", "Acme", "valor", 4000))))))
                .andExpect(status().isCreated());
        return auth;
    }

    private long automovel(String authAgente) throws Exception {
        int n = SEQ.getAndIncrement();
        String placa = "" + (char) ('C' + n / 676 % 24) + (char) ('A' + n / 26 % 26) + (char) ('A' + n % 26) + "1A00";
        String resp = mvc.perform(post("/automoveis").header("Authorization", authAgente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("matricula", "CT" + UUID.randomUUID().toString().substring(0, 8),
                        "ano", 2022, "marca", "Fiat", "modelo", "Argo", "placa", placa))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(resp).get("id").asLong();
    }

    /** Cria um pedido e o leva até APROVADO. */
    private long pedidoAprovado(String cliente, String agente) throws Exception {
        String resp = mvc.perform(post("/pedidos").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("automovelId", automovel(agente)))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = json.readTree(resp).get("id").asLong();
        mvc.perform(put("/pedidos/" + id + "/avaliar").header("Authorization", agente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("aprovado", true, "parecer", "Ok"))))
                .andExpect(status().isOk());
        return id;
    }

    private String contrato(long pedidoId) {
        return json.writeValueAsString(Map.of("pedidoId", pedidoId, "dataInicio", "2030-01-10",
                "dataFim", "2030-01-20", "valor", 1500.50));
    }

    private long contratar(String cliente, long pedidoId) throws Exception {
        String resp = mvc.perform(post("/contratos").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON).content(contrato(pedidoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ALUGUEL"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resp).get("id").asLong();
    }

    // ---------- testes ----------

    @Test
    void agenteSemTipoViraEmpresaEAutomovelGuardaProprietario() throws Exception {
        String empresa = token("AGENTE", null);
        String banco = token("AGENTE", "BANCO");
        mvc.perform(get("/auth/me").header("Authorization", empresa))
                .andExpect(jsonPath("$.tipoAgente").value("EMPRESA"));
        mvc.perform(get("/auth/me").header("Authorization", banco))
                .andExpect(jsonPath("$.tipoAgente").value("BANCO"));
        long id = automovel(banco);
        mvc.perform(get("/automoveis").header("Authorization", empresa))
                .andExpect(jsonPath("$[?(@.id==" + id + ")].proprietario.tipo").value("BANCO"));
    }

    @Test
    void clienteContrataPedidoAprovadoEPedidoViraContratado() throws Exception {
        String cliente = clienteComCadastro();
        String agente = token("AGENTE", null);
        long pedido = pedidoAprovado(cliente, agente);
        long contrato = contratar(cliente, pedido);

        mvc.perform(get("/pedidos/" + pedido).header("Authorization", cliente))
                .andExpect(jsonPath("$.status").value("CONTRATADO"));
        mvc.perform(get("/contratos/" + contrato).header("Authorization", cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pedidoId").value(pedido));
        // não dá para contratar de novo nem cancelar depois de contratado
        mvc.perform(post("/contratos").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON).content(contrato(pedido)))
                .andExpect(status().isConflict());
        mvc.perform(put("/pedidos/" + pedido + "/cancelar").header("Authorization", cliente))
                .andExpect(status().isConflict());
    }

    @Test
    void contratoExigeAprovacaoDonoECliente() throws Exception {
        String cliente = clienteComCadastro();
        String agente = token("AGENTE", null);
        String resp = mvc.perform(post("/pedidos").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("automovelId", automovel(agente)))))
                .andReturn().getResponse().getContentAsString();
        long pendente = json.readTree(resp).get("id").asLong();
        mvc.perform(post("/contratos").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON).content(contrato(pendente)))
                .andExpect(status().isConflict());

        long aprovado = pedidoAprovado(cliente, agente);
        mvc.perform(post("/contratos").header("Authorization", agente)
                .contentType(MediaType.APPLICATION_JSON).content(contrato(aprovado)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/contratos").header("Authorization", clienteComCadastro())
                .contentType(MediaType.APPLICATION_JSON).content(contrato(aprovado)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/contratos").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("pedidoId", aprovado, "dataInicio", "2030-02-10",
                        "dataFim", "2030-02-01", "valor", 100))))
                .andExpect(status().isConflict());
    }

    @Test
    void bancoAssociaCreditoUmaUnicaVez() throws Exception {
        String cliente = clienteComCadastro();
        String empresa = token("AGENTE", "EMPRESA");
        String banco = token("AGENTE", "BANCO");
        long contrato = contratar(cliente, pedidoAprovado(cliente, empresa));
        String credito = json.writeValueAsString(Map.of("valor", 8000, "juros", 1.99));

        mvc.perform(post("/contratos/" + contrato + "/credito").header("Authorization", empresa)
                .contentType(MediaType.APPLICATION_JSON).content(credito)).andExpect(status().isForbidden());
        mvc.perform(post("/contratos/" + contrato + "/credito").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON).content(credito)).andExpect(status().isForbidden());
        mvc.perform(post("/contratos/" + contrato + "/credito").header("Authorization", banco)
                .contentType(MediaType.APPLICATION_JSON).content(credito))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ALUGUEL_COM_CREDITO"))
                .andExpect(jsonPath("$.credito.valor").value(8000))
                .andExpect(jsonPath("$.credito.juros").value(1.99));
        mvc.perform(post("/contratos/" + contrato + "/credito").header("Authorization", banco)
                .contentType(MediaType.APPLICATION_JSON).content(credito)).andExpect(status().isConflict());
    }

    @Test
    void clienteSoVeSeusContratos() throws Exception {
        String dono = clienteComCadastro();
        String outro = clienteComCadastro();
        String agente = token("AGENTE", null);
        long contrato = contratar(dono, pedidoAprovado(dono, agente));

        mvc.perform(get("/contratos").header("Authorization", dono))
                .andExpect(jsonPath("$[?(@.id==" + contrato + ")]").exists());
        mvc.perform(get("/contratos").header("Authorization", outro)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/contratos/" + contrato).header("Authorization", outro)).andExpect(status().isForbidden());
        mvc.perform(get("/contratos").header("Authorization", agente))
                .andExpect(jsonPath("$[?(@.id==" + contrato + ")]").exists());
    }
}
