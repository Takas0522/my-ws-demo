#!/bin/bash

# E2Eテスト用のサービス起動・停止スクリプト

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

# 色付きログ
log_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1"
}

log_error() {
    echo -e "\033[0;31m[ERROR]\033[0m $1"
}

log_warn() {
    echo -e "\033[0;33m[WARN]\033[0m $1"
}

# 環境変数ファイルを読み込む関数
load_env_file() {
    local env_file=$1
    if [ -f "$env_file" ]; then
        log_info "Loading environment variables from $env_file"
        set -a
        source "$env_file"
        set +a
    else
        log_error "Environment file not found: $env_file"
        return 1
    fi
}

# サービスを起動する関数
start_service() {
    local service_name=$1
    local service_port=$2
    local debug_port=$3
    local env_file=$4
    
    local service_dir="$WORKSPACE_ROOT/src/$service_name"
    local war_file="$service_dir/target/$service_name.war"
    
    log_info "Starting $service_name..."
    
    # 環境変数ファイルを読み込む
    if ! load_env_file "$env_file"; then
        return 1
    fi
    
    # WARファイルの存在確認
    if [ ! -f "$war_file" ]; then
        log_error "WAR file not found: $war_file"
        log_info "Building $service_name..."
        cd "$service_dir" && mvn clean package -DskipTests
        if [ $? -ne 0 ]; then
            log_error "Failed to build $service_name"
            return 1
        fi
    fi
    
    # サービスを起動（環境変数ファイルを明示的に渡す）
    cd "$service_dir"
    
    # Java 21で必要なモジュールアクセスオプション
    JAVA_OPTS="--add-opens java.base/jdk.internal.loader=ALL-UNNAMED"
    JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.lang=ALL-UNNAMED"
    JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.net=ALL-UNNAMED"
    JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.nio=ALL-UNNAMED"
    JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.util=ALL-UNNAMED"
    JAVA_OPTS="$JAVA_OPTS --add-opens java.base/sun.nio.ch=ALL-UNNAMED"
    JAVA_OPTS="$JAVA_OPTS --add-opens java.management/sun.management=ALL-UNNAMED"
    JAVA_OPTS="$JAVA_OPTS --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED"
    
    # 環境変数をJavaに渡すため、envコマンドで起動
    if [ -f "$env_file" ]; then
        # .envファイルを読み込んでenvコマンドで渡す
        nohup env $(cat "$env_file" | grep -v '^#' | xargs) \
            java $JAVA_OPTS \
            -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$debug_port \
            -jar /opt/payara-micro.jar \
            --deploy "$war_file" \
            --port $service_port \
            --nocluster \
            > "$service_dir/e2e-test.log" 2>&1 &
    else
        nohup java $JAVA_OPTS \
            -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$debug_port \
            -jar /opt/payara-micro.jar \
            --deploy "$war_file" \
            --port $service_port \
            --nocluster \
            > "$service_dir/e2e-test.log" 2>&1 &
    fi
    
    local pid=$!
    echo $pid > "$service_dir/.e2e-pid"
    
    log_info "$service_name started (PID: $pid, Port: $service_port, Debug: $debug_port)"
    
    # サービスが起動するまで待機
    log_info "Waiting for $service_name to be ready..."
    for i in {1..30}; do
        if curl -s http://localhost:$service_port > /dev/null 2>&1; then
            log_info "$service_name is ready!"
            return 0
        fi
        sleep 2
    done
    
    log_warn "$service_name may not be ready yet"
    return 0
}

# サービスを停止する関数
stop_service() {
    local service_name=$1
    local service_dir="$WORKSPACE_ROOT/src/$service_name"
    local pid_file="$service_dir/.e2e-pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            log_info "Stopping $service_name (PID: $pid)..."
            kill $pid
            sleep 2
            
            # 強制終了が必要な場合
            if ps -p $pid > /dev/null 2>&1; then
                log_warn "Force killing $service_name..."
                kill -9 $pid
            fi
            
            log_info "$service_name stopped"
        else
            log_warn "$service_name (PID: $pid) is not running"
        fi
        rm -f "$pid_file"
    else
        log_warn "PID file not found for $service_name"
        # プロセス名で検索して停止
        pkill -f "payara-micro.*$service_name"
    fi
}

# フロントエンドを起動する関数
start_frontend() {
    local frontend_dir="$WORKSPACE_ROOT/src/frontend"
    
    log_info "Starting frontend..."
    
    cd "$frontend_dir"
    nohup npm run dev > "$frontend_dir/e2e-test.log" 2>&1 &
    
    local pid=$!
    echo $pid > "$frontend_dir/.e2e-pid"
    
    log_info "Frontend started (PID: $pid, Port: 3000)"
    
    # フロントエンドが起動するまで待機
    log_info "Waiting for frontend to be ready..."
    for i in {1..30}; do
        if curl -s http://localhost:3000 > /dev/null 2>&1; then
            log_info "Frontend is ready!"
            return 0
        fi
        sleep 2
    done
    
    log_warn "Frontend may not be ready yet"
    return 0
}

# フロントエンドを停止する関数
stop_frontend() {
    local frontend_dir="$WORKSPACE_ROOT/src/frontend"
    local pid_file="$frontend_dir/.e2e-pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            log_info "Stopping frontend (PID: $pid)..."
            kill $pid
            sleep 2
            
            if ps -p $pid > /dev/null 2>&1; then
                log_warn "Force killing frontend..."
                kill -9 $pid
            fi
            
            log_info "Frontend stopped"
        else
            log_warn "Frontend (PID: $pid) is not running"
        fi
        rm -f "$pid_file"
    else
        log_warn "PID file not found for frontend"
        pkill -f "vite.*frontend"
    fi
}

# 全サービスを起動
start_all() {
    local env_suffix=${1:-.env}
    
    log_info "Starting all services with $env_suffix..."
    
    start_service "user-service" 8080 5005 "$WORKSPACE_ROOT/src/user-service/$env_suffix"
    start_service "auth-service" 8081 5006 "$WORKSPACE_ROOT/src/auth-service/$env_suffix"
    start_service "point-service" 8082 5007 "$WORKSPACE_ROOT/src/point-service/$env_suffix"
    start_service "bff" 8090 5008 "$WORKSPACE_ROOT/src/bff/$env_suffix"
    start_frontend
    
    log_info "All services started!"
}

# 全サービスを停止
stop_all() {
    log_info "Stopping all services..."
    
    stop_frontend
    stop_service "bff"
    stop_service "point-service"
    stop_service "auth-service"
    stop_service "user-service"
    
    log_info "All services stopped!"
}

# メイン処理
case "$1" in
    start)
        start_all "${2:-.env}"
        ;;
    stop)
        stop_all
        ;;
    restart)
        stop_all
        sleep 2
        start_all "${2:-.env}"
        ;;
    start-e2e)
        start_all ".env.e2e"
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|start-e2e} [env-file]"
        echo ""
        echo "Commands:"
        echo "  start         - Start all services with .env (default)"
        echo "  start-e2e     - Start all services with .env.e2e (for E2E tests)"
        echo "  stop          - Stop all services"
        echo "  restart       - Restart all services"
        echo ""
        echo "Examples:"
        echo "  $0 start              # Start with .env"
        echo "  $0 start .env.e2e     # Start with .env.e2e"
        echo "  $0 start-e2e          # Start with .env.e2e"
        echo "  $0 stop               # Stop all services"
        exit 1
        ;;
esac
