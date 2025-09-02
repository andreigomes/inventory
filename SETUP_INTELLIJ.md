# Sistema de Inventário - Configuração Local

## Pré-requisitos
- Java 17
- Docker Desktop
- IntelliJ IDEA
- Maven 3.8+

## Configuração do IntelliJ IDEA

### 1. Importar o Projeto
1. Abra o IntelliJ IDEA
2. Selecione "Open" ou "Import Project"
3. Navegue até a pasta `C:\Users\Andrei\workspace\inventory`
4. Selecione o arquivo `pom.xml` na raiz
5. Clique em "Open as Project"
6. Quando perguntado, selecione "Open as Maven Project"

### 2. Configurar Maven
1. Vá em `File > Settings`
2. Navegue para `Build, Execution, Deployment > Build Tools > Maven`
3. Verifique se o Maven home directory está configurado
4. Em "User settings file", marque "Override" e aponte para seu settings.xml se necessário

### 3. Configurar SDK do Projeto
1. Vá em `File > Project Structure`
2. Em "Project", configure:
   - Project SDK: Java 17
   - Project language level: 17
3. Em "Modules", verifique se todos os módulos estão sendo reconhecidos

### 4. Configurar Run Configurations
O IntelliJ deve detectar automaticamente as aplicações Spring Boot em cada módulo.

## Iniciando o Ambiente Local

### 1. Iniciar Infraestrutura
```bash
# Na pasta raiz do projeto
docker-compose -f docker-compose-local.yml up -d
```

### 2. Aguardar Serviços
Aguarde todos os serviços estarem prontos (pode levar 2-3 minutos).

### 3. Executar Migrações do Banco
```bash
mvn flyway:migrate
```

### 4. Inserir Dados de Teste
```bash
# Windows
insert-test-data.bat

# Linux/Mac
./insert-test-data.sh
```

### 5. Iniciar Microserviços
No IntelliJ, execute na seguinte ordem:
1. `InventoryServiceApplication`
2. `StoreServiceApplication` 
3. `NotificationServiceApplication`
4. `ObservabilityApplication`
5. `ApiGatewayApplication`

## URLs dos Serviços

- **API Gateway**: http://localhost:8088
- **Inventory Service**: http://localhost:8080
- **Store Service**: http://localhost:8081
- **Notification Service**: http://localhost:8082
- **Observability**: http://localhost:8083
- **Kafka UI**: http://localhost:8090
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Jaeger**: http://localhost:16686

## Testando a API

Use a API Key: `enterprise-api-key-2023` no header `X-API-Key`

### Exemplos de Endpoints:
```bash
# Listar lojas
curl -H "X-API-Key: enterprise-api-key-2023" http://localhost:8088/api/v1/stores

# Listar produtos
curl -H "X-API-Key: enterprise-api-key-2023" http://localhost:8088/api/v1/inventory/products

# Verificar estoque
curl -H "X-API-Key: enterprise-api-key-2023" http://localhost:8088/api/v1/inventory/stock
```

## Troubleshooting

### IntelliJ não reconhece Maven
1. Delete a pasta `.idea` do projeto
2. Reimporte o projeto selecionando o `pom.xml`
3. Execute `File > Reload Maven Projects`
4. Execute `File > Invalidate Caches and Restart`

### Erro de SDK
1. Vá em `File > Project Structure > SDKs`
2. Adicione o Java 17 se não estiver listado
3. Configure o Project SDK para Java 17

### Problemas com Banco de Dados
1. Verifique se o Docker está rodando
2. Execute: `docker-compose -f docker-compose-local.yml logs postgres`
3. Reinicie os containers se necessário
