# API Specification — Sistema PROATI

## Autenticação

Todos os endpoints (exceto login) exigem um Bearer JWT no header `Authorization`.

Exemplo: `Authorization: Bearer <token_jwt>`

JWT contém `sub` (email do usuário), `iat`, `exp`, e custom claim `papel` (papel do usuário).

---

## Endpoints

### 1. Autenticação (RF01)

| Método | Path | Papel | Descrição | RF |
|--------|------|-------|-----------|-----|
| POST | `/api/auth/login` | — | Login com email e senha; retorna JWT | RF01 |

**Request**:
```json
{
  "email": "string",
  "senha": "string"
}
```

**Response** (200):
```json
{
  "token": "string (JWT)",
  "usuario": {
    "id": "long",
    "nome": "string",
    "email": "string",
    "papel": "string (ADM_SUPREMO|ADM_PROATI|PROFESSOR)",
    "unidadeEscolar": {
      "id": "long",
      "nome": "string"
    }
  }
}
```

---

### 2. Unidades Escolares (ADM_SUPREMO, RF03–RF06)

| Método | Path | Papel | Descrição | RF |
|--------|------|-------|-----------|-----|
| POST | `/api/unidades` | ADM_SUPREMO | Criar unidade escolar | RF03 |
| GET | `/api/unidades` | ADM_SUPREMO | Listar todas as unidades (com indicadores) | RF06 |
| GET | `/api/unidades/{id}` | ADM_SUPREMO | Detalhe de uma unidade | — |
| PUT | `/api/unidades/{id}` | ADM_SUPREMO | Editar unidade | RF04 |
| DELETE | `/api/unidades/{id}` | ADM_SUPREMO | Inativar unidade (soft delete, RN03) | RF04 |

**POST /api/unidades Request**:
```json
{
  "nome": "string"
}
```

**PUT /api/unidades/{id} Request**:
```json
{
  "nome": "string"
}
```

---

### 3. Usuários — PROATI (ADM_SUPREMO, RF05)

| Método | Path | Papel | Descrição | RF |
|--------|------|-------|-----------|-----|
| POST | `/api/usuarios/proati` | ADM_SUPREMO | Cadastrar ADM_PROATI vinculado a unidade (respeita RN01: 1–2 por unidade) | RF05 |
| GET | `/api/unidades/{id}/proatis` | ADM_SUPREMO | Listar PROATI de uma unidade | — |
| PUT | `/api/usuarios/{id}` | ADM_SUPREMO | Editar PROATI | — |
| DELETE | `/api/usuarios/{id}` | ADM_SUPREMO | Inativar PROATI | — |

**POST /api/usuarios/proati Request**:
```json
{
  "nome": "string",
  "email": "string",
  "senha": "string",
  "unidadeEscolarId": "long"
}
```

---

### 4. Professores (ADM_PROATI, RF07–RF08)

| Método | Path | Papel | Descrição | RF |
|--------|------|-------|-----------|-----|
| POST | `/api/professores` | ADM_PROATI | Cadastrar professor na própria unidade (RN06) | RF07 |
| GET | `/api/professores` | ADM_PROATI | Listar professores da própria unidade | — |
| GET | `/api/professores/{id}` | ADM_PROATI | Detalhe de um professor | — |
| PUT | `/api/professores/{id}` | ADM_PROATI | Editar professor | RF08 |
| DELETE | `/api/professores/{id}` | ADM_PROATI | Inativar professor | RF08 |

**POST /api/professores Request**:
```json
{
  "nome": "string",
  "email": "string",
  "senha": "string"
}
```

---

### 5. Equipamentos (ADM_PROATI, RF09–RF11, RF20)

