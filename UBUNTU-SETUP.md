# 🐧 Configuración Ubuntu Server para Billaton Backend

Esta guía específica te ayudará a preparar un servidor Ubuntu para desplegar la aplicación Billaton Backend.

## 📋 Requisitos del Sistema

- **Ubuntu Server**: 20.04 LTS o superior
- **RAM**: Mínimo 4GB (recomendado 8GB)
- **CPU**: Mínimo 2 cores
- **Almacenamiento**: Mínimo 20GB libres
- **Red**: Acceso a internet y conectividad con base de datos

## 🔧 Configuración Inicial del Servidor

### 1. Actualizar el Sistema
```bash
# Actualizar paquetes
sudo apt update && sudo apt upgrade -y

# Instalar utilidades básicas
sudo apt install -y curl wget git htop net-tools ufw
```

### 2. Configurar Firewall
```bash
# Habilitar UFW
sudo ufw enable

# Permitir SSH (importante para no perder acceso)
sudo ufw allow ssh

# Permitir puerto de la aplicación
sudo ufw allow 8080

# Verificar reglas
sudo ufw status
```

### 3. Instalar Docker Engine
```bash
# Eliminar versiones antiguas si existen
sudo apt remove docker docker-engine docker.io containerd runc

# Instalar dependencias
sudo apt update
sudo apt install -y ca-certificates curl gnupg lsb-release

# Añadir clave GPG oficial de Docker
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Añadir repositorio
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar Docker Engine
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Habilitar y iniciar Docker
sudo systemctl enable docker
sudo systemctl start docker

# Verificar instalación
sudo docker run hello-world
```

### 4. Instalar Docker Compose (standalone)
```bash
# Descargar última versión
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Hacer ejecutable
sudo chmod +x /usr/local/bin/docker-compose

# Verificar instalación
docker-compose --version
```

### 5. Configurar Usuario para GitHub Actions
```bash
# Crear usuario dedicado
sudo useradd -m -s /bin/bash github-runner

# Añadir al grupo docker
sudo usermod -aG docker github-runner

# Verificar grupos
groups github-runner
```

## 🚀 Configurar GitHub Actions Runner

### 1. Clonar el Repositorio (temporal)
```bash
# Clonar en directorio temporal para obtener el script
git clone https://github.com/tu-usuario/tu-repo.git /tmp/billaton-setup
cd /tmp/billaton-setup
```

### 2. Ejecutar Script de Configuración
```bash
# Hacer ejecutable
chmod +x setup-github-runner.sh

# Configurar runner (requiere token de GitHub)
sudo ./setup-github-runner.sh \
  --repo https://github.com/tu-usuario/tu-repo \
  --token TU_TOKEN_DE_GITHUB \
  --service

# Limpiar archivos temporales
cd / && rm -rf /tmp/billaton-setup
```

### 3. Verificar Configuración del Runner
```bash
# Verificar estado del servicio
sudo systemctl status actions.runner.*

# Ver logs del runner
sudo journalctl -u actions.runner.* -f

# Verificar que el usuario puede usar Docker
sudo -u github-runner docker run hello-world
```

## 🔒 Configuración de Seguridad

### 1. Configurar SSH (si es acceso remoto)
```bash
# Editar configuración SSH
sudo nano /etc/ssh/sshd_config

# Configuraciones recomendadas:
# Port 22 (o cambiar por seguridad)
# PermitRootLogin no
# PasswordAuthentication yes (o usar keys)
# AllowUsers tu-usuario github-runner

# Reiniciar SSH
sudo systemctl restart ssh
```

### 2. Configurar Logs de Docker
```bash
# Crear configuración de logging
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

# Reiniciar Docker
sudo systemctl restart docker
```

## 📊 Monitoreo y Mantenimiento

