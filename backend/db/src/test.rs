use crate::migrations;
use hex::encode;
use rand::{thread_rng, RngCore};
use sqlx::{Connection, PgConnection, PgPool};
use std::panic;

/// Executes a test with a database connection. Prepares a new test database that is cleaned up after the test.
/// Configure the TEST_DATABASE_CONNECTION in your .env file. The user needs to have access to the postgres database
/// and have the permission to create / delete databases.
pub fn asynchronous<F, R>(f: F)
where
    F: FnOnce(String) -> R + std::panic::UnwindSafe,
    R: std::future::Future<Output = Result<(), anyhow::Error>>,
{
    let result = synchronous(|db_url| {
        tokio::runtime::Builder::new()
            .basic_scheduler()
            .enable_all()
            .build()?
            .block_on(f(db_url.to_string()))?;
        Ok(())
    });
    assert!(result.is_ok(), "Test failed during execution");
}

/// Executes a test with a database connection. Prepares a new test database that is cleaned up after the test.
/// Configure the TEST_DATABASE_CONNECTION in your .env file. The user needs to have access to the postgres database
/// and have the permission to create / delete databases.
pub fn synchronous<'a, F>(test: F) -> Result<(), anyhow::Error>
where
    F: FnOnce(&str) -> Result<(), anyhow::Error> + panic::UnwindSafe,
{
    let _ = dotenv::dotenv();
    let db_url = &dotenv::var("TEST_DATABASE_URL")?;

    let mut runtime = tokio::runtime::Builder::new()
        .basic_scheduler()
        .enable_all()
        .build()
        .unwrap();

    let db_name = runtime.block_on(setup_db(db_url)).unwrap();
    let db_string = format!("{}/{}", db_url, db_name);

    let result = panic::catch_unwind(|| test(&db_string));

    runtime.block_on(teardown_db(db_url, &db_name))?;

    assert!(result.is_ok()); // did a panic occur?
    result.unwrap() // did the test function return Err?
}

/// Creates a randomly named test database.
async fn setup_db(db_url: &str) -> Result<String, anyhow::Error> {
    let mut random = vec![0u8; 28];
    thread_rng().fill_bytes(random.as_mut_slice());
    let db_name: String = format!("movieknight_test_{}", encode(random));

    let pool: PgPool = sqlx::postgres::PgPoolOptions::new()
        .max_connections(1)
        .connect(&format!("{}/postgres", db_url))
        .await?;

    sqlx::query(format!("CREATE DATABASE {};", db_name).as_ref())
        .execute(&pool)
        .await?;

    let pool: PgPool = sqlx::postgres::PgPoolOptions::new()
        .max_connections(1)
        .connect(&format!("{}/{}", db_url, db_name))
        .await?;

    migrations::run(&pool).await?;

    Ok(db_name)
}

/// Deletes the randomly named test database.
async fn teardown_db(db_url: &str, db_name: &str) -> Result<(), anyhow::Error> {
    let mut conn = PgConnection::connect(&format!("{}/postgres", db_url)).await?;

    // Drop all other connections to the database
    sqlx::query(
        format!(
            r#"SELECT pg_terminate_backend(pg_stat_activity.pid)
                   FROM pg_stat_activity
                   WHERE datname = '{}'
                   AND pid <> pg_backend_pid();"#,
            db_name
        )
        .as_ref(),
    )
    .execute(&mut conn)
    .await?;

    // Drop the database itself
    sqlx::query(format!("DROP DATABASE {}", db_name).as_ref())
        .execute(&mut conn)
        .await?;

    Ok(())
}
