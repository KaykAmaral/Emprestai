# Arquitetura — Sistema PROATI Backend

## Visão Geral

Backend estruturado em **camadas desacopladas** (Controller → Service → Repository → Database), seguindo boas práticas Spring Boot. Cada camada tem responsabilidade bem definida:

```
┌─────────────────────────────────────────────────────────┐
│  HTTP Request (REST Endpoint)                           │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  CONTROLLER LAYER (REST Adapter)                        │
│  ─────────────────────────────────────────────────────  │
│  • @RestController endpoints (@GetMapping, @PostMapping)│
│  • Request/Response DTOs (validação via @Valid)        │
│  • Autorização por papel (@PreAuthorize)               │
│  • Converte HTTP ↔ DTO                                 │
│  • Delega lógica para Service                          │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  SERVICE LAYER (Business Logic)                         │
│  ─────────────────────────────────────────────────────  │
│  • Regras de negócio (RN01–RN24, RF*)                  │
│  • Transações (@Transactional)                         │
│  • Orquestração de entidades                           │
│  • Validações complexas (ex: RN15 — sobreposição)      │
│  • Auditoria (RNF07 — usuário/timestamp de cada ação) │
│  • Acessa Repository para persistência                 │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  REPOSITORY LAYER (Data Access)                         │
│  ─────────────────────────────────────────────────────  │
│  • Spring Data JPA (@Repository extends JpaRepository) │
│  • Query methods (findBy*, custom @Query)              │
│  • Soft delete (@Where, @SQLDelete)                    │
│  • Índices e constraints definidos em entidades        │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│  DATABASE LAYER (MySQL)                                 │
│  ─────────────────────────────────────────────────────  │
│  • Entidades JPA (UnidadeEscolar, Usuario, etc.)      │
│  • Relacionamentos e constraints (FK, UNIQUE, etc.)    │
│  • Índices para performance                            │
│  • Schema definido em application.properties/yaml      │
└─────────────────────────────────────────────────────────┘
```

---

## Controller Layer

**Responsabilidades**:
- Expor endpoints REST
- Mapear requisições HTTP → DTOs
- Validar entrada via Bean Validation (`@Valid`, `@NotNull`, `@Min`, etc.)
- Autorizar acesso por papel (`@PreAuthorize("hasRole('ADM_PROATI')")`)
- Delegar lógica para Service
- Mapear respostas DTOs → HTTP 200/201/204/400/403/404/500

**Estrutura de Pacotes**:
```
src/main/java/com/example/emprestai/
└── controller/
    ├── AuthController.java       (RF01 — login)
    ├── UnidadeController.java    (RF03–RF06)
    ├── UsuarioController.java    (RF05)
    ├── ProfessorController.java  (RF07–RF08)
    ├── EquipamentoController.java (RF09–RF11, RF20)
    └── ReservaController.java    (RF12–RF20)
```

**Exemplo Pattern**:
```java
@RestController
@RequestMapping("/api/equipamentos")
@PreAuthorize("hasAnyRole('ADM_PROATI', 'PROFESSOR')")
public class EquipamentoController {
  @GetMapping("/disponiveis")
  @PreAuthorize("hasRole('PROFESSOR')")
  public ResponseEntity<List<DisponibilidadeDTO>> 
    consultar(@RequestParam String tipo) {
    // Chama service.obterDisponibilidade(tipo)
    // Retorna List<DisponibilidadeDTO>
  }
}
```

---

## Service Layer

**Responsabilidades**:
- Implementar regras de negócio (RN01–RN24, RF)
- Validar estado (ex: RN13 — data no futuro, RN15 — capacidade)
- Orquestrar transações (método marcado com `@Transactional`)
- Registrar auditoria (quem/quando fez cada ação — RNF07)
- Calcular status derivados (ex: RN22 — `ATRASADA`)
- Chamar Repository para persistência
- Lançar exceções customizadas para violações de RN

**Estrutura de Pacotes**:
```
src/main/java/com/example/emprestai/
└── service/
    ├── AuthService.java       (autenticação, geração JWT)
    ├── UnidadeService.java    (CRUD unidades)
    ├── UsuarioService.java    (CRUD usuários, hash senha)
    ├── EquipamentoService.java (CRUD equipamentos, RN10, alertas RF20)
    ├── ReservaService.java    (ciclo de vida: solicitar, aprovar, retirar, devolver, cancelar)
    └── RelatorioService.java  (consultas complexas — mais usado, equipamentos)
```

