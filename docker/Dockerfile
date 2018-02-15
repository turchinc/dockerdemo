FROM stefanscherer/openjdk-windows:9-nanoserver-1709
# context must be project root, so run with docker build -f .\docker\Dockerfile .
# copy application JAR (with libraries inside)
COPY target/restserver-swarm.jar /app.jar
# specify default command
CMD ["java", "-jar",  "/app.jar"]
# run with docker run -p 8080:8080 <containerid> 