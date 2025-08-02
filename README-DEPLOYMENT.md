# Guía de Despliegue - Billaton Backend

Esta guía te ayudará a desplegar tu aplicación Spring Boot usando GitHub Actions y Docker.

## 📋 Prerrequisitos

- **Servidor Ubuntu Server** 20.04 LTS o superior
- **Docker y Docker Compose** instalados
- **Maven 3.9+ y Java 21+** (para desarrollo local)
- **Acceso a servicios externos**:
  - Base de datos PostgreSQL
  - Cloudflare R2
  - Supabase (para JWT)

> 📖 **Guía detallada**: Ver [UBUNTU-SETUP.md](UBUNTU-SETUP.md) para configurar Ubuntu Server paso a paso.

## 🚀 Opciones de Despliegue

### 1. Despliegue Local con Docker Compose

#### Configuración inicial:
```bash
# 1. Copia el archivo de configuración
cp env.example .env

# 2. Edita las variables de entorno
nano .env  # o tu editor preferido
```

#### Despliegue automático:
```bash
# Despliegue completo (con tests y build)
./deploy.sh --env local

# Despliegue rápido (sin tests ni rebuild)
./deploy.sh --env local --skip-tests --skip-build
```

#### Despliegue manual:
```bash
# Construir la imagen
docker build -t billaton-backend:latest .

# Iniciar servicios
docker-compose up -d

# Verificar estado
docker-compose ps
```

### 2. Despliegue en Producción

#### Con GitHub Actions (Recomendado):

1. **Configurar GitHub Secrets:**
   Ve a `Settings > Secrets and variables > Actions` y añade:
   
   ```
   BILLATON_DB_URL=jdbc:postgresql://tu-host:5432/billaton
   BILLATON_DB_USERNAME=tu_usuario
   BILLATON_DB_PASSWORD=tu_password
   R2_ACCESS_KEY=tu_access_key
   R2_SECRET_KEY=tu_secret_key
   R2_DOCUMENTS_BUCKET=tu_bucket_documentos
   R2_IMAGES_BUCKET=tu_bucket_imagenes
   R2_ENDPOINT=tu_endpoint_r2
   SUPABASE_JWT_SECRET=tu_jwt_secret
   ```

2. **Configurar Self-hosted Runner en Ubuntu:**
   ```bash
   # En tu servidor Ubuntu
   # Obtén el token desde GitHub: Settings > Actions > Runners > New self-hosted runner
   
   # Ejecutar script de configuración
   sudo ./setup-github-runner.sh -r https://github.com/tu-usuario/tu-repo -t TU_TOKEN -s
   ```

3. **Desplegar:**
   - Haz push a la rama `main`
   - GitHub Actions se ejecutará automáticamente
   - La aplicación se desplegará en tu máquina

#### Manual en Producción:
```bash
# Configurar variables de entorno
export BILLATON_DB_URL="jdbc:postgresql://tu-host:5432/billaton"
export BILLATON_DB_USERNAME="tu_usuario"
# ... resto de variables

# Desplegar
./deploy.sh --env production
```

## 🔧 Configuración del GitHub Actions

### Estructura del Workflow

El workflow incluye 3 jobs:

1. **Test**: Ejecuta las pruebas unitarias
2. **Build**: Construye la imagen Docker
3. **Deploy**: Despliega en el runner self-hosted

### Self-hosted Runner en Ubuntu Server

Para que GitHub Actions pueda desplegar en tu servidor Ubuntu:

1. **Configuración automática (Recomendado):**
   ```bash
   # Hacer el script ejecutable
   chmod +x setup-github-runner.sh
   
   # Configurar runner como servicio (requiere sudo)
   sudo ./setup-github-runner.sh \
     --repo https://github.com/tu-usuario/tu-repo \
     --token TU_TOKEN \
     --service
   ```

2. **Configuración manual:**
   ```bash
   # Crear directorio
   mkdir actions-runner && cd actions-runner
   
   # Descargar última versión
   curl -o actions-runner-linux-x64.tar.gz -L \
     $(curl -s https://api.github.com/repos/actions/runner/releases/latest | \
       grep "browser_download_url.*linux-x64" | cut -d '"' -f 4)
   
   # Extraer y configurar
   tar xzf ./actions-runner-linux-x64.tar.gz
   ./config.sh --url https://github.com/tu-usuario/tu-repo --token TU_TOKEN
   
   # Instalar como servicio
   sudo ./svc.sh install
   sudo ./svc.sh start
   ```

3. **Verificar instalación:**
   ```bash
   # Verificar servicio
   sudo systemctl status actions.runner.*
   
   # Verificar Docker
   docker run hello-world
   ```

## 🏥 Monitoreo y Salud

### Endpoints de Salud
- **Health Check**: `http://localhost:8080/api/actuator/health`
- **Metrics**: `http://localhost:8080/api/actuator/metrics`
- **Info**: `http://localhost:8080/api/actuator/info`

### Verificar Estado
```bash
# Estado de contenedores
docker ps

# Logs de la aplicación
docker logs billaton-app

# Health check manual
curl http://localhost:8080/api/actuator/health
```

### Comandos Útiles
```bash
# Reiniciar aplicación
docker restart billaton-app

# Ver logs en tiempo real
docker logs -f billaton-app

# Acceder al contenedor
docker exec -it billaton-app bash

# Limpiar recursos Docker
docker system prune -f
```

## 🐛 Troubleshooting

### Problemas Comunes

1. **Error de conexión a base de datos:**
   ```bash
   # Verificar variables de entorno
   docker exec billaton-app env | grep BILLATON_DB
   
   # Verificar conectividad
   docker exec billaton-app ping tu-host-db
   ```

2. **Error en variables de entorno:**
   ```bash
   # Verificar todas las variables
   docker exec billaton-app env | grep -E "(BILLATON|R2|SUPABASE)"
   ```

3. **Puerto ocupado:**
   ```bash
   # Verificar qué usa el puerto 8080
   lsof -i :8080
   
   # Cambiar puerto en docker-compose.yml o .env
   ports:
     - "8081:8080"  # Mapear a puerto 8081 local
   ```

4. **Problemas de memoria:**
   ```bash
   # Ajustar memoria JVM
   docker run ... -e JAVA_OPTS="-Xmx1g -Xms512m" billaton-backend:latest
   ```

## 🔄 Actualizaciones

### Actualización Automática (GitHub Actions)
- Simplemente haz push a `main`
- El workflow se ejecutará automáticamente

### Actualización Manual
```bash
# Pull último código
git pull origin main

# Rebuilding y desplegar
./deploy.sh --env local  # o production
```

## 📊 Configuración de Producción

### Recursos Recomendados
- **CPU**: 2+ cores
- **RAM**: 4GB+ (2GB para la aplicación + PostgreSQL)
- **Disco**: 20GB+ para logs y datos

### Variables de Entorno de Producción
```bash
# Performance
JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_DEV_EROSENDE=DEBUG

# Pool de conexiones
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
```

## 🔐 Seguridad

- Las variables de entorno con secretos nunca deben commitirse
- Usa GitHub Secrets para almacenar credenciales
- El contenedor se ejecuta con usuario no-root
- Considera usar un reverse proxy (nginx) en producción

## 📞 Soporte

Si encuentras problemas:
1. Revisa los logs: `docker logs billaton-app`
2. Verifica el health check: `curl http://localhost:8080/api/actuator/health`
3. Consulta esta guía para troubleshooting
4. Revisa la configuración de variables de entorno