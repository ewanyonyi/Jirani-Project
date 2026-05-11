use rocket::serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};

#[derive(Debug, Clone, Deserialize, Serialize, PartialEq, Eq)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct SyncEnvelope {
    pub envelope_id: String,
    pub record_type: String,
    pub record_id: String,
    pub content_hash: String,
    pub version: u32,
    pub last_modified_bucket: String,
    pub audience_tier: String,
    pub expires_at_epoch_seconds: i64,
    pub payload: SanitizedReportPayload,
}

#[derive(Debug, Clone, Deserialize, Serialize, PartialEq, Eq)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct SanitizedReportPayload {
    pub report_type: String,
    pub general_area: String,
    pub time_window: String,
    pub submitted_at_epoch_seconds: i64,
    pub observed_risk: String,
    pub verification_status: String,
    pub sensitivity: String,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct EnvelopeList {
    pub envelopes: Vec<SyncEnvelope>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct ApiMessage {
    pub message: String,
}

#[derive(Debug, Clone, Serialize)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct HealthResponse {
    pub status: &'static str,
    pub service: &'static str,
    pub stores_network_identity: bool,
}

#[derive(Debug, Clone, Serialize)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct PrivacyResponse {
    pub direct_ip_visibility: &'static str,
    pub stored_network_identity: bool,
    pub stored_device_identity: bool,
    pub stored_precise_location: bool,
    pub payload_policy: &'static str,
    pub hosted_recommendation: &'static str,
}

#[derive(Debug, Clone, Serialize)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct AnonymousSummary {
    pub total_envelopes: usize,
    pub by_sensitivity: Vec<SummaryCount>,
    pub by_verification_status: Vec<SummaryCount>,
    pub top_areas: Vec<AreaSummary>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct SummaryCount {
    pub key: String,
    pub count: usize,
}

#[derive(Debug, Clone, Serialize)]
#[serde(crate = "rocket::serde", rename_all = "camelCase")]
pub struct AreaSummary {
    pub general_area: String,
    pub count: usize,
}

impl SyncEnvelope {
    pub fn validate_for_gateway(&self, now_epoch_seconds: i64) -> Result<(), String> {
        if self.envelope_id.trim().is_empty() || self.record_id.trim().is_empty() {
            return Err("Envelope and record IDs are required.".to_string());
        }
        if self.expires_at_epoch_seconds <= now_epoch_seconds {
            return Err("Expired envelopes are not accepted.".to_string());
        }
        if self
            .payload
            .sensitivity
            .eq_ignore_ascii_case("SurvivorCentered")
            || self
                .audience_tier
                .eq_ignore_ascii_case("SurvivorSupportOnly")
        {
            return Err(
                "Survivor-centered reports are not accepted by the default gateway.".to_string(),
            );
        }
        if self.contains_obvious_pii() {
            return Err(
                "Envelope appears to contain personal identifying information.".to_string(),
            );
        }
        if self.content_hash != self.payload.content_hash() {
            return Err("Content hash does not match sanitized payload.".to_string());
        }
        Ok(())
    }

    fn contains_obvious_pii(&self) -> bool {
        let combined = format!(
            "{} {} {}",
            self.payload.general_area, self.payload.observed_risk, self.payload.report_type
        );
        contains_phone_like_value(&combined) || contains_exact_home_hint(&combined)
    }
}

impl SanitizedReportPayload {
    pub fn content_hash(&self) -> String {
        let content = [
            self.report_type.as_str(),
            self.general_area.as_str(),
            self.time_window.as_str(),
            &self.submitted_at_epoch_seconds.to_string(),
            self.observed_risk.as_str(),
            self.verification_status.as_str(),
            self.sensitivity.as_str(),
        ]
        .join("|");
        let digest = Sha256::digest(content.as_bytes());
        hex::encode(digest)
    }
}

fn contains_phone_like_value(value: &str) -> bool {
    let digits = value.chars().filter(|ch| ch.is_ascii_digit()).count();
    digits >= 7
}

fn contains_exact_home_hint(value: &str) -> bool {
    let lower = value.to_ascii_lowercase();
    ["house ", "home ", "plot ", "room "]
        .iter()
        .any(|term| lower.contains(term))
}
