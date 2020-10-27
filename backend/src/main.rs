mod db;

#[macro_use]
extern crate rocket;
use rocket::http::Header;
use rocket::response::status::NotFound;
use rocket::State;
use sqlx::postgres::PgPoolOptions;
use sqlx::PgPool;
use std::env;


#[derive(Responder)]
#[response(status = 200)]
struct MyResponder {
    inner: String,
    cors: Header<'static>,
}

impl MyResponder {
    fn new(inner: impl Into<String>) -> Self {
        MyResponder {
            inner: inner.into(),
            cors: Header::new("Access-Control-Allow-Origin", "http://localhost:8080"),
        }
    }
}

#[get("/")]
async fn index<'a>() -> MyResponder {
    MyResponder::new("Hello")
}

#[get("/votes")]
async fn votes(pool: State<'_, PgPool>) -> Result<MyResponder, NotFound<&str>> {
    let result = sqlx::query_as::<_, db::Account>("SELECT * FROM users")
        .fetch_one(pool.inner())
        .await;

    match result {
        Ok(row) => Ok(MyResponder::new(row.name)),
        Err(_) => Err(NotFound("Unable to find user")),
    }
}

async fn init_db() -> anyhow::Result<PgPool> {
    dotenv::dotenv()?;
    let db_url = env::var("DATABASE_URL").expect("Environment variable DATABASE_URL is not set");
    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect(&db_url)
        .await?;

    db::create_account(&pool, "Andreas Arvidsson").await?;

    Ok(pool)
}

#[launch]
async fn rocket() -> rocket::Rocket {
    let pool = init_db().await.expect("Unable to initialize database");
    rocket::ignite()
        .manage(pool)
        .mount("/", routes![index, votes])
}
