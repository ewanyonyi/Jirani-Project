mod auth;
mod models;
mod routes;
mod store;

#[rocket::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let store = store::GatewayStore::from_env().await?;

    rocket::build()
        .manage(auth::GatewayConfig::from_env())
        .manage(store)
        .mount("/", routes::routes())
        .launch()
        .await?;

    Ok(())
}
