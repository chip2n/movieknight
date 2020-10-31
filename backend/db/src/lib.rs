use sqlx::postgres::PgDone;
use sqlx::postgres::PgPoolOptions;
use sqlx::PgPool;
use std::env;
use sqlx::Executor;
use sqlx::Postgres;

pub type SqlResult<T> = Result<T, sqlx::Error>;

pub trait PgExecutor<'c>: Executor<'c, Database = Postgres> {}
impl<'c, U> PgExecutor<'c> for U where U: Executor<'c, Database = Postgres> {}

#[derive(sqlx::FromRow)]
pub struct Account {
    pub id: i64,
    pub name: String,
}

#[derive(sqlx::FromRow)]
pub struct Movie {
    pub id: i64,
    pub title: String,
    pub synopsis: String,
    pub image_url: String,
}

#[derive(sqlx::FromRow)]
pub struct Vote {
    pub id: i32,
    pub user_id: i32,
    pub movie_id: i32,
}

pub async fn init() -> anyhow::Result<PgPool> {
    dotenv::dotenv()?;
    let db_url = env::var("DATABASE_URL").expect("Environment variable DATABASE_URL is not set");
    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect(&db_url)
        .await?;

    Ok(pool)
}

pub async fn create_movie_table(pool: &PgPool) -> SqlResult<PgDone> {
    sqlx::query!(
        "
CREATE TABLE IF NOT EXISTS movie (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR,
  synopsis VARCHAR,
  image_url VARCHAR
)
",
    )
    .execute(pool)
    .await
}

pub async fn create_account_table(pool: &PgPool) -> SqlResult<PgDone> {
    sqlx::query!(
        "
CREATE TABLE IF NOT EXISTS account (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR
)
",
    )
    .execute(pool)
    .await
}

pub async fn drop_account_table(pool: &PgPool) -> SqlResult<PgDone> {
    sqlx::query!("DROP TABLE IF EXISTS account")
        .execute(pool)
        .await
}

pub async fn drop_movie_table(pool: &PgPool) -> SqlResult<PgDone> {
    sqlx::query!("DROP TABLE IF EXISTS movie")
        .execute(pool)
        .await
}

pub async fn create_account<'c, E>(pool: E, name: &str) -> SqlResult<PgDone>
where
    E: PgExecutor<'c>,
{
    sqlx::query!("INSERT INTO account (name) VALUES ($1)", name)
        .execute(pool)
        .await
}

pub async fn insert_movie<'c, E>(pool: E, movie: &Movie) -> SqlResult<PgDone>
where
    E: PgExecutor<'c>,
{
    sqlx::query!(
        "INSERT INTO movie (title, synopsis, image_url) VALUES ($1, $2, $3)",
        movie.title,
        movie.synopsis,
        movie.image_url
    )
    .execute(pool)
    .await
}

pub async fn get_movies<'c, E>(pool: E) -> SqlResult<Vec<Movie>>
where
    E: PgExecutor<'c>,
{
    sqlx::query_as("SELECT * FROM movie").fetch_all(pool).await
}
