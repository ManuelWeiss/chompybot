# Consumers schema

# --- !Ups

CREATE TABLE Consumers (
    id              bigint(20)   NOT NULL AUTO_INCREMENT,
    oauthId         varchar(255) NOT NULL,
    oauthSecret     varchar(255) NOT NULL,
    capabilitiesUrl varchar(255) NOT NULL,
    roomId          bigint(20)   NOT NULL,
    groupId         bigint(20)   NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO Consumers (oauthId, oauthSecret, capabilitiesUrl, roomId, groupId)
    VALUES (
        '8534e74b-8b8d-4270-812d-73bff87e5d28',
        'IAuNDb5Tu2tp9BYmA12hQ53wdqtRCim4mnFQx7IJ',
        'https://api.hipchat.com/v2/capabilities',
        546167,
        35222
    );

# --- !Downs

DROP TABLE Consumers;