### 1. Scripts de Monitoreo
```bash
# Crear script de monitoreo básico
sudo tee /usr/local/bin/billaton-status.sh > /dev/null <<'EOF'
#!/bin/bash
echo "=== Billaton Backend Status ==="
echo "Date: $(date)"
echo ""
echo "=== Docker Containers ==="
docker ps --filter "name=billaton"
echo ""
echo "=== GitHub Runner Service ==="
systemctl status actions.runner.* --no-pager
echo ""
echo "=== Application Health ==="
curl -s http://localhost:8080/api/actuator/health | jq . || echo "Application not responding"
echo ""
echo "=== System Resources ==="
free -h
df -h /
EOF

sudo chmod +x /usr/local/bin/billaton-status.sh
```

### 2. Configurar Logrotate
```bash
# Configurar rotación de logs de la aplicación
sudo tee /etc/logrotate.d/billaton > /dev/null <<EOF
/var/lib/docker/containers/*/*.log {
    daily
    rotate 7
    compress
    missingok
    notifempty
    create 0644 root root
}
EOF
```

### 3. Cronjob para Limpieza Automática
```bash
# Añadir limpieza semanal de Docker
(crontab -l 2>/dev/null; echo "0 2 * * 0 docker system prune -f") | crontab -
```

## 🧪 Verificación de la Configuración

### 1. Tests de Conectividad
```bash
# Test conexión a GitHub
curl -I https://github.com

# Test conexión a Docker Hub
docker pull hello-world

# Test conectividad a base de datos (reemplazar con tu host)
telnet tu-host-db 5432
```

### 2. Test del Runner
```bash
# Verificar que el runner aparece online en:
# GitHub.com → Tu Repo → Settings → Actions → Runners

# Test manual del runner
sudo -u github-runner bash -c "cd /home/github-runner/actions-runner && ./run.sh" &
# Ctrl+C para parar
```

### 3. Test de Despliegue
```bash
# Hacer push al repositorio para probar el despliegue automático
# git push origin main

# Verificar que la aplicación se despliega correctamente
# Los logs aparecerán en: GitHub → Actions → tu workflow
```

## 🆘 Troubleshooting Ubuntu

### Problemas Comunes

1. **Runner no aparece como online:**
   ```bash
   sudo systemctl restart actions.runner.*
   sudo journalctl -u actions.runner.* --no-pager
   ```

2. **Error de permisos Docker:**
   ```bash
   sudo usermod -aG docker github-runner
   # Reiniciar sesión o servicio del runner
   ```

3. **Puerto 8080 ocupado:**
   ```bash
   sudo netstat -tlnp | grep :8080
   sudo kill -9 PID_DEL_PROCESO
   ```

4. **Falta de espacio en disco:**
   ```bash
   df -h
   docker system prune -a -f
   sudo apt autoremove -y
   ```

5. **Problemas de red/DNS:**
   ```bash
   # Verificar DNS
   nslookup google.com
   
   # Verificar conectividad
   ping 8.8.8.8
   ```

## 📚 Comandos Útiles

### Gestión del Servicio
```bash
# Ver estado
sudo systemctl status actions.runner.*

# Iniciar
sudo systemctl start actions.runner.*

# Parar
sudo systemctl stop actions.runner.*

# Reiniciar
sudo systemctl restart actions.runner.*

# Ver logs
sudo journalctl -u actions.runner.* -f
```

### Gestión de Docker
```bash
# Ver contenedores
docker ps -a

# Ver imágenes
docker images

# Limpiar sistema
docker system prune -a -f

# Ver uso de recursos
docker stats

# Ver logs de contenedor
docker logs billaton-app -f
```

### Monitoreo del Sistema
```bash
# Uso de CPU y memoria
htop

# Uso de disco
df -h

# Procesos de red
sudo netstat -tulpn

# Estado general
billaton-status.sh
```

---

## ✅ Checklist Final de Ubuntu

- [ ] Ubuntu Server actualizado
- [ ] Docker Engine instalado y funcionando
- [ ] Docker Compose instalado
- [ ] Usuario `github-runner` creado y configurado
- [ ] Firewall configurado (puerto 8080 abierto)
- [ ] GitHub Actions Runner instalado como servicio
- [ ] Runner aparece como "online" en GitHub
- [ ] Tests de conectividad completados
- [ ] Scripts de monitoreo configurados
- [ ] Logrotate configurado

**¡Tu servidor Ubuntu está listo para recibir despliegues automáticos!** 🎉