#!/bin/bash

# Billaton Backend Deployment Script
# This script helps deploy the application locally or in production

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="local"
BUILD_IMAGE=true
RUN_TESTS=true

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
    echo "  -e, --env ENVIRONMENT     Set environment (local/production) [default: local]"
    echo "  -s, --skip-build         Skip Docker image build"
    echo "  -t, --skip-tests         Skip running tests"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --env local           Deploy locally with Docker Compose"
    echo "  $0 --env production      Deploy for production"
    echo "  $0 --skip-build          Deploy without rebuilding image"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -s|--skip-build)
            BUILD_IMAGE=false
            shift
            ;;
        -t|--skip-tests)
            RUN_TESTS=false
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

# Validate environment
if [[ "$ENVIRONMENT" != "local" && "$ENVIRONMENT" != "production" ]]; then
    print_error "Invalid environment: $ENVIRONMENT. Must be 'local' or 'production'"
    exit 1
fi

print_info "Starting deployment for environment: $ENVIRONMENT"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if .env file exists
if [[ "$ENVIRONMENT" == "local" && ! -f ".env" ]]; then
    print_warning ".env file not found. Creating from .env.example"
    if [[ -f ".env.example" ]]; then
        cp .env.example .env
        print_warning "Please edit .env file with your actual configuration values"
        exit 1
    else
        print_error ".env.example file not found. Cannot create .env file."
        exit 1
    fi
fi

# Run tests if requested
if [[ "$RUN_TESTS" == true ]]; then
    print_info "Running tests..."
    if ! mvn clean test; then
        print_error "Tests failed. Aborting deployment."
        exit 1
    fi
    print_info "Tests passed successfully"
fi

# Build Docker image if requested
if [[ "$BUILD_IMAGE" == true ]]; then
    print_info "Building Docker image..."
    if ! docker build -t billaton-backend:latest .; then
        print_error "Docker build failed"
        exit 1
    fi
    print_info "Docker image built successfully"
fi

# Deploy based on environment
if [[ "$ENVIRONMENT" == "local" ]]; then
    print_info "Deploying locally with Docker Compose..."
    
    # Stop existing containers
    docker-compose down --remove-orphans
    
    # Start services
    docker-compose up -d
    
    # Wait for services to be healthy
    print_info "Waiting for services to be healthy..."
    sleep 30
    
    # Check application health
    max_attempts=30
    attempt=1
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
            print_info "Application is healthy and ready!"
            print_info "API available at: http://localhost:8080/api"
            print_info "Health check: http://localhost:8080/api/actuator/health"
            break
        fi
        
        if [[ $attempt -eq $max_attempts ]]; then
            print_error "Application failed to start after $max_attempts attempts"
            print_info "Checking logs..."
            docker-compose logs app
            exit 1
        fi
        
        print_info "Waiting for application to start... ($attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
    
elif [[ "$ENVIRONMENT" == "production" ]]; then
    print_info "Deploying for production..."
    
    # Stop existing container
    docker stop billaton-app || true
    docker rm billaton-app || true
    
    # Check if all required environment variables are set
    required_vars=(
        "BILLATON_DB_URL"
        "BILLATON_DB_USERNAME" 
        "BILLATON_DB_PASSWORD"
        "R2_ACCESS_KEY"
        "R2_SECRET_KEY"
        "R2_DOCUMENTS_BUCKET"
        "R2_IMAGES_BUCKET"
        "R2_ENDPOINT"
        "SUPABASE_JWT_SECRET"
    )
    
    missing_vars=()
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            missing_vars+=("$var")
        fi
    done
    
    if [[ ${#missing_vars[@]} -gt 0 ]]; then
        print_error "Missing required environment variables:"
        for var in "${missing_vars[@]}"; do
            echo "  - $var"
        done
        print_error "Please set these variables and try again"
        exit 1
    fi
    
    # Run production container
    docker run -d \
        --name billaton-app \
        --restart unless-stopped \
        -p 8080:8080 \
        -e BILLATON_DB_URL="$BILLATON_DB_URL" \
        -e BILLATON_DB_USERNAME="$BILLATON_DB_USERNAME" \
        -e BILLATON_DB_PASSWORD="$BILLATON_DB_PASSWORD" \
        -e R2_ACCESS_KEY="$R2_ACCESS_KEY" \
        -e R2_SECRET_KEY="$R2_SECRET_KEY" \
        -e R2_DOCUMENTS_BUCKET="$R2_DOCUMENTS_BUCKET" \
        -e R2_IMAGES_BUCKET="$R2_IMAGES_BUCKET" \
        -e R2_ENDPOINT="$R2_ENDPOINT" \
        -e SUPABASE_JWT_SECRET="$SUPABASE_JWT_SECRET" \
        -e SUPABASE_STORAGE_URL="$SUPABASE_STORAGE_URL" \
        -e SUPABASE_STORAGE_ACCESS_KEY="$SUPABASE_STORAGE_ACCESS_KEY" \
        -e SUPABASE_STORAGE_SECRET_KEY="$SUPABASE_STORAGE_SECRET_KEY" \
        -e SUPABASE_PROJECT_ID="$SUPABASE_PROJECT_ID" \
        -e PORT=8080 \
        billaton-backend:latest
    
    # Wait for application to start
    print_info "Waiting for application to start..."
    max_attempts=30
    attempt=1
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
            print_info "Application deployed successfully!"
            break
        fi
        
        if [[ $attempt -eq $max_attempts ]]; then
            print_error "Application failed to start after $max_attempts attempts"
            print_info "Checking logs..."
            docker logs billaton-app
            exit 1
        fi
        
        print_info "Waiting for application to start... ($attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
fi

print_info "Deployment completed successfully!"

# Show running containers
print_info "Running containers:"
docker ps --filter "name=billaton" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"