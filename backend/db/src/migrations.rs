use sqlx::PgPool;
use sqlx::postgres::PgDone;
use crate::SqlResult;
use crate::create_account_table;
use crate::create_movie_table;

pub async fn run(pool: &PgPool) -> SqlResult<PgDone> {
    create_account_table(pool).await?;
    create_movie_table(pool).await
}
