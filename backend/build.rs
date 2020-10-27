use sqlx::postgres::PgDone;
use sqlx::postgres::PgPoolOptions;
use sqlx::PgPool;
use std::env;

pub type SqlResult<T> = Result<T, sqlx::Error>;

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

async fn init_db(db_url: &str) -> SqlResult<PgDone> {
    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect(&db_url)
        .await
        .unwrap();
    drop_account_table(&pool).await?;
    drop_movie_table(&pool).await?;
    create_account_table(&pool).await?;
    create_movie_table(&pool).await
}

fn main() {
    dotenv::dotenv().unwrap();
    let db_url = env::var("DATABASE_URL").expect("Environment variable DATABASE_URL is not set");
    tokio::runtime::Builder::new()
        .basic_scheduler()
        .enable_all()
        .build()
        .unwrap()
        .block_on(async { init_db(&db_url).await })
        .unwrap();
}
