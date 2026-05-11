use jirani_rust::auth::GatewayConfig;
use jirani_rust::models::{SanitizedReportPayload, SyncEnvelope};
use jirani_rust::store::EnvelopeStore;
use rocket::http::{ContentType, Status};
use rocket::local::blocking::Client;

#[test]
fn post_then_get_sync_envelope() {
    let client = open_client();
    let envelope = community_envelope();

    let response = client
        .post("/sync/envelopes")
        .header(ContentType::JSON)
        .body(serde_json::to_string(&envelope).expect("serialize envelope"))
        .dispatch();

    assert_eq!(response.status(), Status::Created);

    let response = client.get("/sync/envelopes").dispatch();
    assert_eq!(response.status(), Status::Ok);
    let body = response.into_string().expect("response body");
    assert!(body.contains("livestock or grazing dispute"));
    assert!(body.contains(&envelope.content_hash));
}

#[test]
fn duplicate_envelope_returns_conflict_for_android_dedupe() {
    let client = open_client();
    let envelope = community_envelope();
    let body = serde_json::to_string(&envelope).expect("serialize envelope");

    assert_eq!(
        client
            .post("/sync/envelopes")
            .header(ContentType::JSON)
            .body(body.clone())
            .dispatch()
            .status(),
        Status::Created,
    );
    assert_eq!(
        client
            .post("/sync/envelopes")
            .header(ContentType::JSON)
            .body(body)
            .dispatch()
            .status(),
        Status::Conflict,
    );
}

#[test]
fn survivor_centered_envelope_is_rejected() {
    let client = open_client();
    let mut envelope = community_envelope();
    envelope.audience_tier = "SurvivorSupportOnly".to_string();
    envelope.payload.sensitivity = "SurvivorCentered".to_string();
    envelope.content_hash = envelope.payload.content_hash();

    let response = client
        .post("/sync/envelopes")
        .header(ContentType::JSON)
        .body(serde_json::to_string(&envelope).expect("serialize envelope"))
        .dispatch();

    assert_eq!(response.status(), Status::BadRequest);
}

#[test]
fn mismatched_content_hash_is_rejected() {
    let client = open_client();
    let mut envelope = community_envelope();
    envelope.content_hash = "tampered".to_string();

    let response = client
        .post("/sync/envelopes")
        .header(ContentType::JSON)
        .body(serde_json::to_string(&envelope).expect("serialize envelope"))
        .dispatch();

    assert_eq!(response.status(), Status::BadRequest);
}

#[test]
fn dashboard_pages_render_in_open_demo_mode() {
    let client = open_client();

    let response = client.get("/").dispatch();
    assert_eq!(response.status(), Status::Ok);
    let body = response.into_string().expect("dashboard body");
    assert!(body.contains("Jirani report sync"));

    let response = client.get("/analysis").dispatch();
    assert_eq!(response.status(), Status::Ok);
    let body = response.into_string().expect("analysis body");
    assert!(body.contains("Anonymous analysis"));
}

#[test]
fn token_auth_blocks_sync_and_dashboard_when_enabled() {
    let client = Client::tracked(jirani_rust::rocket_with_config(GatewayConfig::with_token(
        "test-token",
    )))
    .expect("valid rocket instance");
    let envelope = community_envelope();

    assert_eq!(
        client
            .post("/sync/envelopes")
            .header(ContentType::JSON)
            .body(serde_json::to_string(&envelope).expect("serialize envelope"))
            .dispatch()
            .status(),
        Status::Unauthorized,
    );

    assert_eq!(client.get("/").dispatch().status(), Status::Ok);
    assert_eq!(
        client.get("/?token=test-token").dispatch().status(),
        Status::Ok
    );
    let access_page = client
        .get("/")
        .dispatch()
        .into_string()
        .expect("access page body");
    assert!(access_page.contains("Jirani gateway access"));
    assert_eq!(
        client
            .post("/sync/envelopes")
            .header(ContentType::JSON)
            .header(rocket::http::Header::new(
                "Authorization",
                "Bearer test-token",
            ))
            .body(serde_json::to_string(&envelope).expect("serialize envelope"))
            .dispatch()
            .status(),
        Status::Created,
    );
}

#[test]
fn file_backed_store_survives_restart_without_identity_metadata() {
    let dir = tempfile::tempdir().expect("temp dir");
    let path = dir.path().join("envelopes.json");
    let envelope = community_envelope();

    let client = Client::tracked(jirani_rust::rocket_with_store(
        GatewayConfig::open(),
        EnvelopeStore::from_path(path.clone()),
    ))
    .expect("valid rocket instance");
    assert_eq!(
        client
            .post("/sync/envelopes")
            .header(ContentType::JSON)
            .body(serde_json::to_string(&envelope).expect("serialize envelope"))
            .dispatch()
            .status(),
        Status::Created,
    );

    let stored = std::fs::read_to_string(&path).expect("persisted envelopes");
    assert!(stored.contains(&envelope.envelope_id));
    assert!(!stored.contains("127.0.0.1"));
    assert!(!stored.contains("User-Agent"));

    let restarted = Client::tracked(jirani_rust::rocket_with_store(
        GatewayConfig::open(),
        EnvelopeStore::from_path(path),
    ))
    .expect("valid rocket instance");
    let response = restarted.get("/sync/envelopes").dispatch();
    assert_eq!(response.status(), Status::Ok);
    assert!(response
        .into_string()
        .expect("body")
        .contains(&envelope.content_hash));
}

#[test]
fn privacy_endpoint_states_network_identity_is_not_stored() {
    let client = open_client();
    let response = client.get("/privacy").dispatch();

    assert_eq!(response.status(), Status::Ok);
    let body = response.into_string().expect("privacy body");
    assert!(body.contains("\"storedNetworkIdentity\":false"));
    assert!(body.contains("direct HTTPS request exposes"));
}

fn open_client() -> Client {
    Client::tracked(jirani_rust::rocket_with_config(GatewayConfig::open()))
        .expect("valid rocket instance")
}

fn community_envelope() -> SyncEnvelope {
    let payload = SanitizedReportPayload {
        report_type: "livestock or grazing dispute".to_string(),
        general_area: "near river".to_string(),
        time_window: "morning".to_string(),
        submitted_at_epoch_seconds: 1_800_000_000,
        observed_risk: "Cattle crossed the grazing boundary this morning.".to_string(),
        verification_status: "PendingVerification".to_string(),
        sensitivity: "Community".to_string(),
    };

    SyncEnvelope {
        envelope_id: "env-demo-community".to_string(),
        record_type: "SafetyReportRecord".to_string(),
        record_id: "report-demo-community".to_string(),
        content_hash: payload.content_hash(),
        version: 1,
        last_modified_bucket: "day-20833".to_string(),
        audience_tier: "TrustedVerifier".to_string(),
        expires_at_epoch_seconds: 1_900_000_000,
        payload,
    }
}
