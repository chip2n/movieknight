use std::fmt;

use serde::Deserialize;

#[derive(Deserialize)]
struct JikanResponse<T> {
    request_hash: String,
    request_cached: bool,
    request_cache_expiry: u32,
    results: Vec<T>,
}

#[derive(Deserialize)]
pub struct JikanSearchResult {
    pub mal_id: u32,
    pub url: String,
    pub image_url: String,
    pub title: String,
    pub airing: bool,
    pub synopsis: String,
    #[serde(rename(deserialize = "type"))]
    pub media_type: String,
    pub episodes: u32,
    pub score: f32,
    pub start_date: String,
    pub end_date: String,
    pub members: u32,
    pub rated: String,
}

#[derive(Debug)]
pub enum JikanError {
    ApiRequestFailed(reqwest::Error),
    JsonParsingFailed(serde_json::Error),
}

impl From<reqwest::Error> for JikanError {
    fn from(error: reqwest::Error) -> Self {
        JikanError::ApiRequestFailed(error)
    }
}

impl From<serde_json::Error> for JikanError {
    fn from(error: serde_json::Error) -> Self {
        JikanError::JsonParsingFailed(error)
    }
}

impl fmt::Display for JikanError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let msg = match self {
            JikanError::ApiRequestFailed(_) => "Failed during request to Jikan API",
            JikanError::JsonParsingFailed(_) => "Failed to parse JSON from Jikan API response",
        };
        write!(f, "{}", msg)
    }
}

impl std::error::Error for JikanError {
    fn cause(&self) -> Option<&dyn std::error::Error> {
        Some(match *self {
            JikanError::ApiRequestFailed(ref err) => err as &dyn std::error::Error,
            JikanError::JsonParsingFailed(ref err) => err as &dyn std::error::Error,
        })
    }
}

pub async fn search(title: &str) -> Result<Vec<JikanSearchResult>, JikanError> {
    let body = reqwest::get("https://pi.jikan.moe/v3/search/anime?q=Naruto&limit=3")
        .await?
        .text()
        .await?;

    let response: JikanResponse<JikanSearchResult> = serde_json::from_str(&body)?;
    Ok(response.results)
}
