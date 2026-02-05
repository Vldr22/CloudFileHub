#!/bin/bash
# Универсальный скрипт для управления проектом CloudFileHub

CUR_DIR=$(pwd)
PROJECT_DIR="/home/vldr2212/IdeaProjects/CloudFileHub"

CONTAINERS=("s3-file-service" "antivirus-service" "cloudfilehub-clamav")

while true; do
    echo
    echo "=== Меню проекта CloudFileHub ==="
    echo "1) Собрать проект (Maven + Docker)"
    echo "2) Поднять контейнеры (docker compose up -d)"
    echo "3) Проверить запущенные контейнеры (docker ps)"
    echo "4) Просмотреть логи контейнера"
    echo "0) Выйти"

    # shellcheck disable=SC2162
    read -r -p "Выберите действие [0-4]: " CHOICE

    case "$CHOICE" in
        0)
            echo "Выход..."
            break
            ;;
        1)
            echo "=== Сборка Maven проектов ==="
            cd "$PROJECT_DIR" || { echo "Не удалось перейти в папку проекта"; continue; }
            mvn clean package -DskipTests

            echo "=== Сборка Docker образов ==="
            docker compose build

            cd "$CUR_DIR" || exit
            ;;
        2)
            echo "=== Поднимаем контейнеры ==="
            docker compose up -d
            ;;
        3)
            echo "=== Список запущенных контейнеров ==="
            docker ps
            ;;
        4)
            echo
            echo "Выберите контейнер для просмотра логов:"
            for i in "${!CONTAINERS[@]}"; do
                echo "$((i+1))) ${CONTAINERS[$i]}"
            done
            echo "0) Назад в главное меню"

            read -r -p "Введите номер [0-${#CONTAINERS[@]}]: " LOG_CHOICE

            if [[ "$LOG_CHOICE" == "0" ]]; then
                continue
            elif [[ "$LOG_CHOICE" -ge 1 && "$LOG_CHOICE" -le "${#CONTAINERS[@]}" ]]; then
                SELECTED_CONTAINER="${CONTAINERS[$((LOG_CHOICE-1))]}"

                if docker ps -q -f name="$SELECTED_CONTAINER" | grep -q .; then
                    echo
                    echo "=== Просмотр логов контейнера: $SELECTED_CONTAINER ==="
                    echo "Нажмите Ctrl+C, чтобы вернуться в меню"
                    echo
                    docker logs -f "$SELECTED_CONTAINER"
                else
                    echo "Контейнер '$SELECTED_CONTAINER' не запущен!"
                fi
            else
                echo "Неверный ввод. Попробуйте снова."
            fi
            ;;
        *)
            echo "Неверный выбор. Попробуйте снова."
            ;;
    esac
done
