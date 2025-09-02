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

#### 2.1 Subir Infraestrutura (PostgreSQL, Redis, Kafka)
```bash
# Na pasta raiz do projeto
cd C:\Users\Andrei\workspace\inventory
docker-compose -f docker-compose-local.yml up -d
```

#### 2.2 Aguardar Serviços Estarem Prontos (2-3 minutos)
```bash
# Verificar se PostgreSQL está pronto
docker exec inventory-postgres pg_isready -U inventory_user -d inventory_db

# Verificar se Redis está pronto
docker exec inventory-redis redis-cli ping

# Verificar logs do Kafka (opcional)
docker logs inventory-kafka
```

### 3. Executar os Microsserviços

#### 3.1 Compilar o Projeto
```bash
# Na pasta raiz do projeto
mvn clean install -DskipTests
```

#### 3.2 Iniciar os Serviços (via IntelliJ - Recomendado)

**No IntelliJ IDEA:**
1. Localize as classes `*Application.java` de cada serviço:
   - `inventory-service/src/main/java/.../InventoryServiceApplication.java`
   - `store-service/src/main/java/.../StoreServiceApplication.java`
   - `notification-service/src/main/java/.../NotificationServiceApplication.java`
   - `api-gateway/src/main/java/.../ApiGatewayApplication.java`

2. **Clique com botão direito** em cada classe → **"Run"**
3. **As migrações Flyway executarão automaticamente** durante o startup
4. Aguarde cada serviço inicializar completamente

#### 3.3 Ou Executar via Terminal (Alternativo)
```bash
# Terminal 1 - Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 2 - Store Service  
cd store-service
mvn spring-boot:run

# Terminal 3 - Notification Service
cd notification-service
mvn spring-boot:run

# Terminal 4 - API Gateway
cd api-gateway
mvn spring-boot:run
```

### 4. Verificar se Está Funcionando

#### 4.1 Health Checks
```bash
# Inventory Service
curl http://localhost:8080/actuator/health

# Store Service
curl http://localhost:8081/actuator/health

# Notification Service  
curl http://localhost:8082/actuator/health

# API Gateway
curl http://localhost:8000/actuator/health
```

#### 4.2 Verificar Banco de Dados
```bash
# Conectar ao PostgreSQL e verificar se as tabelas foram criadas
docker exec -it inventory-postgres psql -U inventory_user -d inventory_db

# No PostgreSQL, verificar se as migrações Flyway funcionaram:
\dt  # Listar tabelas (deve mostrar inventory_items, stock_reservations, etc.)
\q   # Sair
```

#### 4.3 APIs Disponíveis
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Gateway**: http://localhost:8000
- **Inventory Service**: http://localhost:8080
- **Store Service**: http://localhost:8081
- **Notification Service**: http://localhost:8082

### 5. Dados de Teste (Opcional)

```bash
# Inserir dados de teste no banco
# Windows:
insert-test-data.bat

# Linux/Mac:
chmod +x insert-test-data.sh
./insert-test-data.sh
```

## 🔧 Troubleshooting

### Problema: Flyway migration error
**Causa**: Banco não inicializou completamente
**Solução**: 
1. Aguardar PostgreSQL estar 100% pronto
2. Reiniciar a aplicação - as migrações executarão automaticamente

### Problema: Port already in use
**Solução**: 
```bash
# Verificar o que está usando a porta
netstat -ano | findstr :8080
# Terminar o processo ou usar outra porta
```

### Problema: Docker containers não sobem
**Solução**:
```bash
# Limpar containers antigos
docker-compose -f docker-compose-local.yml down --volumes
docker system prune -f

# Subir novamente
docker-compose -f docker-compose-local.yml up -d
```

## ✅ Pronto!

Agora você tem um ambiente completo de microsserviços rodando localmente com:
- ✅ PostgreSQL com bancos separados por serviço
- ✅ Redis para cache e rate limiting  
- ✅ Kafka para eventos entre serviços
- ✅ **Migrações Flyway executando automaticamente**
- ✅ Observabilidade com Prometheus/Grafana
- ✅ Documentação API via Swagger

**Importante**: As migrações do banco são **automáticas** - não precisa executar Flyway manualmente! 🚀