| Método | Path | Papel | Descrição | RF |
|--------|------|-------|-----------|-----|
| POST | `/api/equipamentos` | ADM_PROATI | Cadastrar equipamento na própria unidade | RF09 |
| GET | `/api/equipamentos` | ADM_PROATI, PROFESSOR | Listar equipamentos da própria unidade; PROFESSOR vê só disponíveis por tipo (RN10) | — |
| GET | `/api/equipamentos/disponiveis?tipo=X` | PROFESSOR | Consultar qtd disponível por tipo (RN10, RN08) | RF11 |
| GET | `/api/equipamentos/{id}` | ADM_PROATI | Detalhe de um equipamento | — |
| PUT | `/api/equipamentos/{id}` | ADM_PROATI | Editar equipamento (marca, modelo); RN11: só PROATI da unidade proprietária | RF10 |
| PATCH | `/api/equipamentos/{id}/status` | ADM_PROATI | Mudar status (DISPONIVEL → MANUTENCAO/INATIVO); dispara alerta se RN23 (RF20) | RF10 |
| DELETE | `/api/equipamentos/{id}` | ADM_PROATI | Dar baixa (soft delete); RN11 | RF10 |

**POST /api/equipamentos Request**:
```json
{
  "sigla": "string",
  "numeroPatrimonio": "string",
  "tipo": "string",
  "marca": "string",
  "modelo": "string"
}
```

**PATCH /api/equipamentos/{id}/status Request**:
```json
{
  "status": "DISPONIVEL|MANUTENCAO|INATIVO"
}
```

**GET /api/equipamentos/disponiveis Response**:
```json
[
  {
    "tipo": "string",
    "quantidade": "integer"
  }
]
```

---

### 6. Reservas (RF12–RF20)

#### 6a. PROFESSOR: Solicitar e Acompanhar

| Método | Path | Papel | Descrição | RF |
|--------|------|-------|-----------|-----|
| POST | `/api/reservas` | PROFESSOR | Solicitar reserva (tipo, qtd, horário); cria com status=PENDENTE (RN12, RN13) | RF12 |
| GET | `/api/reservas/minhas` | PROFESSOR | Listar próprias reservas (acompanhar status, RF18) | RF18 |
| PATCH | `/api/reservas/{id}/cancelar` | PROFESSOR | Cancelar reserva própria; só até horário início, status PENDENTE/APROVADA (RN19) | RF17 |

**POST /api/reservas Request**:
```json
{
  "tipoEquipamento": "string",
  "quantidade": "integer",
  "dataHoraInicio": "2024-12-20T10:00:00",
  "dataHoraFim": "2024-12-20T12:00:00"
}
```

**Response** (201):
```json
{
  "id": "long",
  "professor": { "id": "long", "nome": "string", "email": "string" },
  "tipoEquipamento": "string",
  "quantidade": "integer",
  "dataHoraInicio": "string (ISO 8601)",
  "dataHoraFim": "string (ISO 8601)",
  "status": "PENDENTE",
  "criadoEm": "string (ISO 8601)"
}
```

---

#### 6b. ADM_PROATI: Analisar e Processar

| Método | Path | Papel | Descrição | RF |
|--------|------|-------|-----------|-----|
| GET | `/api/reservas` | ADM_PROATI | Listar reservas da própria unidade, filtráveis por status/professor (RF19) | RF19 |
| GET | `/api/reservas/{id}` | ADM_PROATI | Detalhe completo de uma reserva | — |
| PATCH | `/api/reservas/{id}/aprovar` | ADM_PROATI | Aprovar reserva; valida RN15 (capacidade) | RF13 |
| PATCH | `/api/reservas/{id}/recusar` | ADM_PROATI | Recusar reserva; exige motivoRecusa (RN16) | RF14 |
| PATCH | `/api/reservas/{id}/retirada` | ADM_PROATI | Registrar retirada; seleciona equipamentos específicos (RF15, RN24); só após APROVADA (RN17) | RF15 |
| PATCH | `/api/reservas/{id}/devolucao` | ADM_PROATI | Registrar devolução; só após RETIRADO (RN18) | RF16 |
| PATCH | `/api/reservas/{id}/cancelar` | ADM_PROATI | Cancelar reserva; qualquer momento antes retirada (RN20) | RF17 |
| GET | `/api/reservas/atrasadas` | ADM_PROATI | Listar reservas atrasadas (status=RETIRADO, dataHoraFim < NOW; RN22) | RF20 |

**PATCH /api/reservas/{id}/aprovar Request**:
```json
{}
```
*Valida RN15: soma de quantidades já APROVADAS do mesmo tipo, no mesmo intervalo horário, não deve ultrapassar total de equipamentos DISPONIVEL.*

