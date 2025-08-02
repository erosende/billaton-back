# 📋 Checklist de Despliegue - Billaton Backend

## Pre-requisitos ✅

### Servicios Externos
- [ ] **Base de datos PostgreSQL** configurada y accesible
- [ ] **Cloudflare R2** configurado con buckets creados
- [ ] **Supabase** configurado para autenticación JWT
- [ ] **Dominio/DNS** configurado (si es producción)

### Software Requerido
- [ ] **Docker Engine** instalado en Ubuntu Server
- [ ] **Java 21** instalado localmente (para desarrollo)
- [ ] **Maven 3.9+** instalado localmente (para desarrollo)
- [ ] **Git** configurado

## Configuración Local 🏠

### 1. Clonar y Configurar
- [ ] Repositorio clonado: `git clone <repo-url>`
- [ ] Archivo `.env` creado desde `env.example`
- [ ] Variables de entorno configuradas en `.env`

### 2. Pruebas Locales
- [ ] Tests ejecutan correctamente: `mvn test`
- [ ] Aplicación compila: `mvn clean package`
- [ ] Docker build funciona: `docker build -t billaton-backend:latest .`

### 3. Despliegue Local
- [ ] Docker Compose funciona: `docker-compose up -d`
- [ ] Health check responde: `curl http://localhost:8080/api/actuator/health`
- [ ] API endpoints funcionan

## Configuración GitHub Actions 🚀

### 1. Secrets en GitHub
Ir a `Settings > Secrets and variables > Actions` y añadir:

#### Base de Datos
- [ ] `BILLATON_DB_URL` - URL de conexión PostgreSQL
- [ ] `BILLATON_DB_USERNAME` - Usuario de BD
- [ ] `BILLATON_DB_PASSWORD` - Contraseña de BD

#### Cloudflare R2
- [ ] `R2_ACCESS_KEY` - Access Key de R2
- [ ] `R2_SECRET_KEY` - Secret Key de R2
- [ ] `R2_DOCUMENTS_BUCKET` - Bucket para documentos
- [ ] `R2_IMAGES_BUCKET` - Bucket para imágenes
- [ ] `R2_ENDPOINT` - Endpoint de R2

#### Supabase
- [ ] `SUPABASE_JWT_SECRET` - Secret para JWT

### 2. Self-hosted Runner en Ubuntu
- [ ] Script `setup-github-runner.sh` ejecutado correctamente
- [ ] Runner instalado como servicio systemd
- [ ] Runner aparece como "online" en GitHub
- [ ] Docker accesible para el usuario `github-runner`
- [ ] Servicio iniciado: `sudo systemctl status actions.runner.*`

## Configuración Ubuntu Server 🏭

### 1. Servidor Ubuntu
- [ ] **Ubuntu Server 20.04 LTS** o superior
- [ ] **Sistema actualizado**: `sudo apt update && sudo apt upgrade`
- [ ] **Firewall configurado**: `sudo ufw allow 8080`
- [ ] **Recursos**: mínimo 4GB RAM, 2 CPU cores
- [ ] **Espacio en disco**: mínimo 20GB libres

### 2. Docker en Ubuntu
- [ ] **Docker Engine** instalado
- [ ] **Docker Compose** instalado 
- [ ] **Usuario github-runner** creado y en grupo docker
- [ ] **Servicio Docker** habilitado: `sudo systemctl enable docker`
- [ ] **Test Docker**: `docker run hello-world`

### 3. Monitoreo
- [ ] Logs configurados y accesibles
- [ ] Health checks configurados
- [ ] Alertas configuradas (opcional)

## Variables de Entorno 🔧

### Verificar que están configuradas:
```bash
# Localmente (en .env)
BILLATON_DB_URL=jdbc:postgresql://db:5432/billaton
BILLATON_DB_USERNAME=billaton
BILLATON_DB_PASSWORD=tu_password

# En GitHub Secrets
BILLATON_DB_URL=jdbc:postgresql://tu-host:5432/billaton
BILLATON_DB_USERNAME=tu_usuario_prod
BILLATON_DB_PASSWORD=tu_password_prod
```

### Formato correcto:
- [ ] **DB_URL**: Formato `jdbc:postgresql://host:port/database`
- [ ] **R2_ENDPOINT**: URL completa del endpoint
- [ ] **Secrets**: Sin espacios ni caracteres especiales problemáticos

## Tests de Despliegue 🧪

### 1. Tests Locales
- [ ] `mvn test` - Tests unitarios pasan
- [ ] `mvn integration-test` - Tests de integración pasan
- [ ] Aplicación inicia sin errores
- [ ] Health check retorna 200

### 2. Tests de Docker
- [ ] Imagen se construye correctamente
- [ ] Contenedor inicia sin errores
- [ ] Variables de entorno se cargan
- [ ] Puertos expuestos correctamente

### 3. Tests de GitHub Actions
- [ ] Workflow se ejecuta sin errores
- [ ] Build job completa exitosamente
- [ ] Deploy job completa exitosamente
- [ ] Aplicación responde después del deploy

## Verificación Post-Despliegue ✔️

### 1. Funcionalidad
- [ ] **API endpoints** responden correctamente
- [ ] **Base de datos** conecta sin errores
- [ ] **Autenticación JWT** funciona
- [ ] **Subida de archivos** a R2 funciona

### 2. Performance
- [ ] **Tiempo de respuesta** aceptable (< 2s)
- [ ] **Uso de memoria** estable
- [ ] **CPU** no al 100% constantemente
- [ ] **Logs** no muestran errores

### 3. Monitoreo
- [ ] **Health check** accesible: `/api/actuator/health`
- [ ] **Metrics** accesibles: `/api/actuator/metrics`
- [ ] **Logs** de aplicación disponibles
- [ ] **Logs** de contenedor accesibles

## Comandos de Verificación 🔍

### En el Servidor Ubuntu:
```bash
# Verificar estado de contenedores
docker ps

# Ver logs de aplicación
docker logs billaton-app

# Test health check
curl http://localhost:8080/api/actuator/health

# Ver uso de recursos
docker stats billaton-app

# Verificar servicio del runner
sudo systemctl status actions.runner.*

# Verificar logs del runner
sudo journalctl -u actions.runner.* -f

# Verificar GitHub runner status
# En el repo: Settings > Actions > Runners
```

## Troubleshooting 🚨

### Problemas Comunes en Ubuntu
- [ ] **Puerto 8080 ocupado**: `sudo netstat -tlnp | grep :8080`
- [ ] **Variables de entorno**: Verificar formato y valores
- [ ] **Conexión BD**: `telnet tu-host-db 5432`
- [ ] **Permisos Docker**: `groups github-runner` debe incluir docker
- [ ] **Runner offline**: `sudo systemctl status actions.runner.*`
- [ ] **Firewall**: `sudo ufw status` y `sudo ufw allow 8080`

### Rollback Plan
- [ ] **Backup** de versión anterior disponible
- [ ] **Procedimiento** de rollback documentado
- [ ] **Contactos** de emergencia listos

## Documentación 📚

- [ ] **README** actualizado con instrucciones
- [ ] **Configuración** documentada
- [ ] **Procedimientos** de operación claros
- [ ] **Contactos** y responsables definidos

---

## 🎉 Despliegue Exitoso

Una vez completado todo el checklist:

1. ✅ Aplicación responde en puerto esperado
2. ✅ Todos los endpoints funcionan correctamente
3. ✅ GitHub Actions despliega automáticamente
4. ✅ Monitoreo y logs funcionando
5. ✅ Documentación actualizada

**¡Tu aplicación Billaton Backend está lista para producción!** 🚀