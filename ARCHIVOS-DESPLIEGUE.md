# 📁 Archivos de Despliegue - Billaton Backend Ubuntu

Resumen de todos los archivos necesarios para el despliegue en Ubuntu Server.

## 🐳 Archivos Docker

### `Dockerfile`
- **Propósito**: Imagen Docker multi-stage optimizada para la aplicación
- **Características**:
  - Build con Maven y OpenJDK 21
  - Runtime con OpenJDK 21 slim
  - Usuario no-root para seguridad
  - Health check integrado

### `docker-compose.yml`
- **Propósito**: Despliegue local completo con PostgreSQL
- **Incluye**:
  - Aplicación Spring Boot
  - Base de datos PostgreSQL
  - Redes y volúmenes
  - Health checks

### `.dockerignore`
- **Propósito**: Optimizar contexto de build Docker
- **Excluye**: documentación, IDE files, builds previos

## 🚀 Scripts de Despliegue

### `deploy.sh`
- **Propósito**: Script principal de despliegue para Linux/Ubuntu
- **Funciones**:
  - Tests automáticos
  - Build de imagen Docker
  - Despliegue local y producción
  - Health checks y verificación

### `setup-github-runner.sh`
- **Propósito**: Configuración automática del GitHub Actions Runner
- **Funciones**:
  - Instalación de dependencias
  - Configuración de Docker
  - Instalación como servicio systemd
  - Configuración de usuario dedicado

## ⚙️ Configuración

### `env.example`
- **Propósito**: Template de variables de entorno
- **Incluye**: todas las variables necesarias para la aplicación

### `.github/workflows/deploy.yml`
- **Propósito**: Workflow de CI/CD con GitHub Actions
- **Etapas**:
  1. Test - ejecuta pruebas unitarias
  2. Build - construye imagen Docker
  3. Deploy - despliega en self-hosted runner

## 📚 Documentación

### `README-DEPLOYMENT.md`
- **Propósito**: Guía completa de despliegue
- **Contenido**:
  - Opciones de despliegue (local/producción)
  - Configuración de GitHub Actions
  - Monitoreo y troubleshooting
  - Comandos útiles

### `UBUNTU-SETUP.md`
- **Propósito**: Guía específica para configurar Ubuntu Server
- **Contenido**:
  - Instalación de Docker
  - Configuración de firewall
  - Setup del GitHub Actions Runner
  - Scripts de monitoreo
  - Troubleshooting específico Ubuntu

### `DEPLOYMENT-CHECKLIST.md`
- **Propósito**: Lista de verificación paso a paso
- **Incluye**:
  - Pre-requisitos
  - Configuración local
  - Configuración GitHub Actions
  - Configuración Ubuntu Server
  - Tests de verificación

### `ARCHIVOS-DESPLIEGUE.md` (este archivo)
- **Propósito**: Índice y explicación de todos los archivos

## 🗂️ Estructura Final de Archivos

```
billaton-back/
├── Dockerfile                    # ✅ Imagen Docker de la aplicación
├── docker-compose.yml           # ✅ Despliegue local completo
├── .dockerignore                 # ✅ Optimización build Docker
├── deploy.sh                     # ✅ Script principal despliegue
├── setup-github-runner.sh       # ✅ Setup GitHub Runner Ubuntu
├── env.example                   # ✅ Template variables entorno
├── .github/
│   └── workflows/
│       └── deploy.yml           # ✅ CI/CD GitHub Actions
├── README-DEPLOYMENT.md         # ✅ Guía completa despliegue
├── UBUNTU-SETUP.md              # ✅ Guía específica Ubuntu
├── DEPLOYMENT-CHECKLIST.md      # ✅ Lista verificación
├── ARCHIVOS-DESPLIEGUE.md       # ✅ Este archivo (índice)
└── [archivos de código fuente...]
```

## ❌ Archivos Eliminados (específicos Windows)

Los siguientes archivos fueron eliminados ya que el despliegue es en Ubuntu Server:

- ~~`deploy.ps1`~~ - Script PowerShell Windows
- ~~`deploy-windows.bat`~~ - Script Command Prompt Windows  
- ~~`setup-github-runner.ps1`~~ - Setup GitHub Runner Windows

## 🚀 Orden de Uso Recomendado

### 1. Preparación Ubuntu Server
```bash
# Seguir la guía completa
cat UBUNTU-SETUP.md
```

### 2. Configuración GitHub
```bash
# Configurar secrets en GitHub
# Settings > Secrets and variables > Actions
```

### 3. Setup GitHub Runner
```bash
# En el servidor Ubuntu
chmod +x setup-github-runner.sh
sudo ./setup-github-runner.sh -r REPO_URL -t TOKEN -s
```

### 4. Configuración Local (desarrollo)
```bash
# Copiar variables de entorno
cp env.example .env
# Editar .env con valores reales

# Test local
./deploy.sh --env local
```

### 5. Despliegue Automático
```bash
# Push a main para trigger automático
git push origin main
```

### 6. Verificación
```bash
# Usar checklist
cat DEPLOYMENT-CHECKLIST.md
```

## 📋 Lista de Verificación Rápida

- [ ] ✅ `Dockerfile` - imagen optimizada
- [ ] ✅ `docker-compose.yml` - despliegue local
- [ ] ✅ `deploy.sh` - script principal
- [ ] ✅ `setup-github-runner.sh` - setup runner
- [ ] ✅ `.github/workflows/deploy.yml` - CI/CD
- [ ] ✅ `env.example` - template configuración
- [ ] ✅ Documentación completa (3 guías)
- [ ] ❌ Sin archivos Windows residuales

## 💡 Comandos Útiles de Gestión

```bash
# Hacer todos los scripts ejecutables
chmod +x deploy.sh setup-github-runner.sh

# Ver estado del sistema
docker ps
sudo systemctl status actions.runner.*

# Verificar aplicación
curl http://localhost:8080/api/actuator/health

# Ver logs
docker logs billaton-app -f
sudo journalctl -u actions.runner.* -f
```

---

**✅ Todos los archivos están optimizados para despliegue en Ubuntu Server**

Esta configuración proporciona un sistema completo de CI/CD con GitHub Actions, despliegue automatizado con Docker, y documentación exhaustiva específica para Ubuntu Server.