# Histórias de Usuário — Sistema de Aluguel de Carros

> Versão 1 (texto original) + **status de implementação** ao final do documento. Atores: **Cliente** (usuário individual) e **Agente** (especializações **Empresa** e **Banco**).

## Autenticação

| ID | História | Critérios de aceitação |
|----|----------|------------------------|
| US01 | Como **cliente**, quero me cadastrar no sistema para poder utilizá-lo. | Informo login e senha; login é único; a senha é armazenada criptografada. |
| US02 | Como **usuário** (cliente ou agente), quero fazer login com usuário e senha para acessar o sistema. | Credenciais válidas dão acesso; inválidas exibem erro; sem login nenhuma tela é acessível. |
| US03 | Como **usuário**, quero encerrar a sessão (logout). | O acesso às telas protegidas deixa de ser permitido. |

## Cliente

| ID | História | Critérios de aceitação |
|----|----------|------------------------|
| US04 | Como **cliente**, quero cadastrar meus dados de identificação (RG, CPF, nome, endereço) e profissão. | CPF único; campos obrigatórios validados. |
| US05 | Como **cliente**, quero informar entidades empregadoras e rendimentos (máximo 3). | Não é possível salvar mais de 3 rendimentos. |
| US06 | Como **cliente**, quero consultar, alterar e excluir meu cadastro. | Alterações refletem imediatamente; exclusão pede confirmação. |

## Pedidos de aluguel (Cliente)

| ID | História | Critérios de aceitação |
|----|----------|------------------------|
| US07 | Como **cliente**, quero **introduzir** um pedido de aluguel escolhendo um automóvel. | O pedido nasce com status `PENDENTE`. |
| US08 | Como **cliente**, quero **consultar** meus pedidos e ver o status de cada um. | Vejo apenas os meus pedidos, com status e parecer do agente. |
| US09 | Como **cliente**, quero **modificar** um pedido ainda não decidido. | Só é possível modificar enquanto `PENDENTE`. |
| US10 | Como **cliente**, quero **cancelar** um pedido. | Status muda para `CANCELADO`; não é possível cancelar pedido já `CONTRATADO`. |
| US11 | Como **cliente**, quero, diante de um parecer positivo, **confirmar a execução do contrato**. | Só pedidos `APROVADO` podem ser contratados; status muda para `CONTRATADO` e o contrato é gerado. |

## Agentes (Empresa e Banco)

| ID | História | Critérios de aceitação |
|----|----------|------------------------|
| US12 | Como **agente**, quero listar os pedidos recebidos para análise. | Listagem com status e dados do cliente (incluindo rendimentos). |
| US13 | Como **agente**, quero **avaliar financeiramente** um pedido e emitir parecer (positivo ou negativo). | Status vai para `APROVADO` ou `REPROVADO`; parecer textual é registrado. |
| US14 | Como **agente**, quero **modificar** um pedido (ex.: condições ou automóvel). | Alteração fica registrada e o pedido volta para `EM_ANALISE`, se necessário. |
| US15 | Como **empresa**, quero cadastrar automóveis (matrícula, ano, marca, modelo, placa) e ser registrada como proprietária. | Matrícula e placa únicas. |
| US16 | Como **banco**, quero associar um **contrato de crédito** a um aluguel. | Contrato de crédito guarda banco, valor e juros e fica ligado ao contrato. |

## Automóveis

| ID | História | Critérios de aceitação |
|----|----------|------------------------|
| US17 | Como **usuário**, quero consultar automóveis disponíveis (matrícula, ano, marca, modelo, placa). | O proprietário pode ser cliente, empresa ou banco, conforme o tipo de contrato. |

## Ciclo de vida do pedido

```
PENDENTE ──(agente inicia análise)──> EM_ANALISE ──(parecer positivo)──> APROVADO ──(cliente confirma)──> CONTRATADO
    │                                      │
    │                                      └──(parecer negativo)──> REPROVADO
    └──(cliente cancela, antes de CONTRATADO)──> CANCELADO
```

## Requisitos não funcionais (do enunciado)

- Sistema **web**, com servidor central acessado pela Internet.
- Back-end em **Spring Boot 4.0.x (Java 25)** com arquitetura **MVC**; front-end **React + Vite**; banco **Supabase (PostgreSQL)**.
- **Login obrigatório** para qualquer uso.

## Status de implementação (Fase 8)

| Situação | Histórias | Observação |
|----------|-----------|------------|
| ✅ Implementada | US01, US02, US03 | Cadastro com senha BCrypt, login com JWT e logout no front. O cadastro aceita os perfis CLIENTE e AGENTE. |
| ✅ Implementada | US04, US05, US06 | CRUD de cliente com CPF único e no máximo 3 rendimentos; exclusão pede confirmação. Um cadastro de cliente por usuário. |
| ✅ Implementada | US07, US08, US09, US10 | Pedido nasce `PENDENTE`; cliente vê só os seus; modifica enquanto `PENDENTE`; cancela até ser `CONTRATADO`. |
| ✅ Implementada | US12, US13, US14 | Agente lista todos os pedidos com os rendimentos do cliente, avalia com parecer (`APROVADO`/`REPROVADO`) e, ao modificar, o pedido vai para `EM_ANALISE`. |
| 🟡 Parcial | US15, US17 | Agentes cadastram automóveis (matrícula e placa únicas) e qualquer usuário os consulta. Ainda **não** há proprietário do automóvel, nem distinção entre Empresa e Banco. |
| ⏳ Prevista | US11 | Confirmação do contrato pelo cliente (`APROVADO` -> `CONTRATADO`) depende da entidade Contrato. |
| ⏳ Prevista | US16 | Contrato de crédito associado a um aluguel, concedido por um Banco. |

**Decisões de projeto em relação à v1**
- Existem apenas dois perfis (`CLIENTE` e `AGENTE`); Empresa e Banco serão especializações de Agente quando o contrato for implementado.
- O agente pode avaliar um pedido direto de `PENDENTE`; o status `EM_ANALISE` é atingido quando o agente modifica o pedido.
- Apenas o cliente cancela o próprio pedido (US10).
- Um cliente com pedidos não pode ser removido (evita pedidos órfãos).
