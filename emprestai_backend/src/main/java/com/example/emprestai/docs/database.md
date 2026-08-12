# Database Design — Sistema PROATI

## Modelo Conceitual

O banco de dados centraliza três domínios: unidades escolares e seus gestores, equipamentos categorizados por tipo, e um fluxo de reserva com ciclo de vida bem definido (PENDENTE → APROVADA/RECUSADA → RETIRADO → DEVOLVIDO/CANCELADA/ATRASADA).

## Entidades e Atributos

### `UnidadeEscolar`
| Campo | Tipo | Restrições | Nota |
|-------|------|-----------|------|
| id | Long | PK | Auto-incremento |
| nome | String(255) | NOT NULL, UNIQUE | Nome da escola |
| ativo | Boolean | NOT NULL, default=true | Soft delete (RN03) |
| criadoEm | LocalDateTime | NOT NULL | Auditoria |
| atualizadoEm | LocalDateTime | — | Auditoria |

**Regras**: RN01 (1–2 ADM_PROATI por unidade), RN03 (soft delete), RN06 (PROATI cadastra professores só da própria unidade).

---

### `Usuario`
| Campo | Tipo | Restrições | Nota |
|-------|------|-----------|------|
| id | Long | PK | Auto-incremento |
| nome | String(255) | NOT NULL | Nome completo |
| email | String(255) | NOT NULL, UNIQUE | RN04: email único no sistema |
| senha | String(255) | NOT NULL | Hash BCrypt (RN05: nunca texto plano) |
| papel | Enum | NOT NULL | `ADM_SUPREMO`, `ADM_PROATI`, `PROFESSOR` |
| unidadeEscolar_id | Long | FK, conditional | FK para `UnidadeEscolar`; obrigatória se papel=`ADM_PROATI` ou `PROFESSOR` (RN02/RN07); nula se `ADM_SUPREMO` |
| ativo | Boolean | NOT NULL, default=true | Soft delete |
| criadoEm | LocalDateTime | NOT NULL | Auditoria |
| atualizadoEm | LocalDateTime | — | Auditoria |

**Constraints**:
- `UNIQUE(email)` — RN04
- `FK(unidadeEscolar_id)` referencia `UnidadeEscolar(id)`
- Validação em camada Service: ADM_PROATI e PROFESSOR exigem unidadeEscolar; ADM_SUPREMO não pode ter unidadeEscolar

**Regras**: RF01 (autenticação email/senha), RN02/RN07 (vinculação a unidade), RN05 (hash), RNF07 (auditoria).

---

### `Equipamento`
| Campo | Tipo | Restrições | Nota |
|-------|------|-----------|------|
| id | Long | PK | Auto-incremento |
| sigla | String(50) | NOT NULL | Ex: "NB", "TB" (RN08: única por unidade) |
| numeroPatrimonio | String(100) | NOT NULL, UNIQUE | RN09: único em todo o sistema |
| tipo | String(100) | NOT NULL | Ex: "Notebook", "Tablet" (listar tipo distinto para RF11) |
| marca | String(100) | — | Ex: "Dell", "Apple" |
| modelo | String(100) | — | Ex: "ThinkPad E14" |
| status | Enum | NOT NULL | `DISPONIVEL`, `MANUTENCAO`, `INATIVO` (RN10: só `DISPONIVEL` conta para RF11) |
| unidadeEscolar_id | Long | FK, NOT NULL | FK para `UnidadeEscolar` |
| criadoEm | LocalDateTime | NOT NULL | Auditoria |
| atualizadoEm | LocalDateTime | — | Auditoria |

**Constraints**:
- `UNIQUE(numeroPatrimonio)` — RN09
- `UNIQUE(sigla, unidadeEscolar_id)` — RN08 (sigla única por unidade)
- `FK(unidadeEscolar_id)` referencia `UnidadeEscolar(id)`

**Regras**: RF09/RF10 (CRUD de equipamento), RN08/RN09 (unicidade), RN10 (apenas DISPONIVEL conta), RN11 (só PROATI da unidade edita/deleta), RNF07 (auditoria).

