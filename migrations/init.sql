CREATE TABLE IF NOT EXISTS movie (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR,
  synopsis VARCHAR,
  image_url VARCHAR
);
--;;
CREATE TABLE IF NOT EXISTS account (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR
);
--;;
CREATE TABLE IF NOT EXISTS vote (
  PRIMARY KEY(user_id, movie_id),
  user_id BIGSERIAL REFERENCES account (id),
  movie_id BIGSERIAL REFERENCES movie (id),
  answer BOOLEAN
);
