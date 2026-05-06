FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S neobankx && adduser -S neobankx -G neobankx
COPY services/api-gateway/build/libs/api-gateway-*.jar app.jar
USER neobankx
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget --spider -q http://localhost:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