**PATCH /api/reservas/{id}/recusar Request**:
```json
{
  "motivo": "string (obrigatório, RN16)"
}
```

**PATCH /api/reservas/{id}/retirada Request**:
```json
{
  "equipamentoIds": [long, long, ...]
}
```
*Valida RN24: qtd de equipamentos = qtd aprovada, todos do mesmo tipo, todos status=DISPONIVEL.*

**PATCH /api/reservas/{id}/devolucao Request**:
```json
{}
```

**PATCH /api/reservas/{id}/cancelar Request**:
```json
{}
```

---

#### 6c. Relatórios e Histórico

| Método | Path | Papel | Descrição | RF |
|--------|------|-------|-----------|-----|
| GET | `/api/relatorios/equipamentos-mais-usados` | ADM_PROATI, ADM_SUPREMO | Equipamentos (tipo) mais frequentes em reservas concluídas (DEVOLVIDO) | RF19 (RF23) |
| GET | `/api/relatorios/atrasos` | ADM_PROATI, ADM_SUPREMO | Histórico de devoluções atrasadas (RN22) | RF19 (RF23) |
| GET | `/api/equipamentos/{id}/historico` | ADM_PROATI | Histórico de reservas envolvendo um equipamento específico | RF19 |
| GET | `/api/professores/{id}/historico` | ADM_PROATI, PROFESSOR | Histórico de reservas de um professor | RF19 |

---

## Códigos HTTP

| Código | Caso de Uso |
|--------|-----------|
| 200 | Requisição bem-sucedida (GET, PUT, PATCH sem mudança de dados) |
| 201 | Recurso criado (POST) |
| 204 | Ação bem-sucedida sem corpo de resposta (PUT/PATCH/DELETE) |
| 400 | Validação falhou (dados inválidos, RN violada, ex: RN13, RN15) |
| 401 | JWT inválido ou expirado |
| 403 | Usuário não autorizado para a ação (papel insuficiente, ex: PROFESSOR tentando editar unidade) |
| 404 | Recurso não encontrado |
| 500 | Erro interno do servidor |

---

## Autorizações por Papel

| RF | ADM_SUPREMO | ADM_PROATI | PROFESSOR |
|----|-------------|-----------|-----------|
| RF01 (login) | ✓ | ✓ | ✓ |
| RF03/RF04/RF06 (unidades) | ✓ | — | — |
| RF05 (cadastrar PROATI) | ✓ | — | — |
| RF07/RF08 (professores) | — | ✓ (própria unidade) | — |
| RF09/RF10/RF20 (equipamentos) | — | ✓ (própria unidade) | — |
| RF11 (consultar disponibilidade) | — | — | ✓ |
| RF12 (solicitar reserva) | — | — | ✓ |
| RF13/RF14 (aprovar/recusar) | — | ✓ | — |
| RF15/RF16 (retirada/devolução) | — | ✓ | — |
| RF17 (cancelar) | — | ✓ | ✓ (própria, RN19/RN20) |
| RF18 (acompanhar) | — | — | ✓ |
| RF19 (histórico) | — | ✓ | — |

---

## DTOs Típicos

### Erros (Global Error Handler)

```json
{
  "timestamp": "2024-12-20T10:30:00Z",
  "status": 400,
  "error": "Validação falhou",
  "message": "string",
  "details": [
    "campo: mensagem de erro"
  ]
}
```

### Usuario DTO (Response)

```json
{
  "id": "long",
  "nome": "string",
  "email": "string",
  "papel": "ADM_SUPREMO|ADM_PROATI|PROFESSOR",
  "unidadeEscolar": {
    "id": "long",
    "nome": "string"
  },
  "ativo": "boolean"
}
```

---

## Notas

1. Endpoints não listados (GET detalhes, PUT editar básico) seguem convenções RESTful padrão.
2. Filtros (query params) para listar: `?status=PENDENTE&professor_id=123&sort=dataHoraInicio` — implementar suporte flexível em Service/Repository.
3. Paginação: endpoints que retornam listas devem suportar `?page=0&size=20&sort=descending`.
4. Auditoria (RNF07): cada transição (aprovar, recusar, retirar, devolver, cancelar) armazena usuário responsável + timestamp na entidade `Reserva`.
