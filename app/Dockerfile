FROM openjdk:8-jre-alpine
RUN addgroup -S callers && adduser -S seriouscallersonly -G callers
USER seriouscallersonly
ARG DEPENDENCY=build/libs
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ARG MAIN_CLASS
ENV MAIN_CLASS_VAR=${MAIN_CLASS}
ENV SPRING_CONFIG_LOCATION=classpath:/application.yaml
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -noverify -cp app:app/lib/* ${MAIN_CLASS_VAR} ${@}"]