---

### `Reserva`
| Campo | Tipo | Restrições | Nota |
|-------|------|-----------|------|
| id | Long | PK | Auto-incremento |
| professor_id | Long | FK, NOT NULL | FK para `Usuario` (papel=PROFESSOR) |
| unidadeEscolar_id | Long | FK, NOT NULL | Redundância para facilitar queries; sempre = professor.unidadeEscolar |
| tipoEquipamento | String(100) | NOT NULL | Tipo (ex: "Notebook") solicitado |
| quantidade | Integer | NOT NULL | Qtd solicitada (RN14: múltiplas PENDENTE coexistem) |
| dataHoraInicio | LocalDateTime | NOT NULL | Início do intervalo (RN13: deve ser futuro) |
| dataHoraFim | LocalDateTime | NOT NULL | Fim do intervalo (RN13: > dataHoraInicio) |
| status | Enum | NOT NULL | `PENDENTE`, `APROVADA`, `RECUSADA`, `RETIRADO`, `DEVOLVIDO`, `CANCELADA` (RN22: `ATRASADA` é calculado, não persistido) |
| motivoRecusa | Text | — | Obrigatório se status=`RECUSADA` (RN16) |
| usuarioAprovador_id | Long | FK | FK para `Usuario` que aprovó (auditoria — RNF07) |
| dataHoraAprovacao | LocalDateTime | — | Quando foi aprovada (RNF07) |
| usuarioRecusador_id | Long | FK | FK para `Usuario` que recusou (RNF07) |
| dataHoraRecusa | LocalDateTime | — | Quando foi recusada (RNF07) |
| usuarioRetirante_id | Long | FK | FK para `Usuario` que registrou retirada (RNF07) |
| dataHoraRetirada | LocalDateTime | — | Quando foi retirada; só após APROVADA (RN17 — RNF07) |
| usuarioDevolvendor_id | Long | FK | FK para `Usuario` que registrou devolução (RNF07) |
| dataHoraDevolucao | LocalDateTime | — | Quando foi devolvida; só após RETIRADO (RN18 — RNF07) |
| usuarioCancelador_id | Long | FK | FK para `Usuario` que cancelou (RNF07) |
| dataHoraCancelamento | LocalDateTime | — | Quando foi cancelada; só até retirada (RN19/RN20 — RNF07) |
| criadoEm | LocalDateTime | NOT NULL | Auditoria |
| atualizadoEm | LocalDateTime | — | Auditoria |

**Constraints**:
- `FK(professor_id)` referencia `Usuario(id)` onde papel=PROFESSOR
- `FK(unidadeEscolar_id)` referencia `UnidadeEscolar(id)`
- `FK(usuarioAprovador_id)`, `usuarioRecusador_id`, `usuarioRetirante_id`, `usuarioDevolvendor_id`, `usuarioCancelador_id` referenciam `Usuario(id)` onde papel=ADM_PROATI (validado em Service)

**Regras**: RF12/RF13/RF14/RF15/RF16/RF17 (ciclo de vida), RN13/RN14/RN15/RN16/RN17/RN18/RN19/RN20/RN21/RN22 (transições e estado), RNF07 (auditoria completa de cada transição).

---

### `ReservaEquipamento`
| Campo | Tipo | Restrições | Nota |
|-------|------|-----------|------|
| id | Long | PK | Auto-incremento |
| reserva_id | Long | FK, NOT NULL | FK para `Reserva` |
| equipamento_id | Long | FK, NOT NULL | FK para `Equipamento` |

**Constraints**:
- `FK(reserva_id)` referencia `Reserva(id)`, cascata DELETE
- `FK(equipamento_id)` referencia `Equipamento(id)`
- `UNIQUE(reserva_id, equipamento_id)` — evita duplicação

**Nota**: Usada apenas no contexto de retirada (RF15). Quando `Reserva.status` transiciona para `RETIRADO`, a lista de equipamentos específicos selecionados é armazenada aqui. Validação: todos devem ser do mesmo tipo da reserva e status=DISPONIVEL (RN10/RN24).

