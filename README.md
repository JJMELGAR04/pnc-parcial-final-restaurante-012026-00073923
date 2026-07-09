# 1. Copiar la plantilla y completar valores reales
cp .env.example .env
# Editar .env con tus valores (especialmente JWT_SECRET generado con: openssl rand -base64 32)

# 2. Levantar todo
docker-compose up --build

# La API queda disponible en http://localhost:8080