**Exemplo Pattern — Validação de Negócio**:
```java
@Service
@Transactional
public class ReservaService {
  
  public Reserva aprovar(Long reservaId, Usuario usuario) {
    Reserva r = repository.findById(reservaId)
      .orElseThrow(() -> new ResourceNotFoundException(...));
    
    // RN13: data no futuro
    if (r.getDataHoraInicio().isBefore(LocalDateTime.now())) {
      throw new BusinessRuleException("Data deve ser no futuro");
    }
    
    // RN15: validar capacidade disponível
    int aprovadas = repository.countAprovadasNoMesmoHorario(
      r.getTipoEquipamento(), 
      r.getUnidadeEscolar(),
      r.getDataHoraInicio(), 
      r.getDataHoraFim()
    );
    int disponivel = equipamentoService
      .obterDisponiveisPorTipo(r.getTipoEquipamento(), r.getUnidadeEscolar());
    
    if (aprovadas + r.getQuantidade() > disponivel) {
      throw new BusinessRuleException(
        "Capacidade insuficiente: " + disponivel + " disponível(eis), " + 
        aprovadas + " já aprovada(s), solicitado(s) " + r.getQuantidade()
      );
    }
    
    // Auditoria (RNF07)
    r.setStatus(ReservaStatus.APROVADA);
    r.setUsuarioAprovador(usuario);
    r.setDataHoraAprovacao(LocalDateTime.now());
    
    return repository.save(r);
  }
}
```

---

## Repository Layer

**Responsabilidades**:
- Definir queries de acesso (JPA methods + `@Query` customizado)
- Implementar soft delete (Hibernate `@Where`)
- Fornecer métodos de busca eficientes

**Estrutura de Pacotes**:
```
src/main/java/com/example/emprestai/
└── repository/
    ├── UnidadeRepository.java
    ├── UsuarioRepository.java
    ├── EquipamentoRepository.java
    ├── ReservaRepository.java
    └── ReservaEquipamentoRepository.java
```

**Exemplo Pattern — Queries Customizadas**:
```java
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
  
  // Encontrar reservas ativas de um professor
  List<Reserva> findByProfessorAndStatusIn(
    Usuario professor, 
    List<ReservaStatus> statuses
  );
  
  // Contar quantidade já aprovada num intervalo de tempo (RN15)
  @Query("""
    SELECT SUM(r.quantidade) 
    FROM Reserva r 
    WHERE r.tipoEquipamento = :tipo 
      AND r.unidadeEscolar = :unidade 
      AND r.status = 'APROVADA' 
      AND :inicio < r.dataHoraFim 
      AND :fim > r.dataHoraInicio
  """)
  Integer countAprovadasNoMesmoHorario(
    @Param("tipo") String tipo,
    @Param("unidade") UnidadeEscolar unidade,
    @Param("inicio") LocalDateTime inicio,
    @Param("fim") LocalDateTime fim
  );
  
  // Listar atrasadas (RN22)
  @Query("""
    SELECT r FROM Reserva r 
    WHERE r.status = 'RETIRADO' 
      AND r.dataHoraFim < CURRENT_TIMESTAMP
  """)
  List<Reserva> findAtrasadas();
}
```

---

## Segurança (RNF01)

### Autenticação

- **Login**: email + senha (RF01) → gera JWT com validade configurável (ex: 1 hora)
- **JWT Structure**: header `Authorization: Bearer <token>`
- **Claims**: `sub` (email), `papel` (role), `iat`, `exp`
- **Biblioteca**: JJWT 0.13.0 (via `pom.xml`)

### Autorização

- **Filter Chain**: `SecurityFilterChain` intercepta requisições, valida JWT
- **Role-based access**: `@PreAuthorize("hasRole('ADM_PROATI')")`ou `hasAnyRole(...)`
- **Restrição contextual**: Service também valida (ex: PROFESSOR só vê equipamentos da própria unidade)

