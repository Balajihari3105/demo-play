FROM openjdk:17-jdk-slim

# Install prerequisites and SBT
RUN apt-get update && \
    apt-get install -y curl gnupg && \
    mkdir -p /etc/apt/sources.list.d && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add - && \
    apt-get update && \
    apt-get install -y sbt && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY build.sbt /app/
COPY project/ /app/project/
RUN sbt update
COPY . /app
RUN sbt compile stage
EXPOSE 9000
CMD ["target/universal/stage/bin/demo-play", "-Dhttp.port=9000","-Dplay.http.secret.key=xmPBt/K4lJEyblScF/TWkjWDDygWHGN9E0MBvg3Cf6c="]