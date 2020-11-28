#![feature(async_closure)]

mod jikan;

#[macro_use]
extern crate rocket;
use async_trait::async_trait;
use movieknight_db as db;
use rocket::http::Header;
use rocket::http::RawStr;
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

#[get("/search?<query>")]
async fn search(
    query: &RawStr,
    searcher: State<'_, Box<dyn MovieSearcher>>,
) -> Result<GetMoviesResponder, String> {
    let result = searcher.search(query).await;
    match result {
        Ok(movies) => Ok(GetMoviesResponder::new(movies)),
        Err(e) => {
            // TODO better logging
            // TODO respond with json detailing error
            // TODO use correct status code (500)
            eprintln!("{}", e);
            Err(format!("Failed to execute search: {}", e))
        }
    }
}

#[async_trait]
trait MovieSearcher: Send + Sync {
    async fn search(&self, title: &str) -> anyhow::Result<Vec<Movie>>;
}

struct JikanMovieSearcher {}

#[async_trait]
impl MovieSearcher for JikanMovieSearcher {
    async fn search(&self, title: &str) -> anyhow::Result<Vec<Movie>> {
        let results = jikan::search(title).await?;
        let movies = results
            .into_iter()
            .map(|r| Movie {
                id: 0,
                title: r.title,
                synopsis: r.synopsis,
                image_url: r.image_url,
            })
            .collect::<Vec<Movie>>();
        Ok(movies)
    }
}

async fn launch_server(pool: PgPool, searcher: impl MovieSearcher + 'static) -> rocket::Rocket {
    rocket::ignite()
        .manage(pool)
        .manage(Box::new(searcher) as Box<dyn MovieSearcher>)
        .mount("/", routes![index, votes, movies, search])
}

#[launch]
async fn rocket() -> rocket::Rocket {
    let pool = db::init().await.expect("Unable to initialize database");
    let searcher = JikanMovieSearcher {};
    launch_server(pool, searcher).await
}

#[cfg(test)]
mod tests {
    use super::*;
    use rocket::http::Status;
    use rocket::local::asynchronous::Client;

    struct FakeMovieSearcher {
        has_results: bool,
    }

    #[async_trait]
    impl MovieSearcher for FakeMovieSearcher {
        async fn search(&self, title: &str) -> anyhow::Result<Vec<Movie>> {
            let movies = if self.has_results {
                vec![Movie {
                    id: 0,
                    title: "Hello".to_string(),
                    synopsis: "Synopsis".to_string(),
                    image_url: "".to_string(),
                }]
            } else {
                vec![]
            };
            Ok(movies)
        }
    }

    async fn init_client(pool: PgPool) -> anyhow::Result<Client> {
        let searcher = FakeMovieSearcher { has_results: false };
        let rocket = launch_server(pool, searcher).await;
        let client = Client::tracked(rocket).await?;
        Ok(client)
    }

    #[test]
    fn test_get_movies_empty() {
        db::test::asynchronous(async move |db_url| {
            let pool = db::init_pool(&db_url, 1).await?;
            let client = init_client(pool).await?;
            let response = client.get("/movies").dispatch().await;

            assert_eq!(response.status(), Status::Ok);

            let body = response.into_string().await.unwrap();
            let movies: Vec<Movie> = serde_json::from_str(&body)?;
            assert!(movies.is_empty());
            Ok(())
        });
    }

    #[test]
    fn test_get_movies_nonempty() {
        db::test::asynchronous(async move |db_url| {
            let pool = db::init_pool(&db_url, 1).await?;
            insert_dummy_movie(&pool, 0, "Test").await?;

            let client = init_client(pool).await?;
            let response = client.get("/movies").dispatch().await;

            assert_eq!(response.status(), Status::Ok);

            let body = response.into_string().await.unwrap();
            let movies: Vec<Movie> = serde_json::from_str(&body)?;
            assert!(movies.len() == 1);
            Ok(())
        });
    }

    #[test]
    fn test_search_no_match() {
        db::test::asynchronous(async move |db_url| {
            let pool = db::init_pool(&db_url, 1).await?;
            let searcher = FakeMovieSearcher { has_results: false };
            let rocket = launch_server(pool, searcher).await;
            let client = Client::tracked(rocket).await?;
            let response = client.get("/search?query=Test").dispatch().await;

            assert_eq!(response.status(), Status::Ok);

            let body = response.into_string().await.unwrap();
            let movies: Vec<Movie> = serde_json::from_str(&body)?;
            assert!(movies.is_empty());
            Ok(())
        });
    }

    #[test]
    fn test_search_match() {
        db::test::asynchronous(async move |db_url| {
            let pool = db::init_pool(&db_url, 1).await?;
            let searcher = FakeMovieSearcher { has_results: true };
            let rocket = launch_server(pool, searcher).await;
            let client = Client::tracked(rocket).await?;
            let response = client.get("/search?query=Test").dispatch().await;

            assert_eq!(response.status(), Status::Ok);

            let body = response.into_string().await.unwrap();
            let movies: Vec<Movie> = serde_json::from_str(&body)?;
            assert!(movies.len() == 1);
            Ok(())
        });
    }

    async fn insert_dummy_movie(pool: &PgPool, id: i64, title: &str) -> anyhow::Result<()> {
        db::insert_movie(
            pool,
            &db::Movie {
                id,
                title: title.to_string(),
                synopsis: "Test synopsis".to_string(),
                image_url: "http://example.com".to_string(),
            },
        )
        .await?;
        Ok(())
    }
}
