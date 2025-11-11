<#
PowerShell helper script to run a quick Strimzi demo locally.
Run this from the repo scripts directory: `.	ools\strimzi-demo.ps1` or `.
esources\strimzi-demo.ps1` depending on location.
#>

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$repoRoot = Resolve-Path (Join-Path $scriptDir "..")

Write-Host "Using repo root: $repoRoot"

Write-Host "1) Create namespace 'kafka' (if not present)"
kubectl create namespace kafka -ErrorAction SilentlyContinue

Write-Host "2) Install Strimzi operator (CRDs + operator)"
kubectl apply -f "https://strimzi.io/install/latest?namespace=kafka" -n kafka

Write-Host "3) Apply Kafka cluster manifest"
kubectl apply -f "$repoRoot\k8s\kafka-cluster.yaml" -n kafka

Write-Host "Waiting for Kafka cluster to become Ready (timeout 300s)..."
kubectl -n kafka wait kafka/my-cluster --for=condition=Ready --timeout=300s

Write-Host "4) Create users topic"
kubectl apply -f "$repoRoot\k8s\users-topic.yaml" -n kafka

Write-Host "Done. To access Kafka from your machine for local development you can run the port-forward command in a separate terminal:" -ForegroundColor Yellow
Write-Host "kubectl -n kafka port-forward svc/my-cluster-kafka-bootstrap 9092:9092" -ForegroundColor Cyan

Write-Host "Then configure your local app (e.g. in application.yml) with: spring.kafka.bootstrap-servers: localhost:9092" -ForegroundColor Green

Write-Host "Notes:" -ForegroundColor Yellow
Write-Host "- The port-forward command runs in foreground; run it in a separate terminal window." -ForegroundColor Yellow
Write-Host "- For production deploy consider external listeners, TLS and KafkaUser resources for authentication." -ForegroundColor Yellow
