#!/bin/bash

# GitHub Actions Self-hosted Runner Setup Script for Ubuntu Server
# This script helps set up a GitHub Actions runner on Ubuntu

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
REPO_URL=""
TOKEN=""
RUNNER_NAME=$(hostname)
WORK_FOLDER="_work"
INSTALL_AS_SERVICE=false

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -r, --repo URL           Repository URL (required)"
    echo "  -t, --token TOKEN        Registration token (required)"
    echo "  -n, --name NAME          Runner name [default: hostname]"
    echo "  -w, --work-folder DIR    Work folder [default: _work]"
    echo "  -s, --service            Install as systemd service"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -r https://github.com/user/repo -t TOKEN123 -s"
    echo "  $0 --repo https://github.com/user/repo --token TOKEN123 --name my-runner"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -r|--repo)
            REPO_URL="$2"
            shift 2
            ;;
        -t|--token)
            TOKEN="$2"
            shift 2
            ;;
        -n|--name)
            RUNNER_NAME="$2"
            shift 2
            ;;
        -w|--work-folder)
            WORK_FOLDER="$2"
            shift 2
            ;;
        -s|--service)
            INSTALL_AS_SERVICE=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate required parameters
if [[ -z "$REPO_URL" || -z "$TOKEN" ]]; then
    print_error "Repository URL and token are required"
    show_usage
    exit 1
fi

print_info "Setting up GitHub Actions Self-hosted Runner"
print_info "Repository: $REPO_URL"
print_info "Runner Name: $RUNNER_NAME"

# Check if running as root
if [[ $EUID -eq 0 ]]; then
    print_warning "This script is running as root. Creating dedicated user 'github-runner'..."
    
    # Create github-runner user if it doesn't exist
    if ! id "github-runner" &>/dev/null; then
        useradd -m -s /bin/bash github-runner
        print_info "Created user 'github-runner'"
    fi
    
    # Add github-runner to docker group if docker is installed
    if command -v docker &> /dev/null; then
        usermod -aG docker github-runner
        print_info "Added github-runner to docker group"
    fi
    
    RUNNER_USER="github-runner"
    RUNNER_HOME="/home/github-runner"
else
    RUNNER_USER=$(whoami)
    RUNNER_HOME="$HOME"
fi

# Create runner directory
RUNNER_DIR="$RUNNER_HOME/actions-runner"

if [[ -d "$RUNNER_DIR" ]]; then
    print_warning "Directory $RUNNER_DIR already exists. Removing..."
    rm -rf "$RUNNER_DIR"
fi

print_info "Creating runner directory: $RUNNER_DIR"
mkdir -p "$RUNNER_DIR"

# Download latest runner
print_info "Downloading GitHub Actions Runner..."
cd "$RUNNER_DIR"

# Get latest release URL
LATEST_VERSION=$(curl -s https://api.github.com/repos/actions/runner/releases/latest | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/' | sed 's/v//')
DOWNLOAD_URL="https://github.com/actions/runner/releases/download/v${LATEST_VERSION}/actions-runner-linux-x64-${LATEST_VERSION}.tar.gz"

print_info "Downloading version $LATEST_VERSION from: $DOWNLOAD_URL"
curl -o actions-runner-linux-x64.tar.gz -L "$DOWNLOAD_URL"

print_info "Extracting runner..."
tar xzf ./actions-runner-linux-x64.tar.gz
rm actions-runner-linux-x64.tar.gz

# Set ownership if running as root
if [[ $EUID -eq 0 ]]; then
    chown -R github-runner:github-runner "$RUNNER_DIR"
fi

# Install dependencies
print_info "Installing dependencies..."
if command -v apt-get &> /dev/null; then
    # Ubuntu/Debian
    apt-get update -qq
    apt-get install -y curl wget git jq
    
    # Install Docker if not present
    if ! command -v docker &> /dev/null; then
        print_info "Installing Docker..."
        apt-get install -y ca-certificates gnupg lsb-release
        
        # Add Docker's official GPG key
        mkdir -p /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        
        # Add Docker repository
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
        
        apt-get update -qq
        apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
        
        # Start and enable Docker
        systemctl start docker
        systemctl enable docker
        
        print_info "Docker installed successfully"
    fi
else
    print_error "Package manager not supported. Please install curl, wget, git, jq, and Docker manually."
    exit 1
fi

# Configure runner
print_info "Configuring runner..."

CONFIGURE_CMD="./config.sh --url $REPO_URL --token $TOKEN --name $RUNNER_NAME --work $WORK_FOLDER --unattended"

if [[ $EUID -eq 0 ]]; then
    # Run as github-runner user
    sudo -u github-runner bash -c "cd $RUNNER_DIR && $CONFIGURE_CMD"
else
    # Run as current user
    eval "$CONFIGURE_CMD"
fi

if [[ $? -ne 0 ]]; then
    print_error "Failed to configure runner"
    exit 1
fi

# Install as service if requested
if [[ "$INSTALL_AS_SERVICE" == true ]]; then
    print_info "Installing runner as systemd service..."
    
    if [[ $EUID -ne 0 ]]; then
        print_error "Root privileges required to install as service. Run with sudo."
        exit 1
    fi
    
    # Install service
    cd "$RUNNER_DIR"
    sudo -u github-runner ./svc.sh install
    
    if [[ $? -eq 0 ]]; then
        print_info "Starting runner service..."
        ./svc.sh start
        
        if [[ $? -eq 0 ]]; then
            print_info "Runner service started successfully"
            systemctl status actions.runner.*
        else
            print_error "Failed to start runner service"
        fi
    else
        print_error "Failed to install runner as service"
    fi
else
    print_info "Runner configured successfully"
    print_info "To start the runner manually:"
    if [[ $EUID -eq 0 ]]; then
        print_info "  sudo -u github-runner bash -c 'cd $RUNNER_DIR && ./run.sh'"
    else
        print_info "  cd $RUNNER_DIR && ./run.sh"
    fi
    print_info "To install as a service later, run this script with --service as root"
fi

# Final setup information
print_info ""
print_info "=== Setup Complete ==="
print_info "Runner directory: $RUNNER_DIR"
print_info "Repository: $REPO_URL"
print_info "Runner name: $RUNNER_NAME"
print_info "User: $RUNNER_USER"

if [[ "$INSTALL_AS_SERVICE" == true ]]; then
    print_info "Service status: Installed and running"
    print_info "Service management:"
    print_info "  sudo systemctl status actions.runner.*"
    print_info "  sudo systemctl stop actions.runner.*"
    print_info "  sudo systemctl start actions.runner.*"
fi

print_info ""
print_info "Next steps:"
print_info "1. Verify the runner appears in your repository's Settings > Actions > Runners"
print_info "2. Ensure Docker is working: docker run hello-world"
print_info "3. Test deployment by pushing to main branch"

print_info ""
print_info "Useful commands:"
print_info "  Remove runner: cd $RUNNER_DIR && ./config.sh remove --token YOUR_TOKEN"
if [[ "$INSTALL_AS_SERVICE" == true ]]; then
    print_info "  Stop service: sudo ./svc.sh stop"
    print_info "  Uninstall service: sudo ./svc.sh uninstall"
fi

print_info ""
print_info "Docker commands:"
print_info "  Check Docker: docker --version"
print_info "  Test Docker: docker run hello-world"
print_info "  View containers: docker ps"