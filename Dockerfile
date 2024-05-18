FROM debian
ARG TS3_VER=3.5.6
WORKDIR /tmp
USER root

#Install required packages
RUN apt update
RUN apt install python3 pipx libxslt-dev pulseaudio mpv qt5ct default-jre openjfx libgtk-3-dev mpv-mpris git gnupg2 curl xvfb xauth libpci-dev wget sqlite3 -y

#Get key and source for spotify
RUN curl -sS https://download.spotify.com/debian/pubkey_6224F9941A8AA6D1.gpg | gpg --dearmor --yes -o /etc/apt/trusted.gpg.d/spotify.gpg
RUN echo "deb http://repository.spotify.com stable non-free" | tee /etc/apt/sources.list.d/spotify.list
RUN apt update

#Install spotify
RUN apt install spotify-client -y

#Install yt-dl
RUN pipx install yt-dlp
RUN pipx install youtube-dl

#Get and install teamspeak
RUN wget https://files.teamspeak-services.com/releases/client/$TS3_VER/TeamSpeak3-Client-linux_amd64-$TS3_VER.run -O TeamSpeak3-Client-linux_amd64.run
RUN chmod u+x TeamSpeak3-Client-linux_amd64.run
RUN yes | ./TeamSpeak3-Client-linux_amd64.run
RUN mv /tmp/TeamSpeak3-Client-linux_amd64 /usr/local
RUN chmod 777 /usr/local/TeamSpeak3-Client-linux_amd64
RUN rm -f TeamSpeak3-Client-linux_amd64.run

#Clone the musicbot repo
#RUN git clone https://gitlab.com/Bettehem/ts3-musicbot.git

COPY . ts3-musicbot

#Build the music bot
WORKDIR /tmp/ts3-musicbot
RUN ./build.sh
RUN mkdir -p out/artifacts/ts3_musicbot
RUN ./gradlew assemble "$@"
RUN cp build/libs/ts3-musicbot.jar /bin/

#Create the config directory for the volume
RUN mkdir /config

#Add user
RUN adduser bot
USER bot

#Create the teamspeak3 script and add it to path
WORKDIR $HOME
RUN mkdir $HOME/.local
RUN mkdir $HOME/.local/bin

USER root
RUN echo '#!/bin/sh\ncd /usr/local/TeamSpeak3-Client-linux_amd64\n ./ts3client_runscript.sh $@' > /usr/local/bin/teamspeak3
RUN chmod 777 /usr/local/bin/teamspeak3

#Copy the accept license script
COPY accept_ts3_license.sh .
RUN chmod 777 accept_ts3_license.sh

#Switch back to the user and run the accept license script
USER bot
RUN ./accept_ts3_license.sh

#Run the bot
CMD xvfb-run java --module-path /usr/share/openjfx/lib --add-modules javafx.controls -server -jar /bin/ts3-musicbot.jar --config /config/ts3-musicbot.config