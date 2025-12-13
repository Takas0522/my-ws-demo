#!/bin/bash
# Java 21 Migration Verification Script
# This script verifies that all services build correctly and tests pass

set -e  # Exit on error

echo "======================================"
echo "Java 21 Migration Verification Script"
echo "======================================"
echo ""

# Check Java version
echo "📌 Checking Java version..."
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
java -version
echo ""

# Check Maven version
echo "📌 Checking Maven version..."
mvn -version
echo ""

# Verify Payara Micro (if available)
if [ -f "/tmp/payara-micro.jar" ]; then
    echo "📌 Payara Micro 6.2024.10 available at /tmp/payara-micro.jar"
else
    echo "⚠️  Payara Micro not found at /tmp/payara-micro.jar"
    echo "   Download with: wget https://repo1.maven.org/maven2/fish/payara/extras/payara-micro/6.2024.10/payara-micro-6.2024.10.jar -O /tmp/payara-micro.jar"
fi
echo ""

# Build and test each service
services=("auth-service" "user-service" "point-service" "bff")
total_tests=0
total_failures=0

for service in "${services[@]}"; do
    echo "======================================"
    echo "🔨 Building and testing: $service"
    echo "======================================"
    
    cd "src/$service"
    
    # Clean build with tests
    mvn clean test -q
    
    # Count tests
    if [ -d "target/surefire-reports" ]; then
        test_count=$(find target/surefire-reports -name "TEST-*.xml" | wc -l)
        echo "✅ $service: $test_count test classes executed"
        total_tests=$((total_tests + test_count))
        
        # Check for failures
        failures=$(grep -r "failures=" target/surefire-reports/TEST-*.xml | grep -v 'failures="0"' | wc -l || true)
        if [ "$failures" -gt 0 ]; then
            echo "❌ $service: Test failures detected!"
            total_failures=$((total_failures + 1))
        fi
    fi
    
    # Verify WAR file
    if [ -f "target/$service.war" ]; then
        size=$(du -h "target/$service.war" | cut -f1)
        echo "✅ $service.war generated ($size)"
    else
        echo "❌ $service.war not found!"
        total_failures=$((total_failures + 1))
    fi
    
    cd ../..
    echo ""
done

# Summary
echo "======================================"
echo "📊 Verification Summary"
echo "======================================"
echo "Total test classes executed: $total_tests"
echo "Total failures: $total_failures"
echo ""

if [ "$total_failures" -eq 0 ]; then
    echo "✅ All verifications passed!"
    echo ""
    echo "Next steps:"
    echo "1. Run integration tests: mvn verify (for each service)"
    echo "2. Run E2E tests: cd src/e2e && npm run test:cucumber"
    echo "3. Manual verification with all services running"
    exit 0
else
    echo "❌ Some verifications failed. Please check the output above."
    exit 1
fi
