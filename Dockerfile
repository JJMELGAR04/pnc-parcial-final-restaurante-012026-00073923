# ---------- Etapa 1: build ----------
FROM gradle:8.11-jdk21-alpine AS build

WORKDIR /app

# Copiamos solo los archivos de configuracion de Gradle primero.
# Esto permite que Docker cachee la descarga de dependencias en capas separadas:
# si solo cambia el codigo fuente (no las dependencias), este paso no se repite.
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

RUN ./gradlew dependencies --no-daemon || true

COPY src ./src

RUN ./gradlew build -x test --no-daemon

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuario no-root: si el contenedor es comprometido, el atacante no tiene
# privilegios de root dentro del contenedor. Buena practica estandar de
# seguridad en contenedores, aunque no lo pida explicitamente el enunciado.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/build/libs/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]