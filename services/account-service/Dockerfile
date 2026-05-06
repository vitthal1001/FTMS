FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S neobankx && adduser -S neobankx -G neobankx
COPY services/account-service/build/libs/account-service-*.jar app.jar
USER neobankx
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget --spider -q http://localhost:8082/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

