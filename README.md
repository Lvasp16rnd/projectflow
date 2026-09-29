# 🚀 ProjectFlow

Uma plataforma corporativa resiliente para gerenciar solicitações, processos internos e tarefas, desenhada com foco em consistência distribuída, processamento assíncrono e arquitetura limpa.

![Java](https://img.shields.io/badge/java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![AWS SQS](https://img.shields.io/badge/AWS-SQS-orange.svg)
![Testcontainers](https://img.shields.io/badge/Testcontainers-Integration-success.svg)

## Sobre o projeto

O ProjectFlow resolve o problema clássico de fluxos de aprovação corporativos (como Jira ou ServiceNow), mas com um diferencial arquitetural profundo. O foco não é apenas ser um CRUD de tickets, mas sim um sistema que garante que nenhuma solicitação será perdida, aprovada duplicadamente ou ficará travada em caso de falha de rede.

## 🛠 Metodologia de Desenvolvimento

Este projeto adota duas práticas avançadas de engenharia que moldam a qualidade da entrega:

1. **Spec-Driven Development (Desenvolvimento Orientado a Especificação):** Antes de qualquer linha de código ser escrita, o domínio (regras, máquina de estados, enums) foi estritamente especificado e validado de forma independente de frameworks. As regras de negócio ditam o código, e não o inverso.
2. **Test Harness (Testes como Blindagem):** A arquitetura é validada por uma suíte rigorosa de testes desde o primeiro dia. O núcleo (domínio) possui ampla cobertura unitária, garantindo a máquina de estados. Já as bordas (banco e mensageria) operam sob um *Test Harness* pesado utilizando **Testcontainers** (PostgreSQL real e LocalStack). Isso significa que simulamos os exatos cenários de produção e de falha localmente, garantindo que mecanismos complexos como o *Transactional Outbox* e *Optimistic Locking* funcionem em ambientes reais.

## Funcionalidades principais

- Máquina de estados finita para solicitações (garantindo transições imutáveis de acordo com regras de negócio corporativas).
- Trilha de auditoria assíncrona orientada a eventos.
- Processamento seguro via mensagens SQS com garantia de entrega.
- Controle rigoroso de concorrência em aprovações.

## Stack Tecnológica

- **Java 21 & Spring Boot 4.x:** Aproveitamento de features recentes, como *Virtual Threads* e novas APIs de injeção e teste (`@MockitoBean`, `@ServiceConnection`).
- **PostgreSQL (via Flyway):** Persistência relacional estável, versionada com Flyway para previsibilidade de deploy.
- **AWS SQS (via Spring Cloud AWS):** Mensageria robusta para processamento assíncrono.
- **Testcontainers & LocalStack:** Ambiente de testes de integração espelhado no ambiente de produção AWS.

## Arquitetura e Decisões Técnicas Relevantes

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)**, garantindo que o Domínio seja puro (sem dependências de JPA, Spring ou Cloud).

### 1. Transactional Outbox Pattern
**Por que?** Evita o problema do *dual-write*. Salvar no banco e publicar no SQS em passos separados pode causar inconsistência se a rede falhar.
**Como?** A solicitação e um evento no `Outbox` são salvos na mesma transação `@Transactional`. Um *Scheduler (Poller)* lê essa tabela assincronamente e envia ao SQS de forma segura.

### 2. Optimistic Locking para Concorrência
**Por que?** Em cenários corporativos, dois diretores podem clicar em "Aprovar" a mesma solicitação simultaneamente, gerando inconsistências no grafo de estado.
**Como?** A entidade possui um campo `@Version`. Se a versão em memória divergir da versão do banco no momento do *update*, uma exceção `409 Conflict` aborta a segunda operação.

### 3. Otimização do Spring Data (Interface Persistable)
**Por que?** Como o ID da solicitação (UUID) é gerado na aplicação (pelo Domínio) e não pelo banco de dados, o Spring Data JPA faria um `SELECT` desnecessário antes de todo `INSERT` para verificar se a entidade já existia.
**Como?** A entidade implementa `Persistable<UUID>` e define `isNew()` baseado na existência da versão (onde `version == null` indica um novo registro). Isso elimina a ida extra ao banco e mantém o código limpo sem flags transitórias.

### 4. Tratamento Semântico de APIs
Uso restrito e semântico dos códigos HTTP:
- `400 Bad Request`: Exclusivo para falhas de *Bean Validation* (payload mal formatado).
- `422 Unprocessable Entity`: Quando o payload está correto, mas viola a máquina de estados ou regras de negócio do Domínio.

## Como rodar o projeto localmente

**Pré-requisitos:** Docker (rodando), Java 21+ e Maven.

1. **Subir os serviços dependentes (Banco de dados e LocalStack):**
   ```bash
   # Você pode utilizar o docker-compose (em breve na v2) ou executar os testes diretamente, 
   # que subirão o ambiente provisório via Testcontainers automaticamente.
   ```

2. **Rodar a aplicação:**
   ```bash
   cd request-service
   ./mvnw spring-boot:run
   ```

3. **Rodar a suíte de Test Harness (Integração):**
   ```bash
   cd request-service
   ./mvnw test
   ```

## Estrutura de Pastas (Hexagonal)

```
request-service/src/main/java/com/projectflow/request/
├── domain/            # Coração puro do sistema (Modelos, Máquina de Estados, Portas)
├── application/       # Orquestração (Use Cases e Commands)
├── infrastructure/    # Adaptadores de Banco (JPA, Flyway) e Mensageria (SQS, Poller)
└── presentation/      # Inbound Adapters (REST Controllers e Exception Handling)
```

## Status e Roadmap

- [x] Especificação e Desenho Arquitetural
- [x] Domínio e Regras de Negócio Testadas
- [x] Camada de Persistência (Postgres + Flyway + Optimistic Locking)
- [x] Casos de Uso (Application Layer)
- [x] Camada REST (Validação, Tratamento Semântico de Erros)
- [x] Transactional Outbox Pattern implementado e provado via Testcontainers
- [x] Módulo Consumidor: Notification Service (MongoDB Auditoria + Idempotência via DLQ)
- [x] Orquestração Local com Docker Compose (Postgres, Mongo, LocalStack)
- [x] Interface Frontend (Angular 18 com Glassmorphism)
- [ ] Workflow Service
---
*Desenvolvido como demonstração de padrões arquiteturais avançados e engenharia de software resiliente.*
