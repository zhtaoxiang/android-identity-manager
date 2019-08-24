package com.ndn.jwtan.identitymanager;

public class Constants {
    final static String JSON_CA_NAME = "name";
    final static String JSON_CA_CONFIG = "ca-config";
    final static String JSON_CA_ECDH = "ecdh-pub";
    final static String JSON_CA_SALT = "salt";
    final static String JSON_CA_EQUEST_ID = "request-id";
    final static String JSON_CA_STATUS = "status";
    final static String JSON_CA_CHALLENGES = "challenges";
    final static String JSON_CA_CHALLENGE_ID = "challenge-id";
    final static String JSON_CA_CERT_ID = "certificate-id";

    // JSON format for Challenge Module
    final static String JSON_CHALLENGE_STATUS = "challenge-status";
    final static String JSON_CHALLENGE_REMAINING_TRIES = "remaining-tries";
    final static String JSON_CHALLENGE_REMAINING_TIME = "remaining-time";

    // JSON format for Certificate Requester
    final static String JSON_CLIENT_PROBE_INFO = "probe-info";
    final static String JSON_CLIENT_ECDH = "ecdh-pub";
    final static String JSON_CLIENT_CERT_REQ = "cert-request";
    final static String JSON_CLIENT_SELECTED_CHALLENGE = "selected-challenge";

    // NDNCERT Status Enum
    final static int STATUS_BEFORE_CHALLENGE = 0;
    final static int STATUS_CHALLENGE = 1;
    final static int STATUS_PENDING = 2;
    final static int STATUS_SUCCESS = 3;
    final static int STATUS_FAILURE = 4;
    final static int STATUS_NOT_STARTED = 5;

    // Pre-defined challenge status
    final static String CHALLENGE_STATUS_SUCCESS = "success";
    final static String CHALLENGE_STATUS_FAILURE_TIMEOUT = "failure-timeout";
    final static String CHALLENGE_STATUS_FAILURE_MAXRETRY = "failure-max-retry";
    final static String CHALLENGE_STATUS_UNKNOWN_CHALLENGE = "unknown-challenge";


}
