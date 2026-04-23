# ===== Stage 1: Build =====
FROM node:22.22.2-bookworm AS frontend-builder
WORKDIR /app/prms-web

# Copy package files trước để tận dụng cache
COPY prms-web/package*.json ./
RUN npm ci

# Copy source frontend và build
COPY prms-web/ ./
RUN npm run build

# ===== Stage 2: Build backend with Maven =====
FROM maven:3.9.9-eclipse-temurin-17 AS backend-builder
WORKDIR /app

# Copy toàn bộ source backend
COPY . .

# Copy frontend build vào đúng chỗ Spring Boot sẽ serve static
# Angular mới thường output nằm trong dist/<app-name>/browser
RUN mkdir -p src/main/resources/static
COPY --from=frontend-builder /app/prms-web/dist/ ./target/frontend-dist/

# Tự copy thư mục browser đầu tiên tìm được vào static
RUN sh -c 'BROWSER_DIR=$(find /app/target/frontend-dist -type d -name browser | head -n 1) && \
    if [ -z "$BROWSER_DIR" ]; then echo "Cannot find Angular browser build output"; exit 1; fi && \
    cp -r "$BROWSER_DIR"/. src/main/resources/static/'

# Build jar, bỏ qua frontend Maven plugin vì frontend đã build ở stage riêng
RUN mvn clean package -DskipTests -Dfrontend.skip=true

# ===== Stage 3: Run =====
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=backend-builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]