<!-- README baseado no template do Prof. Dr. João Paulo Aramuni: https://github.com/joaopauloaramuni/desenvolvimento-e-integracao-de-aplicacoes-web/blob/main/TEMPLATES/template_README.md -->

---

# 🚗 Sistema de Aluguel de Carros 👨‍💻

> [!NOTE]
> Sistema web para gestão de aluguéis de automóveis: clientes criam, modificam, consultam e cancelam pedidos pela Internet, e agentes (empresas e bancos) avaliam financeiramente os pedidos e executam os contratos.

<table>
  <tr>
    <td width="800px">
      <div align="justify">
        Projeto do <b>Laboratório 02</b> da disciplina <b>Desenvolvimento e Integração de Aplicações Web (DIAW)</b>, do curso de Engenharia de Software da PUC Minas, sob orientação do <a href="https://github.com/joaopauloaramuni">Prof. Dr. João Paulo Aramuni</a>. O sistema exige cadastro e autenticação prévios e é dividido em dois subsistemas: <b>gestão de pedidos e contratos</b> e <b>construção dinâmica das páginas web</b>. Foi desenvolvido com <b>Spring Boot</b> no back-end (arquitetura MVC), <b>React + Vite</b> no front-end e <b>Supabase (PostgreSQL)</b> como banco de dados.
      </div>
    </td>
    <td>
      <div>
        <img src="https://joaopauloaramuni.github.io/image/logo_ES_vertical.png" alt="Logo do Projeto" width="120px"/>
      </div>
    </td>
  </tr>
</table>

---

## 🚧 Status do Projeto

