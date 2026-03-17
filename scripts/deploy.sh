#!/bin/bash
# Скрипт деплоя на VPS

set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR" || exit 1

echo "=== Проект: $PROJECT_DIR ==="

echo "=== Получаем последние изменения ==="
git pull origin main

echo "=== Собираем образы ==="
docker compose build --no-cache

echo "=== Поднимаем контейнеры ==="
docker compose up -d --remove-orphans

echo "=== Удаляем старые образы ==="
docker image prune -f

echo "=== Статус ==="
docker compose ps