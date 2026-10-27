CREATE TABLE app_users
(
    id       INT AUTO_INCREMENT NOT NULL,
    username VARCHAR(100) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT pk_app_users PRIMARY KEY (id)
);

CREATE TABLE campaign_invitations
(
    id              INT AUTO_INCREMENT NOT NULL,
    campaign_id     INT NOT NULL,
    invited_by_id   INT NOT NULL,
    invited_user_id INT NOT NULL,
    `role`          VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    expires_at      DATETIME(6) NOT NULL,
    CONSTRAINT pk_campaign_invitations PRIMARY KEY (id)
);

CREATE TABLE campaign_participants
(
    id          INT AUTO_INCREMENT NOT NULL,
    campaign_id INT NOT NULL,
    user_id     INT NOT NULL,
    `role`      VARCHAR(20) NOT NULL,
    CONSTRAINT pk_campaign_participants PRIMARY KEY (id)
);

CREATE TABLE campaigns
(
    id          INT AUTO_INCREMENT NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    CONSTRAINT pk_campaigns PRIMARY KEY (id)
);

ALTER TABLE app_users
    ADD CONSTRAINT uc_app_users_username UNIQUE (username);

ALTER TABLE campaign_participants
    ADD CONSTRAINT uk_participants_campaign_user UNIQUE (campaign_id, user_id);

ALTER TABLE campaign_invitations
    ADD CONSTRAINT FK_CAMPAIGN_INVITATIONS_ON_CAMPAIGN FOREIGN KEY (campaign_id) REFERENCES campaigns (id);

ALTER TABLE campaign_invitations
    ADD CONSTRAINT FK_CAMPAIGN_INVITATIONS_ON_INVITED_BY FOREIGN KEY (invited_by_id) REFERENCES app_users (id);

ALTER TABLE campaign_invitations
    ADD CONSTRAINT FK_CAMPAIGN_INVITATIONS_ON_INVITED_USER FOREIGN KEY (invited_user_id) REFERENCES app_users (id);

ALTER TABLE campaign_participants
    ADD CONSTRAINT FK_CAMPAIGN_PARTICIPANTS_ON_CAMPAIGN FOREIGN KEY (campaign_id) REFERENCES campaigns (id);

ALTER TABLE campaign_participants
    ADD CONSTRAINT FK_CAMPAIGN_PARTICIPANTS_ON_USER FOREIGN KEY (user_id) REFERENCES app_users (id);