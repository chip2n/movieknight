use movieknight_db as db;

async fn init_db() -> Result<(), anyhow::Error> {
    let pool = db::init()
        .await
        .unwrap();
    db::drop_account_table(&pool).await?;
    db::drop_movie_table(&pool).await?;
    db::create_account_table(&pool).await?;
    db::create_movie_table(&pool).await?;
    Ok(())
}

fn main() {
    tokio::runtime::Builder::new()
        .basic_scheduler()
        .enable_all()
        .build()
        .unwrap()
        .block_on(init_db())
        .unwrap();
}
