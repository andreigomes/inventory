@echo off
REM Script para inserir dados de teste no sistema de inventário
REM Execute este script após iniciar o docker-compose-local.yml

echo 🚀 Iniciando inserção de dados de teste...

REM Aguarda o PostgreSQL estar pronto
echo ⏳ Aguardando PostgreSQL estar pronto...
:wait_postgres
docker exec inventory-postgres pg_isready -U inventory_user -d inventory_db >nul 2>&1
if %errorlevel% neq 0 (
    timeout /t 2 /nobreak >nul
    goto wait_postgres
)

echo ✅ PostgreSQL está pronto!

REM Executa o script de inserção de dados de teste
echo 📊 Inserindo dados de teste...
docker exec -i inventory-postgres psql -U inventory_user -d inventory_db < insert-test-data.sql

if %errorlevel% equ 0 (
    echo ✅ Dados de teste inseridos com sucesso!
    echo.
    echo 📋 Dados inseridos:
    echo    • 5 lojas
    echo    • 5 categorias de produtos
    echo    • 5 produtos
    echo    • 11 registros de inventário
    echo    • 5 movimentações de estoque
    echo    • 4 fornecedores
    echo    • 3 alertas
    echo.
    echo 🌐 Acesse os serviços:
    echo    • API Gateway: http://localhost:8088
    echo    • Inventory Service: http://localhost:8080
    echo    • Store Service: http://localhost:8081
    echo    • Notification Service: http://localhost:8082
    echo    • Kafka UI: http://localhost:8090
    echo    • Prometheus: http://localhost:9090
    echo    • Grafana: http://localhost:3000 (admin/admin)
    echo    • Jaeger: http://localhost:16686
) else (
    echo ❌ Erro ao inserir dados de teste!
    pause
    exit /b 1
)

pause
