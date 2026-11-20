#!/bin/bash

# Адрес Spring Boot приложения
BASE_URL="http://localhost:8080"

echo "======================================"
echo "        API CRUD TESTING"
echo "======================================"

# Проверка USERS
echo -e "\n========== USERS =========="

echo -e "\n[GET] Все пользователи"
curl -i "$BASE_URL/users"

echo -e "\n[POST] Создание пользователя"
curl -i -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com"}'

echo -e "\n[GET] Пользователь с ID=1"
curl -i "$BASE_URL/users/1"

echo -e "\n[PATCH] Обновление пользователя ID=1"
curl -i -X PATCH "$BASE_URL/users/1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated User","email":"updated@example.com"}'

echo -e "\n[DELETE] Удаление пользователя ID=1"
curl -i -X DELETE "$BASE_URL/users/1"

# Проверка ITEMS
echo -e "\n========== ITEMS =========="

echo -e "\n[GET] Все товары"
curl -i "$BASE_URL/items"

echo -e "\n[POST] Создание товара"
curl -i -X POST "$BASE_URL/items" \
  -H "Content-Type: application/json" \
  -d '{"user_id":"1","about":"Test item","cndn":"Good"}'

echo -e "\n[GET] Товар с ID=1"
curl -i "$BASE_URL/items/1"

echo -e "\n[PATCH] Обновление товара ID=1"
curl -i -X PATCH "$BASE_URL/items/1" \
  -H "Content-Type: application/json" \
  -d '{"user_id":"1","about":"Updated item","cndn":"Excellent"}'

echo -e "\n[DELETE] Удаление товара ID=1"
curl -i -X DELETE "$BASE_URL/items/1"

# Проверка LOTS
echo -e "\n========== LOTS =========="

echo -e "\n[GET] Все лоты"
curl -i "$BASE_URL/lots"

echo -e "\n[POST] Создание лота"
curl -i -X POST "$BASE_URL/lots" \
  -H "Content-Type: application/json" \
  -d '{"item_id":"1","price":"1000","step":"100","date":"2026-12-01","status":"active"}'

echo -e "\n[GET] Лот с ID=1"
curl -i "$BASE_URL/lots/1"

echo -e "\n[PATCH] Обновление лота ID=1"
curl -i -X PATCH "$BASE_URL/lots/1" \
  -H "Content-Type: application/json" \
  -d '{"item_id":"1","price":"1500","step":"200","date":"2026-12-02","status":"updated"}'

echo -e "\n[DELETE] Удаление лота ID=1"
curl -i -X DELETE "$BASE_URL/lots/1"

# Проверка BIRDS
echo -e "\n========== BIRDS =========="

echo -e "\n[GET] Все заявки"
curl -i "$BASE_URL/birds"

echo -e "\n[POST] Создание заявки"
curl -i -X POST "$BASE_URL/birds" \
  -H "Content-Type: application/json" \
  -d '{"lot_id":"1","user_id":"1","price":"1200","date":"2026-12-01"}'

echo -e "\n[GET] Заявка с ID=1"
curl -i "$BASE_URL/birds/1"

echo -e "\n[PATCH] Обновление заявки ID=1"
curl -i -X PATCH "$BASE_URL/birds/1" \
  -H "Content-Type: application/json" \
  -d '{"lot_id":"1","user_id":"1","price":"1300","date":"2026-12-02"}'

echo -e "\n[DELETE] Удаление заявки ID=1"
curl -i -X DELETE "$BASE_URL/birds/1"

# Проверка DEALS
echo -e "\n========== DEALS =========="

echo -e "\n[GET] Все сделки"
curl -i "$BASE_URL/deals"

echo -e "\n[POST] Создание сделки"
curl -i -X POST "$BASE_URL/deals" \
  -H "Content-Type: application/json" \
  -d '{"lot_id":"1","bird_id":"1","price":"1300","status":"completed"}'

echo -e "\n[GET] Сделка с ID=1"
curl -i "$BASE_URL/deals/1"

echo -e "\n[PATCH] Обновление сделки ID=1"
curl -i -X PATCH "$BASE_URL/deals/1" \
  -H "Content-Type: application/json" \
  -d '{"lot_id":"1","bird_id":"1","price":"1500","status":"updated"}'

echo -e "\n[DELETE] Удаление сделки ID=1"
curl -i -X DELETE "$BASE_URL/deals/1"

echo -e "\n======================================"
echo "          TESTING FINISHED"
echo "======================================"
