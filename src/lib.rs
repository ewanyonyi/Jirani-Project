pub mod auth;
pub mod models;
pub mod routes;
pub mod store;

pub fn rocket() -> rocket::Rocket<rocket::Build> {
    rocket_with_config(auth::GatewayConfig::from_env())
}

pub fn rocket_with_config(config: auth::GatewayConfig) -> rocket::Rocket<rocket::Build> {
    rocket::build()
        .manage(config)
        .manage(store::EnvelopeStore::default())
        .mount("/", routes::routes())
}

pub fn rocket_with_store(
    config: auth::GatewayConfig,
    store: store::EnvelopeStore,
) -> rocket::Rocket<rocket::Build> {
    rocket::build()
        .manage(config)
        .manage(store)
        .mount("/", routes::routes())
}
