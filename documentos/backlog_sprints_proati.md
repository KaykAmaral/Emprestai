# Backlog & Sprints — Sistema PROATI

## Product Backlog (por épico)

### EP1 — Autenticação e Autorização
| ID | User Story | Prioridade |
|----|-------------|-----------|
| US01 | Como usuário, quero fazer login com email e senha, para acessar o sistema conforme meu papel | Alta |
| US02 | Como sistema, preciso restringir funcionalidades conforme o papel (ADM_SUPREMO / ADM_PROATI / PROFESSOR) | Alta |

### EP2 — Gestão de Unidades Escolares (ADM_SUPREMO)
| ID | User Story | Prioridade |
|----|-------------|-----------|
| US03 | Como ADM_SUPREMO, quero cadastrar uma unidade escolar | Alta |
| US04 | Como ADM_SUPREMO, quero editar/inativar uma unidade escolar | Média |
| US05 | Como ADM_SUPREMO, quero cadastrar um PROATI e vincular a uma unidade (respeitando 1–2 por unidade) | Alta |
| US06 | Como ADM_SUPREMO, quero visualizar todas as unidades e indicadores gerais | Média |

### EP3 — Gestão de Usuários (ADM_PROATI)
| ID | User Story | Prioridade |
|----|-------------|-----------|
| US07 | Como ADM_PROATI, quero cadastrar um professor na minha unidade | Alta |
| US08 | Como ADM_PROATI, quero editar/inativar um professor | Média |

### EP4 — Gestão de Equipamentos (ADM_PROATI)
| ID | User Story | Prioridade |
|----|-------------|-----------|
| US09 | Como ADM_PROATI, quero cadastrar um equipamento (identificação, patrimônio, tipo, marca/modelo) | Alta |
| US10 | Como ADM_PROATI, quero editar/dar baixa em um equipamento | Alta |
| US11 | Como ADM_PROATI, quero ser alertado se um equipamento com reservas futuras aprovadas mudar para manutenção/inativo | Média |

### EP5 — Reservas (núcleo do sistema)
| ID | User Story | Prioridade |
|----|-------------|-----------|
| US12 | Como PROFESSOR, quero consultar a quantidade disponível por tipo de equipamento, para saber quanto posso solicitar | Alta |
| US13 | Como PROFESSOR, quero solicitar reserva informando tipo de equipamento, quantidade e horário, sem escolher a unidade física | Alta |
| US14 | Como ADM_PROATI, quero aprovar ou recusar solicitações de reserva, respeitando a capacidade disponível do tipo solicitado | Alta |
| US15 | Como sistema, preciso impedir a aprovação de uma reserva se a soma das quantidades já aprovadas do tipo, no mesmo horário, ultrapassar o total de equipamentos ativos desse tipo | Alta |
| US16 | Como ADM_PROATI, quero selecionar quais equipamentos específicos entregar e registrar a retirada | Alta |
| US17 | Como ADM_PROATI, quero registrar a devolução do equipamento | Alta |
| US18 | Como PROFESSOR, quero cancelar minha reserva antes do horário de início | Média |
| US19 | Como ADM_PROATI, quero cancelar uma reserva a qualquer momento antes da retirada | Média |
| US20 | Como usuário, quero visualizar reservas atrasadas (não devolvidas após o horário previsto) | Média |

### EP6 — Consultas e Histórico
| ID | User Story | Prioridade |
|----|-------------|-----------|
| US21 | Como PROFESSOR, quero acompanhar o status das minhas solicitações | Média |
| US22 | Como ADM_PROATI, quero consultar histórico de reservas por equipamento ou professor | Média |
| US23 | Como ADM_PROATI/ADM_SUPREMO, quero relatórios simples (equipamentos mais usados, atrasos) | Baixa |

---

## Plano de Sprints (16 semanas, ciclos de 2 semanas)

| Sprint | Semanas | Foco | Entregas |
|--------|---------|------|----------|
| Sprint 0 | 1–2 | Setup & Design | Ambiente Spring Boot + MySQL + React, DER aplicado ao banco, protótipo de telas, repositório/CI |
| Sprint 1 | 3–4 | Fundação | US01, US02 (auth/JWT), US03, US05 (unidade + PROATI) |
| Sprint 2 | 5–6 | Gestão de acesso | US04, US06 (unidade), US07, US08 (professores) |
| Sprint 3 | 7–8 | Equipamentos | US09, US10, US11 |
| Sprint 4 | 9–10 | Reservas — solicitação | US12, US13, US14, US15 |
| Sprint 5 | 11–12 | Reservas — ciclo de vida | US16, US17, US18, US19, US20 |
| Sprint 6 | 13–14 | Consultas + Testes | US21, US22, US23, testes de integração, correção de bugs |
| Sprint 7 | 15–16 | Fechamento | Testes finais, ajustes de UX, documentação, apresentação, buffer |

---

## Observação sobre divisão do time (4+ pessoas)

Recomendo dividir por **fatia vertical de funcionalidade** (ex: uma dupla cuida de "Reservas" front+back inteiro) em vez de dividir por camada (ex: "um faz só backend, outro só frontend"). Isso evita gargalo de integração no fim de cada sprint e mantém todo mundo com visão ponta a ponta de pelo menos uma parte do sistema.
