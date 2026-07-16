package com.approagency.base.model.network

import com.approagency.base.R
import com.approagency.base.model.UiText

data class Failure(
    val code: Int,
    val text: UiText,
    var serverMessage: String? = null
) : Throwable() {
    companion object {
        val Unknown = Failure(-1, UiText.StringResource(R.string.error_unknown))
        val StoreUnavailable = Failure(-10, UiText.StringResource(R.string.error_store_unavailable))
        val PermissionDenied = Failure(1, UiText.StringResource(R.string.error_permission))
        val InstallMyketApplication = Failure(
            code = 1001,
            text = UiText.StringResource(R.string.error_install_myket_application)
        )

        val InstallBazarApplication = Failure(
            code = 1002,
            text = UiText.StringResource(R.string.error_install_bazar_application)
        )

        val PurchaseCancelled = Failure(
            code = 1003,
            text = UiText.StringResource(R.string.error_purchase_cancelled)
        )

        val PurchaseFailed = Failure(
            code = 1004,
            text = UiText.StringResource(R.string.error_purchase_failed)
        )

        val HaveSubscription = Failure(
            code = 1005,
            text = UiText.StringResource(R.string.error_have_subscription)
        )

        val NoAccess = Failure(
            code = 1005,
            text = UiText.StringResource(R.string.error_no_access)
        )

        // Client Errors
        val BadRequest = Failure(400, UiText.StringResource(R.string.error_bad_request))
        val Unauthorized = Failure(401, UiText.StringResource(R.string.error_unauthorized))
        val PaymentRequired = Failure(402, UiText.StringResource(R.string.error_payment_required))
        val Forbidden = Failure(403, UiText.StringResource(R.string.error_forbidden))
        val NotFound = Failure(404, UiText.StringResource(R.string.error_not_found))
        val MethodNotAllowed =
            Failure(405, UiText.StringResource(R.string.error_method_not_allowed))
        val NotAcceptable = Failure(406, UiText.StringResource(R.string.error_not_acceptable))
        val ProxyAuthenticationRequired = Failure(
            407,
            UiText.StringResource(R.string.error_proxy_authentication_required)
        )
        val RequestTimeout = Failure(408, UiText.StringResource(R.string.error_request_timeout))
        val Conflict = Failure(409, UiText.StringResource(R.string.error_conflict))
        val Gone = Failure(410, UiText.StringResource(R.string.error_gone))
        val LengthRequired = Failure(411, UiText.StringResource(R.string.error_length_required))
        val PreconditionFailed =
            Failure(412, UiText.StringResource(R.string.error_precondition_failed))
        val PayloadTooLarge = Failure(413, UiText.StringResource(R.string.error_payload_too_large))
        val UriTooLong = Failure(414, UiText.StringResource(R.string.error_uri_too_long))
        val UnsupportedMediaType =
            Failure(415, UiText.StringResource(R.string.error_unsupported_media_type))
        val RangeNotSatisfiable =
            Failure(416, UiText.StringResource(R.string.error_range_not_satisfiable))
        val ExpectationFailed =
            Failure(417, UiText.StringResource(R.string.error_expectation_failed))
        val ImATeapot = Failure(418, UiText.StringResource(R.string.error_im_a_teapot))
        val MisdirectedRequest =
            Failure(421, UiText.StringResource(R.string.error_misdirected_request))
        val UnprocessableEntity =
            Failure(422, UiText.StringResource(R.string.error_unprocessable_entity))
        val Locked = Failure(423, UiText.StringResource(R.string.error_locked))
        val FailedDependency = Failure(424, UiText.StringResource(R.string.error_failed_dependency))
        val TooEarly = Failure(425, UiText.StringResource(R.string.error_too_early))
        val UpgradeRequired = Failure(426, UiText.StringResource(R.string.error_upgrade_required))
        val PreconditionRequired =
            Failure(428, UiText.StringResource(R.string.error_precondition_required))
        val TooManyRequests = Failure(429, UiText.StringResource(R.string.error_too_many_requests))
        val RequestHeaderFieldsTooLarge = Failure(
            431,
            UiText.StringResource(R.string.error_request_header_fields_too_large)
        )
        val UnavailableForLegalReasons = Failure(
            451,
            UiText.StringResource(R.string.error_too_many_requests)
        )
        val Connection = Failure(499, UiText.StringResource(R.string.error_connection))

        // Server Errors
        val InternalServerError =
            Failure(500, UiText.StringResource(R.string.error_internal_server_error))
        val NotImplemented = Failure(501, UiText.StringResource(R.string.error_not_implemented))
        val BadGateway = Failure(502, UiText.StringResource(R.string.error_bad_gateway))
        val ServiceUnavailable =
            Failure(503, UiText.StringResource(R.string.error_service_unavailable))
        val GatewayTimeout = Failure(504, UiText.StringResource(R.string.error_gateway_timeout))
        val HttpVersionNotSupported = Failure(
            505,
            UiText.StringResource(R.string.error_http_version_not_supported)
        )
        val VariantAlsoNegates = Failure(
            506,
            UiText.StringResource(R.string.error_variant_also_negotiates)
        )
        val InsufficientStorage =
            Failure(507, UiText.StringResource(R.string.error_insufficient_storage))
        val LoopDetected = Failure(508, UiText.StringResource(R.string.error_loop_detected))
        val NotExtended = Failure(510, UiText.StringResource(R.string.error_not_extended))
        val NetworkAuthenticationRequired = Failure(
            511,
            UiText.StringResource(R.string.error_network_authentication_required)
        )

        private val failures = listOf(
            Unknown,
            InstallBazarApplication,
            InstallBazarApplication,
            PurchaseFailed,
            PurchaseCancelled,
            NoAccess,
            PermissionDenied,
            BadRequest,
            Unauthorized,
            InstallMyketApplication,
            HaveSubscription,
            PaymentRequired,
            Forbidden,
            NotFound,
            MethodNotAllowed,
            NotAcceptable,
            ProxyAuthenticationRequired,
            RequestTimeout,
            Conflict,
            Gone,
            LengthRequired,
            PreconditionFailed,
            PayloadTooLarge,
            UriTooLong,
            UnsupportedMediaType,
            RangeNotSatisfiable,
            ExpectationFailed,
            ImATeapot,
            MisdirectedRequest,
            UnprocessableEntity,
            Locked,
            FailedDependency,
            TooEarly,
            UpgradeRequired,
            PreconditionRequired,
            TooManyRequests,
            RequestHeaderFieldsTooLarge,
            UnavailableForLegalReasons,
            Connection,
            InternalServerError,
            NotImplemented,
            BadGateway,
            ServiceUnavailable,
            GatewayTimeout,
            HttpVersionNotSupported,
            VariantAlsoNegates,
            InsufficientStorage,
            LoopDetected,
            NotExtended,
            NetworkAuthenticationRequired
        )

        private val failureMap = failures.associateBy { it.code }

        fun fromCode(code: Int): Failure {
            return failureMap[code] ?: Unknown
        }
    }
}