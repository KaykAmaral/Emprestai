# Configuração do VS Code

A configuração `Emprestai Backend (dev)` em `launch.json` inicia o Spring Boot com o perfil `dev`.

Para executar:

1. Instale as extensões Extension Pack for Java e Spring Boot Extension Pack.
2. Abra a pasta raiz `Emprestai` no VS Code.
3. Abra **Run and Debug** (`Ctrl+Shift+D`).
4. Selecione **Emprestai Backend (dev)** e pressione `F5`.

As chaves locais ficam em `emprestai_backend/src/main/resources/application-dev.properties`,
que é ignorado pelo Git. Se esse arquivo não existir, copie
`application-dev.example.properties` para `application-dev.properties` e ajuste os valores.