---

## Diagrama de Relacionamentos (ER simplificado)

```
┌─────────────────────┐
│  UnidadeEscolar     │
│  ─────────────────  │
│  id (PK)            │
│  nome               │
│  ativo              │
└──────────┬──────────┘
           │
      (1:N)│ (RN02, RN07, RN06)
           │
┌──────────▼──────────────────┐          ┌──────────────────────┐
│  Usuario                    │          │  Equipamento         │
│  ──────────────────────────  │          │  ────────────────────│
│  id (PK)                    │          │  id (PK)             │
│  nome, email, papel, senha  │          │  sigla, numeroPatr.  │
│  unidadeEscolar_id (FK,cond)│          │  tipo, marca, modelo │
│  ativo                      │          │  status              │
└──────────┬──────────────────┘          │  unidadeEscolar_id   │
           │                             └──────────┬───────────┘
      (1:N)│ (PROFESSOR)                            │ (1:N)
           │                                        │
           └────────────┬─────────────────────────────┘
                        │
                ┌───────▼────────┐
                │   Reserva      │
                │  ────────────  │
                │  id (PK)       │
                │  professor_id  │──> Usuario (PROFESSOR)
                │  tipo          │
                │  status        │
                │  auditoria     │
                │  ────────────  │
                │  usuarioAprovador_id ──> Usuario (ADM_PROATI)
                │  usuarioRetirante_id ──> Usuario (ADM_PROATI)
                │  usuarioDevolvendor_id ──> Usuario (ADM_PROATI)
                │  usuarioCancelador_id ──> Usuario (ADM_PROATI)
                └────────┬───────┘
                         │ (1:N)
                         │
                ┌────────▼────────────────┐
                │  ReservaEquipamento     │
                │  ────────────────────   │
                │  id (PK)                │
                │  reserva_id (FK)        │
                │  equipamento_id (FK)    │
                └─────────────────────────┘
```

---

## Índices e Performance

| Campo | Índice | Razão |
|-------|--------|-------|
| `Usuario.email` | UNIQUE | RN04, acesso no login |
| `Equipamento.numeroPatrimonio` | UNIQUE | RN09 |
| `Equipamento.sigla, unidadeEscolar_id` | UNIQUE | RN08 |
| `Reserva.professor_id` | Não-único | Consultar reservas do professor (RF18) |
| `Reserva.unidadeEscolar_id, status` | Não-único | Listar reservas por unidade e status (RF19) |
| `Reserva.dataHoraInicio, dataHoraFim` | Não-único | Verificar sobreposição de horários (RN15) |
| `Equipamento.unidadeEscolar_id, tipo` | Não-único | Consultar disponibilidade por tipo (RF11) |

---

## Soft Delete

`UnidadeEscolar.ativo` e `Usuario.ativo` implementam soft delete (RN03). Queries devem filtrar `WHERE ativo=true` por padrão; usar `@SQLDelete` e `@Where` do Hibernate ou filtro global em Service para não retornar registros deletados logicamente.

---

## Observações sobre Implementação

1. **Enums** (`Usuario.papel`, `Equipamento.status`, `Reserva.status`) mapeados como `@Enumerated(EnumType.STRING)` em JPA.
2. **Timestamps** (`criadoEm`, `atualizadoEm`) usando `@CreationTimestamp` e `@UpdateTimestamp` do Hibernate, ou `@PrePersist`/`@PreUpdate` manual.
3. **Validações de negócio** (RN13–RN24) implementadas em **camada Service**, não no banco, pois envolvem lógica complexa (ex: RN15 — somação de quantidades aprovadas com overlapping de horários).
4. **Cálculo de status `ATRASADA`** (RN22) é derivado: `SELECT * FROM Reserva WHERE status='RETIRADO' AND dataHoraFim < NOW()` — nunca persistir como status.
5. **Alerta de mudança de equipamento com reservas futuras** (RF20/RN23) implementado em Service na transição `Equipamento.status` para `MANUTENCAO` ou `INATIVO`.