**Exemplo**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf().disable() // API stateless
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/login").permitAll()
        .requestMatchers("/api/**").authenticated()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
      )
      .addFilterBefore(new JwtAuthenticationFilter(...), UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
  }
}
```

---

## DTOs (Data Transfer Objects)

**Separação Request/Response** evita expor entidades:

```
src/main/java/com/example/emprestai/
└── dto/
    ├── request/
    │   ├── LoginRequest.java
    │   ├── CriarUnidadeRequest.java
    │   ├── CriarEquipamentoRequest.java
    │   ├── SolicitarReservaRequest.java
    │   └── ...
    └── response/
        ├── LoginResponse.java
        ├── UsuarioDTO.java
        ├── UnidadeDTO.java
        ├── EquipamentoDTO.java
        ├── ReservaDTO.java
        └── ...
```

**Validação no DTO**:
```java
public class SolicitarReservaRequest {
  @NotNull @NotBlank
  private String tipoEquipamento;
  
  @NotNull @Min(1)
  private Integer quantidade;
  
  @NotNull @FutureOrPresent(message = "Data deve ser no futuro")
  private LocalDateTime dataHoraInicio;
  
  @NotNull
  private LocalDateTime dataHoraFim;
}
```

---

## Tratamento de Erros (Global Exception Handler)

**Centralizado** em `@ControllerAdvice`:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
  
  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ErrorResponse> handleBusinessRule(
    BusinessRuleException ex) {
    return ResponseEntity.status(400).body(
      new ErrorResponse(400, "Validação de negócio falhou", ex.getMessage())
    );
  }
  
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
    MethodArgumentNotValidException ex) {
    // Retorna detalhes dos campos inválidos
    return ResponseEntity.status(400).body(...);
  }
  
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(...) {
    return ResponseEntity.status(403).body(...);
  }
}
```

---

## Auditoria (RNF07)

Cada transição de status em `Reserva` registra:
- **Quem**: usuário responsável (`usuarioAprovador_id`, `usuarioRecusador_id`, etc.)
- **Quando**: timestamp (`dataHoraAprovacao`, `dataHoraRecusa`, etc.)

Implementado em **Service**, populando os campos antes de `repository.save()`.

**Alternativa (não recomendada aqui)**: auditar via Hibernate Envers (mais complexo; escopo atual é simples).

---

## Transações (RNF05)

- Métodos em Service marcados com `@Transactional` (padrão: REQUIRED, isolamento READ_COMMITTED)
- Operações complexas (ex: aprovar reserva → validar → atualizar status → registrar auditoria) são **atomares**
- Rollback automático se lançar `RuntimeException` (BusinessRuleException extends RuntimeException)

---

## Configuração e Variáveis de Ambiente

**`application.properties` ou `application.yml`**:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/proati_db
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=validate

# JWT
jwt.secret=sua-chave-secreta-muito-longa-de-pelo-menos-256-bits
jwt.expiration=3600000 # 1 hora em ms

# Swagger/OpenAPI
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
```

---

## Portabilidade — Docker (RNF06)

**Dockerfile** (raiz do projeto backend):
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml** (orquestração com MySQL):
```yaml
version: '3.8'
services:
  db:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: proati_db
    ports:
      - "3306:3306"
  
  api:
    build: .
    depends_on:
      - db
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/proati_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
    ports:
      - "8080:8080"
```

---

## Padrões e Boas Práticas

1. **Dependency Injection**: usar constructor injection (`@Autowired` ou `private final` com `@RequiredArgsConstructor`)
2. **Immutabilidade de DTOs**: usar records (Java 17+) ou `@Value` (Lombok)
3. **Logging**: usar SLF4J + Logback (já incluídos no Spring Boot)
4. **Testes**:
   - **Unitários**: mock Repository, testar lógica Service isolada
   - **Integração**: `@SpringBootTest`, banco H2 ou testcontainers, testar Controller → Service → Repository
5. **Versionamento de API**: opcional; se usar, prefix `/api/v1/...`
6. **Documentação**: springdoc-openapi gera Swagger automaticamente dos `@Operation`, `@Parameter`

---

## Próximos Passos (Implementação)

1. **Sprint 1**: Entidades JPA + Repository + AuthService + Controllers auth/unidade/PROATI
2. **Sprint 2**: Controllers professor/equipamento, validações iniciais
3. **Sprint 3–5**: ReservaService (ciclo de vida, RN15 complexo), ReservaController
4. **Sprint 6–7**: Testes integração, relatórios, ajustes, apresentação

Ver `backlog_sprints_proati.md` para detalhes.
