FROM python:3

COPY . .

RUN apt update

RUN apt install -y maven

RUN mvn clean install

RUN pip install opencv-python

RUN apt install -y ffmpeg

EXPOSE 8082

CMD java -jar target/Node-1.0-SNAPSHOT.war
