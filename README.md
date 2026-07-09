# 1. Copiar la plantilla y completar valores reales
cp .env.example .env
# Editar .env con tus valores (especialmente JWT_SECRET generado con: openssl rand -base64 32)

# 2. Levantar todo
docker-compose up --build

# La API queda disponible en http://localhost:8080


Archivo  .env 

DB_NAME=restaurante_db
DB_USER=restaurante_user
DB_PASSWORD=123456
POSTGRES_PORT=5432

SERVER_PORT=8080

JWT_SECRET=genera-esto-con-el-comando-de-abajo
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
