CREATE TABLE shortened_url (
  id VARCHAR(8) NOT NULL,
  url TEXT NOT NULL,
  user_id TEXT NOT NULL,
  view_count INT NOT NULL,
  save_count INT NOT NULL,

  PRIMARY KEY(id)
);

CREATE INDEX url_idx ON shortened_url(user_id, url);
CREATE INDEX user_id_idx ON shortened_url(user_id);