![Versão](https://img.shields.io/badge/Versão-v0.1.0-blue?style=for-the-badge) ![Java](https://img.shields.io/badge/Java-25-007ec6?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.x-007ec6?style=for-the-badge&logo=springboot&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-3.9-007ec6?style=for-the-badge&logo=apachemaven&logoColor=white) ![React](https://img.shields.io/badge/React-Vite-007ec6?style=for-the-badge&logo=react&logoColor=white) ![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-007ec6?style=for-the-badge&logo=supabase&logoColor=white)

**Entregas do laboratório:**

- [ ] Modelagem do sistema (Casos de Uso, Histórias do Usuário, Diagrama de Classes e Diagrama de Pacotes)
- [ ] Autenticação (login de clientes e agentes)
- [ ] CRUD de cliente
- [ ] Protótipo do sistema (criar e visualizar status de um pedido de aluguel)
- [ ] Revisão dos diagramas

---

## 📚 Índice
- [Links Úteis](#-links-úteis)
- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades Principais](#-funcionalidades-principais)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura](#-arquitetura)
- [Instalação e Execução](#-instalação-e-execução)
  - [Pré-requisitos](#pré-requisitos)
  - [Variáveis de Ambiente](#-variáveis-de-ambiente)
  - [Instalação de Dependências](#-instalação-de-dependências)
  - [Banco de Dados (Supabase)](#-banco-de-dados-supabase)
  - [Como Executar a Aplicação](#-como-executar-a-aplicação)
- [Estrutura de Pastas](#-estrutura-de-pastas)
- [Demonstração](#-demonstração)
- [Testes](#-testes)
- [Documentações utilizadas](#-documentações-utilizadas)
- [Autores](#-autores)
- [Contribuição](#-contribuição)
- [Agradecimentos](#-agradecimentos)
- [Licença](#-licença)

---

## 🔗 Links Úteis
* 📖 **Modelagem UML e Histórias de Usuário:** [`/docs`](./docs)
  > 📚 **Descrição:** Diagramas de casos de uso, classes e pacotes (todas as versões) e histórias de usuário.
* 📄 **Enunciado do laboratório:** [PDF](./DIAW_-_LABORATÓRIO_02_-_Sistema_de_Aluguel_de_Carros.pdf)

---

## 📝 Sobre o Projeto
Pretende-se apoiar a gestão de aluguéis de automóveis, permitindo efetuar, cancelar e modificar pedidos através da Internet.

- **Contexto:** trabalho acadêmico (Laboratório 02, DIAW, PUC Minas).
- **Problema que resolve:** centraliza o ciclo de vida do aluguel — do pedido do cliente à análise financeira pelo agente e à execução do contrato (incluindo contrato de crédito concedido por um banco).
- **Quem usa:**
  - **Clientes (usuários individuais):** introduzem, modificam, consultam e cancelam pedidos de aluguel.
  - **Agentes (empresas e bancos):** modificam e avaliam pedidos. Em caso de parecer financeiro positivo, o pedido é colocado à consideração do cliente para execução do contrato.
- **Dados armazenados:**
  - *Contratantes:* RG, CPF, nome, endereço, profissão, entidades empregadoras e respectivos rendimentos (máximo 3).
  - *Automóveis:* matrícula, ano, marca, modelo e placa. Conforme o tipo de contrato, podem ser propriedade de clientes, empresas ou bancos.
  - *Contrato de crédito:* o aluguel pode estar associado a um crédito concedido por um dos bancos agentes.

---

## ✨ Funcionalidades Principais

- 🔐 **Autenticação:** cadastro prévio e login obrigatório (usuário e senha) para clientes e agentes.
- 👤 **CRUD de Cliente:** cadastro com dados de identificação, profissão e até 3 rendimentos.
- 📝 **Pedidos de aluguel:** o cliente cria, modifica, consulta e cancela pedidos e acompanha o status.
- 🏦 **Avaliação por agentes:** empresas e bancos analisam financeiramente e modificam pedidos.
- 📑 **Contratos:** execução do contrato após parecer positivo, com contrato de crédito opcional.
- 🚘 **Automóveis:** registro de matrícula, ano, marca, modelo, placa e proprietário.

---

## 🛠 Tecnologias Utilizadas

### 💻 Front-end

* **Framework/Biblioteca:** React
* **Linguagem:** JavaScript (ES6+)
* **Build Tool:** Vite
* **Requisições HTTP:** Axios
* **Roteamento:** React Router

### 🖥️ Back-end

* **Linguagem/Runtime:** Java (JDK 25)
* **Framework:** Spring Boot 4.0.x (arquitetura MVC)
* **Build e dependências:** Apache Maven
* **Banco de Dados:** Supabase (PostgreSQL)
* **ORM:** Spring Data JPA / Hibernate
* **Autenticação:** Spring Security (login com usuário e senha)

### ⚙️ Infraestrutura

* **Banco de dados em nuvem:** Supabase
* **Versionamento:** Git e GitHub

---

## 🏗 Arquitetura

O back-end segue o padrão **MVC** em camadas (`controller` → `service` → `repository` → `model`), com **DTOs** para entrada/saída da API e tratamento global de exceções. O front-end React consome a API REST do back-end.

O sistema é subdividido em dois subsistemas:
1. **Gestão de pedidos e contratos** (back-end).
2. **Construção dinâmica das páginas web** (front-end).

### Diagramas

Os diagramas (todas as versões produzidas) ficam em [`/docs`](./docs):

| Diagrama | Arquivo |
| :--- | :--- |
| Casos de Uso | _a adicionar em `/docs`_ |
| Histórias do Usuário | _a adicionar em `/docs`_ |
| Classes | _a adicionar em `/docs`_ |
| Pacotes (Visão Lógica) | _a adicionar em `/docs`_ |

---

## 🔧 Instalação e Execução

### Pré-requisitos

* **Java JDK:** versão **25** ou superior
* **Maven:** 3.9+
* **Node.js:** 22 LTS ou superior (com npm)
* **Conta no Supabase** com um projeto PostgreSQL criado

---

### 🔑 Variáveis de Ambiente

#### Back-end (Spring Boot)

Configure como variáveis de ambiente do sistema (nunca versione senhas):

| Variável | Descrição | Exemplo |
| :--- | :--- | :--- |
| `SERVER_PORT` | Porta do Back-end. | `8080` |
| `SPRING_DATASOURCE_URL` | URL JDBC do Supabase (aba *Connect*). | `jdbc:postgresql://<host>:5432/postgres?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco. | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco. | `********` |
| `JWT_SECRET` | Chave secreta para assinatura de tokens. | `chave_super_segura_base64` |

#### Front-end (React, Vite)

Crie `/frontend/.env.local`:

| Variável | Descrição | Exemplo |
| :--- | :--- | :--- |
| `VITE_API_URL` | URL base da API Spring Boot. | `http://localhost:8080` |

---

### 📦 Instalação de Dependências

```bash
git clone https://github.com/elijuniordd/lab2-diaw.git
cd lab2-diaw
```

**Front-end:**
```bash
cd frontend
npm install
cd ..
```

**Back-end:**
```bash
cd backend
./mvnw clean install
cd ..
```

---

### 💾 Banco de Dados (Supabase)

1. Crie um projeto em [supabase.com](https://supabase.com).
2. Em **Connect**, copie host, porta, usuário e senha e monte a `SPRING_DATASOURCE_URL`.
3. O schema é criado automaticamente pelo Hibernate (`ddl-auto`) na primeira execução do Back-end. As tabelas aparecem no *Table Editor* do Supabase.

---

### ⚡ Como Executar a Aplicação

Em **dois terminais separados**:

**Terminal 1: Back-end (Spring Boot)**
```bash
cd backend
./mvnw spring-boot:run
```
🚀 *Disponível em **http://localhost:8080**.*

**Terminal 2: Front-end (React, Vite)**
```bash
cd frontend
npm run dev
```
🎨 *Disponível em **http://localhost:5173**.*

---

## 📂 Estrutura de Pastas

```
.
├── .gitignore                   # 🧹 Arquivos/pastas não versionados (.env, node_modules, target, etc.).
├── README.md                    # 📘 Documentação principal do projeto.
│
├── /frontend                    # 📁 Aplicação React + Vite
│   └── /src
│       ├── /components          # 🧱 Componentes reutilizáveis.
│       ├── /pages               # 📄 Páginas (Login, Cadastro, Clientes, Pedidos).
│       └── /services            # 🔌 Chamadas HTTP (Axios).
│
├── /backend                     # 📁 Aplicação Spring Boot
│   └── /src/main/java/br/pucminas/aluguelcarros
│       ├── /controller          # 🎮 Endpoints REST.
│       ├── /service             # ⚙️ Regras de negócio.
│       ├── /repository          # 🗄️ Repositórios (JPA).
│       ├── /model               # 🧬 Entidades JPA.
│       ├── /dto                 # ✉️ Data Transfer Objects.
│       ├── /config              # 🔧 Configurações (CORS, etc.).
│       ├── /exception           # 💥 Exceptions e handlers globais.
│       └── /security            # 🛡️ Autenticação e autorização (Spring Security).
│
└── /docs                        # 📚 Diagramas UML (todas as versões) e histórias de usuário.
```

---

## 🎥 Demonstração

_Prints e GIFs do protótipo serão adicionados conforme as telas forem implementadas (login, cadastro de clientes, criação e acompanhamento de pedidos)._

---

## 🧪 Testes

**Back-end:**
```bash
cd backend
./mvnw test
```
*Ferramenta: JUnit (Spring Boot Test).*

**Front-end:** _a definir._

---

## 🔗 Documentações utilizadas

* 📖 **Back-end:** [Spring Boot](https://docs.spring.io/spring-boot/index.html)
* 📖 **Front-end:** [React](https://react.dev/) e [Vite](https://vitejs.dev/guide/)
* 📖 **Banco de dados:** [Supabase](https://supabase.com/docs)
* 📖 **Padrão de commits:** [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)

---

## 👥 Autores

| 👤 Nome | :octocat: GitHub | 📤 Gmail |
|---------|-----------------|-----------|
| Elias Junior Dias | [elijuniordd](https://github.com/elijuniordd) | [elijuniordias098@gmail.com](mailto:elijuniordias098@gmail.com) |
| _Nome 2_ | _GitHub_ | _E-mail_ |

---

## 🤝 Contribuição

1. Faça um `fork` do projeto.
2. Crie uma branch para sua feature (`git checkout -b feature/minha-feature`).
3. Commit suas mudanças (`git commit -m 'feat: adiciona nova funcionalidade X'`), usando [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
4. Faça o `push` para a branch (`git push origin feature/minha-feature`).
5. Abra um **Pull Request**.

---

## 🙏 Agradecimentos

* [**Engenharia de Software PUC Minas**](https://www.instagram.com/engsoftwarepucminas/) - Pelo apoio institucional e estrutura acadêmica.
* [**Prof. Dr. João Paulo Aramuni**](https://github.com/joaopauloaramuni) - Pelos ensinamentos sobre Arquitetura de Software e Padrões de Projeto, e pelo template deste README.

---

## 📄 Licença

Projeto acadêmico, desenvolvido para fins educacionais.
