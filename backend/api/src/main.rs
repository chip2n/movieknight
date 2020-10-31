#[macro_use]
extern crate rocket;
use movieknight_db as db;
use rocket::http::Header;
use rocket::response::status::NotFound;
use rocket::State;
use rocket_contrib::json::Json;
use serde::{Deserialize, Serialize};
use sqlx::PgPool;

#[derive(Responder)]
#[response(status = 200)]
struct MyResponder {
    inner: String,
    cors: Header<'static>,
}

impl MyResponder {
    fn new(inner: impl Into<String>) -> Self {
        Self {
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

#[derive(Serialize, Deserialize, Debug)]
pub struct Movie {
    pub id: i64,
    pub title: String,
    pub synopsis: String,
    pub image_url: String,
}

#[derive(Responder)]
#[response(status = 200)]
struct GetMoviesResponder {
    inner: Json<Vec<Movie>>,
    cors: Header<'static>,
}

impl GetMoviesResponder {
    fn new(inner: Vec<Movie>) -> Self {
        Self {
            inner: Json(inner.into()),
            cors: Header::new("Access-Control-Allow-Origin", "http://localhost:8080"),
        }
    }
}

impl From<db::Movie> for Movie {
    fn from(movie: db::Movie) -> Self {
        Movie {
            id: movie.id,
            title: movie.title,
            synopsis: movie.synopsis,
            image_url: movie.image_url,
        }
    }
}

#[get("/movies")]
async fn movies(pool: State<'_, PgPool>) -> GetMoviesResponder {
    let movies = db::get_movies(pool.inner())
        .await
        .expect("Unable to get movies from database")
        .into_iter()
        .map(Into::into)
        .collect();

    GetMoviesResponder::new(movies)
}

#[launch]
async fn rocket() -> rocket::Rocket {
    let pool = db::init().await.expect("Unable to initialize database");
    db::create_account(&pool, "Andreas Arvidsson")
        .await
        .unwrap();
    rocket::ignite()
        .manage(pool)
        .mount("/", routes![index, votes, movies])
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
            .block_on(rocket());

        Client::tracked(rocket).expect("valid rocket instance")
    }

    #[test]
    fn test_get_movies() {
        let client = init_client();
        let response = client.get("/movies").dispatch();

        assert_eq!(response.status(), Status::Ok);

        let body = response.into_string().unwrap();
        let movies: Vec<Movie> = serde_json::from_str(&body).unwrap();
        assert!(movies.is_empty());
    }
}
