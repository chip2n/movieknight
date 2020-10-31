#[macro_use]
extern crate rocket;
use movieknight_db as db;
use rocket::http::Header;
use rocket::response::status::NotFound;
use rocket::State;
use sqlx::PgPool;

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

#[launch]
async fn rocket() -> rocket::Rocket {
    let pool = db::init().await.expect("Unable to initialize database");
    db::create_account(&pool, "Andreas Arvidsson")
        .await
        .unwrap();
    rocket::ignite()
        .manage(pool)
        .mount("/", routes![index, votes])
}

#[cfg(test)]
mod tests {
    use super::*;
    use rocket::http::Status;
    use rocket::local::blocking::Client;

    fn init_client() -> Client {
        let rocket = tokio::runtime::Builder::new()
            .basic_scheduler()
            .enable_all()
            .build()
            .unwrap()
            .block_on(async { rocket().await });

        Client::tracked(rocket).expect("valid rocket instance")
    }

    #[test]
    fn test_api() {
        let client = init_client();
        let req = client.get("/");
        let response = req.dispatch();

        assert_eq!(response.status(), Status::Ok);
    }
}
