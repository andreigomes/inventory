# Configuração Local - Sistema de Inventário

## 🚀 Guia Rápido de Setup

### Pré-requisitos
- Java 17 instalado
- Docker Desktop executando
- IntelliJ IDEA 2023+ 
- Maven 3.8+

## 📋 Passo a Passo

### 1. Configurar IntelliJ IDEA

#### Problema: IntelliJ não reconhece como projeto Maven
**Solução:**

1. **Feche o IntelliJ** se estiver aberto
2. **Delete a pasta `.idea`** no diretório `inventory` (se existir)
3. **Abra o IntelliJ IDEA**
4. **Selecione "Open"** (não "Import Project")
5. **Navegue até** `C:\Users\Andrei\workspace\inventory`
6. **Selecione o arquivo `pom.xml`** na raiz
7. **Clique "Open as Project"**
8. **Quando perguntado, escolha "Open as Maven Project"**

#### Configurações Adicionais:
```
File > Settings > Build, Execution, Deployment > Build Tools > Maven
- Maven home directory: (deixe auto-detect ou configure manualmente)
- User settings file: Default ou configure seu settings.xml

File > Project Structure > Project
- Project SDK: Java 17
- Project language level: 17 - Sealed types, always-strict floating-point semantics
```

### 2. Inicializar Ambiente

#### 2.1 Subir Infraestrutura
```bash
# Na pasta raiz do projeto
cd C:\Users\Andrei\workspace\inventory
docker-compose -f docker-compose-local.yml up -d
```

#### 2.2 Aguardar Serviços (2-3 minutos)
```bash
# Verificar se PostgreSQL está pronto
docker exec inventory-postgres pg_isready -U inventory_user -d inventory_db
```

#### 2.3 Executar Migrações do Banco
```bash
# Migração para todos os serviços
mvn flyway:migrate

# Ou individual por serviço:
cd inventory-service && mvn flyway:migrate
cd ../store-service && mvn flyway:migrate  
cd ../notification-service && mvn flyway:migrate
```

#### 2.4 Inserir Dados de Teste
```bash
# Windows
insert-test-data.bat

# Linux/Mac  
chmod +x insert-test-data.sh
./insert-test-data.sh
```

### 3. Executar Microserviços no IntelliJ

**Ordem recomendada:**

1. **Shared Module** (compilar apenas): `mvn clean install`
2. **Inventory Service** → `InventoryServiceApplication.java`
3. **Store Service** → `StoreServiceApplication.java`
4. **Notification Service** → `NotificationServiceApplication.java`
5. **Observability Service** → `ObservabilityApplication.java`
6. **API Gateway** → `ApiGatewayApplication.java`

### 4. Configurações das Run Configurations

Para cada microserviço, configure:
```
Main class: com.enterprise.{service}.{Service}Application
VM options: -Dspring.profiles.active=dev
Environment variables: 
  SPRING_PROFILES_ACTIVE=dev
  JAVA_TOOL_OPTIONS=-XX:+UseG1GC -Xmx512m
```

## 🌐 URLs dos Serviços

| Serviço | URL | Porta |
|---------|-----|-------|
| API Gateway | http://localhost:8088 | 8088 |
| Inventory Service | http://localhost:8080 | 8080 |
| Store Service | http://localhost:8081 | 8081 |
| Notification Service | http://localhost:8082 | 8082 |
| Observability | http://localhost:8083 | 8083 |
| Kafka UI | http://localhost:8090 | 8090 |
| Prometheus | http://localhost:9090 | 9090 |
| Grafana | http://localhost:3000 | 3000 |
| Jaeger | http://localhost:16686 | 16686 |

## 🧪 Testando a API

### Headers obrigatórios:
```
X-API-Key: enterprise-api-key-2023
Content-Type: application/json
```

### Exemplos de testes:
```bash
# 1. Listar todas as lojas
curl -H "X-API-Key: enterprise-api-key-2023" \
     http://localhost:8088/api/v1/stores

# 2. Buscar produtos
curl -H "X-API-Key: enterprise-api-key-2023" \
     http://localhost:8088/api/v1/inventory/products

# 3. Verificar estoque por loja
curl -H "X-API-Key: enterprise-api-key-2023" \
     http://localhost:8088/api/v1/inventory/stock

# 4. Alertas ativos
curl -H "X-API-Key: enterprise-api-key-2023" \
     http://localhost:8088/api/v1/notifications/alerts
```

## 🛠️ Troubleshooting

### IntelliJ não reconhece módulos Maven:
```bash
1. File > Invalidate Caches and Restart
2. Delete .idea folder e reimporte
3. Maven > Reload Projects
4. File > Project Structure > Modules > Verificar módulos
```

### Erro de dependências:
```bash
mvn clean install -U
```

### Problemas com banco de dados:
```bash
# Verificar logs
docker-compose -f docker-compose-local.yml logs postgres

# Reiniciar containers
docker-compose -f docker-compose-local.yml restart
```

### Porta já em uso:
```bash
# Windows - verificar portas
netstat -ano | findstr :8080

# Matar processo
taskkill /PID {PID} /F
```

## 📊 Dados de Teste Inseridos

- **5 Lojas** (Centro, Ibirapuera, Paulista, Vila Madalena, Morumbi)
- **5 Categorias** (Eletrônicos, Roupas, Casa e Jardim, Esportes, Livros)
- **5 Produtos** (Galaxy S23, Dell Inspiron, Camiseta Polo, Tênis Nike, Livro Clean Code)
- **11 Registros de Inventário** (distribuídos pelas lojas)
- **5 Movimentações de Estoque** (entradas, saídas, ajustes)
- **4 Fornecedores** (Samsung, Dell, Nike, Pearson)
- **3 Alertas** (estoque baixo e manutenção)

## 🎯 Próximos Passos

1. Testar endpoints via Postman/curl
2. Verificar métricas no Grafana
3. Monitorar logs no Jaeger
4. Criar novos produtos via API
5. Simular alertas de estoque baixo
