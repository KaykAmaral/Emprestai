# PROATI — Gerenciador de Equipamentos e Reservas

## Projeto

Sistema acadêmico (Fatec/LEG) para gestão de equipamentos (notebooks, tablets, etc.) de unidades escolares da SEDUC. 
Permite professores solicitarem empréstimos e administradores gerenciarem aprovações, retiradas e devoluções.

## Atores e Papéis

| Papel | Responsabilidades |
|-------|-------------------|
| `ADM_SUPREMO` | Gerenciar unidades escolares, designar PROATI responsáveis |
| `ADM_PROATI` | Gerenciar equipamentos, professores e reservas de uma unidade |
| `PROFESSOR` | Consultar disponibilidade, solicitar e acompanhar reservas |

## Stack Técnico

- **Backend**: Spring Boot 4.1, Java 21, MySQL 8, Spring Data JPA
- **Segurança**: Spring Security + JWT (JJWT 0.13.0), BCrypt para senhas
- **Documentação**: springdoc-openapi (Swagger UI)
- **Build**: Maven 3.9+
- **Deployment**: Docker + docker-compose

## Arquitetura

Camadas desacopladas: Controller (REST + DTO) → Service (lógica + RN) → Repository (JPA) → Database.

Segurança via JWT + autorização por papel (`@PreAuthorize`). Auditoria completa: cada ação (aprovar, recusar, retirar, devolver, cancelar) registra usuário responsável + timestamp.

## Núcleo do Domínio

**Ciclo de vida da reserva**:
1. `PENDENTE` — professor solicita (RF12)
2. `APROVADA` / `RECUSADA` — admin aprova (RF13) ou recusa (RF14) com motivo obrigatório
3. `RETIRADO` — admin registra retirada, selecionando equipamentos específicos (RF15)
4. `DEVOLVIDO` — admin registra devolução (RF16)
5. `CANCELADA` — professor ou admin cancela (RF17, com restrições de estado)
6. `ATRASADA` — derivado: `status=RETIRADO AND data_hora_fim < agora` (RN22)

**Restrições críticas** (regras de negócio RN01–RN24):
- Uma unidade escolar tem **1–2 ADM_PROATI** (RN01)
- Email **único** no sistema (RN04)
- Número de patrimônio de equipamento **único** em todo o sistema (RN09)
- Aprovação de reserva **respeita capacidade disponível** por tipo/horário (RN15 — soma de aprovadas não ultrapassa disponíveis)
- Cancelamento **só até retirada**, respeitando horário de início para professor (RN19/RN20)
- **Alerta automático** (sem cancelamento) se equipamento com reservas futuras aprovadas mudar para manutenção/inativo (RN23/RF20)

## Estrutura de Diretórios

```
emprestai_backend/
├── src/main/java/com/example/emprestai/
│   ├── docs/                 (database.md, api.md, architecture.md)
│   ├── controller/           (endpoints REST)
│   ├── service/              (lógica de negócio, RN01–RN24)
│   ├── repository/           (Spring Data JPA)
│   ├── entity/               (entidades JPA)
│   ├── dto/                  (request/response DTOs)
│   ├── config/               (SecurityConfig, JWT config)
│   ├── exception/            (exceções customizadas)
│   └── EmprestaiApplication.java
├── pom.xml
└── Dockerfile
└── docker-compose.yml
```

## Documentação Técnica

- **`docs/database.md`** — modelo de dados (entidades, relacionamentos, constraints, índices)
- **`docs/api.md`** — especificação de endpoints REST (métodos, paths, autorizações)
- **`docs/architecture.md`** — arquitetura em camadas, segurança, configuração, Docker

Documentação de requisitos completa em `documentos/`:
- `requisitos_proati.md` (RF, RNF, RN)
- `backlog_sprints_proati.md` (product backlog, plano de 8 sprints)
- `casos_de_uso_proati.mermaid.md` (diagrama de casos de uso)

## Escopo Atual

**Backend only** — sem frontend. Branch principal: `backend`.

Código em estágio inicial (Sprint 0): apenas `EmprestaiApplication.java`, nenhuma entidade ou controller implementado ainda. Documentação técnica (docs/) agora preenchida.

## Próximas Prioridades

1. Implementar entidades JPA (UnidadeEscolar, Usuario, Equipamento, Reserva, ReservaEquipamento)
2. Autenticação + JWT + Spring Security
3. Controllers e Services para CRUD de unidades, professores, equipamentos
4. Lógica complexa de reservas (RN15 — validação de capacidade, ciclo de vida)
5. Testes de integração, relatórios, ajustes finais

Ver `backlog_sprints_proati.md` para cronograma detalhado (16 semanas, 8 sprints).
