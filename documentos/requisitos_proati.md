# Especificação de Requisitos — Sistema PROATI

## 1. Atores

| Ator | Descrição |
|------|-----------|
| ADM_SUPREMO | Gerencia unidades escolares e define os PROATIs responsáveis por cada uma |
| ADM_PROATI | Gerencia equipamentos, professores e reservas dentro da sua unidade escolar |
| PROFESSOR | Consulta equipamentos e solicita reservas dentro da sua unidade escolar |

---

## 2. Requisitos Funcionais (RF)

| ID | Descrição | Ator |
|----|-----------|------|
| RF01 | O sistema deve autenticar o usuário por email e senha | Todos |
| RF02 | O sistema deve restringir o acesso às funcionalidades conforme o papel do usuário | Sistema |
| RF03 | Cadastrar unidade escolar | ADM_SUPREMO |
| RF04 | Editar/inativar unidade escolar | ADM_SUPREMO |
| RF05 | Cadastrar PROATI e vincular a uma unidade escolar | ADM_SUPREMO |
| RF06 | Visualizar dashboard geral com todas as unidades escolares | ADM_SUPREMO |
| RF07 | Cadastrar professor | ADM_PROATI |
| RF08 | Editar/inativar professor | ADM_PROATI |
| RF09 | Cadastrar equipamento | ADM_PROATI |
| RF10 | Editar/dar baixa em equipamento | ADM_PROATI |
| RF11 | Consultar quantidade disponível por tipo de equipamento na unidade | PROFESSOR |
| RF12 | Solicitar reserva informando tipo de equipamento, quantidade e horário | PROFESSOR |
| RF13 | Aprovar solicitação de reserva, respeitando a capacidade disponível do tipo | ADM_PROATI |
| RF14 | Recusar solicitação de reserva, com motivo obrigatório | ADM_PROATI |
| RF15 | Selecionar os equipamentos específicos do tipo solicitado e registrar a retirada física | ADM_PROATI |
| RF16 | Registrar a devolução do equipamento | ADM_PROATI |
| RF17 | Cancelar reserva | PROFESSOR / ADM_PROATI |
| RF18 | Acompanhar o status das próprias solicitações de reserva | PROFESSOR |
| RF19 | Consultar histórico de reservas por equipamento ou professor | ADM_PROATI |
| RF20 | Alertar o PROATI quando um equipamento com reservas futuras aprovadas mudar para manutenção/inativo | Sistema |

---

## 3. Requisitos Não Funcionais (RNF)

> Esses ainda não tinham sido discutidos explicitamente — são uma proposta inicial com base na stack escolhida. Ajuste o que fizer sentido.

| ID | Categoria | Descrição |
|----|-----------|-----------|
| RNF01 | Segurança | Autenticação via JWT; senhas armazenadas com hash (BCrypt); autorização por papel validada em todos os endpoints |
| RNF02 | Usabilidade | Interface responsiva, utilizável em desktop e tablet |
| RNF03 | Compatibilidade | Suporte aos navegadores modernos mais usados (Chrome, Firefox, Edge — versões recentes) |
| RNF04 | Desempenho | Requisições de CRUD devem responder em até 2 segundos sob uso normal |
| RNF05 | Manutenibilidade | Backend estruturado em camadas (Controller / Service / Repository), seguindo boas práticas do Spring |
| RNF06 | Portabilidade | Backend executável via Docker, facilitando deploy e apresentação |
| RNF07 | Auditoria | Toda ação sobre reservas (aprovação, recusa, retirada, devolução, cancelamento) deve registrar usuário responsável e timestamp |
| RNF08 | Integridade | Restrições de integridade referencial e unicidade garantidas em nível de banco de dados sempre que possível |

---

## 4. Regras de Negócio (RN)

### Unidade Escolar / Usuários
| ID | Regra |
|----|-------|
| RN01 | Toda unidade escolar deve ter entre 1 e 2 usuários ADM_PROATI ativos |
| RN02 | Um ADM_PROATI não pode ser responsável por mais de uma unidade escolar |
| RN03 | Exclusão de unidade escolar é lógica (soft delete), preservando histórico vinculado |
| RN04 | Email de usuário deve ser único no sistema |
| RN05 | Senha deve ser armazenada apenas como hash, nunca em texto plano |
| RN06 | PROATI só pode cadastrar professores para a própria unidade |
| RN07 | Professor pertence a exatamente uma unidade escolar |

### Equipamento
| ID | Regra |
|----|-------|
| RN08 | Identificação (sigla) do equipamento deve ser única dentro da mesma unidade escolar |
| RN09 | Número de patrimônio do equipamento deve ser único em todo o sistema |
| RN10 | Apenas equipamentos com status DISPONIVEL contam para a quantidade disponível de um tipo, e apenas esses podem ser selecionados na retirada |
| RN11 | Apenas o PROATI da unidade proprietária pode editar ou dar baixa no equipamento |

### Reserva
| ID | Regra |
|----|-------|
| RN12 | Professor só pode solicitar reserva de tipos de equipamento existentes na própria unidade escolar |
| RN13 | Data/horário da reserva deve ser no futuro; hora_fim deve ser maior que hora_inicio |
| RN14 | Múltiplas solicitações do mesmo tipo podem coexistir em PENDENTE, independente da soma das quantidades pedidas |
| RN15 | O sistema não permite aprovar uma reserva se a soma das quantidades já APROVADAS do mesmo tipo, no mesmo intervalo de horário, ultrapassar o total de equipamentos ativos (DISPONIVEL) desse tipo na unidade |
| RN16 | Recusa de reserva exige motivo obrigatório |
| RN17 | Retirada só pode ser registrada a partir do status APROVADA |
| RN18 | Devolução só pode ser registrada a partir do status RETIRADO |
| RN19 | Professor pode cancelar a reserva somente até o horário de início, enquanto PENDENTE ou APROVADA |
| RN20 | PROATI pode cancelar a reserva a qualquer momento antes da retirada |
| RN21 | Reserva com status RETIRADO não pode mais ser cancelada, apenas devolvida |
| RN22 | Reserva com horário de término expirado e sem devolução é exibida como ATRASADA (status calculado, não persistido) |
| RN23 | Se um equipamento mudar para MANUTENCAO/INATIVO e isso fizer a soma de quantidades já APROVADAS de um tipo, em algum horário futuro, ultrapassar o novo total de ativos desse tipo, o sistema alerta o PROATI — sem cancelamento automático |
| RN24 | Na retirada, a quantidade de equipamentos selecionados deve ser igual à quantidade aprovada na reserva, e todos devem ser do tipo solicitado |
