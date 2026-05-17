package com.example.hotel_pere_maria_app.data.model

import java.util.Date

/** Bloque P19 en reserva (early_checkin_requested / late_checkout_requested). */
data class FlexibilityRequestBlock(
    val status: String? = null,
    val requested_time: Date? = null,
    val loyalty_tier: String? = null,
    val hours_difference: Double? = null,
    val rate_per_hour: Double? = null,
    val final_fee: Double? = null,
    val review_note: String? = null,
    val auto_approved: Boolean? = null,
    /** room | facilities (zonas comunes, sin habitación). */
    val late_mode: String? = null,
) {
    fun hasActiveRequest(): Boolean =
        status == "pending" || status == "approved"

    fun canSubmitNew(): Boolean = status == null || status == "rejected"
}

enum class FlexibilityKind { EARLY, LATE, LATE_FACILITIES }

data class FlexibilityFeeQuote(
    val loyalty_tier: String? = null,
    val hours_difference: Double? = null,
    val rate_per_hour: Double? = null,
    val final_fee: Double? = null,
    val discount_percent: Double? = null,
    val free_access: Boolean? = null,
    val note: String? = null,
)

data class FlexibilityFeePreview(
    val early_checkin: FlexibilityFeeQuote? = null,
    val late_checkout: FlexibilityFeeQuote? = null,
)

data class FlexibilityStatusResponse(
    val reservation_id: String? = null,
    val loyalty_tier: String? = null,
    val early_checkin_requested: FlexibilityRequestBlock? = null,
    val late_checkout_requested: FlexibilityRequestBlock? = null,
    val price: Number? = null,
    val fee_preview: FlexibilityFeePreview? = null,
    val pricing_formula: String? = null,
)

fun FlexibilityKind.feeQuote(status: FlexibilityStatusResponse?): FlexibilityFeeQuote? =
    when (this) {
        FlexibilityKind.EARLY -> status?.fee_preview?.early_checkin
        FlexibilityKind.LATE, FlexibilityKind.LATE_FACILITIES -> status?.fee_preview?.late_checkout
    }

fun loyaltyTierLabel(tier: String?): String =
    when (tier?.lowercase()) {
        "gold" -> "Oro"
        "silver" -> "Plata"
        else -> "Bronce"
    }

fun loyaltyApprovalHint(tier: String?): String =
    when (tier?.lowercase()) {
        "gold", "silver" -> "Con tu rango, si hay hueco en la habitación la solicitud se aprueba al instante."
        else -> "Rango bronce: recepción revisará la solicitud si la habitación está libre."
    }

fun formatFeePreviewLine(quote: FlexibilityFeeQuote?): String? {
    quote ?: return null
    if (quote.free_access == true) return "Sin suplemento con tu rango."
    val fee = quote.final_fee
    if (fee != null && fee > 0) {
        return "Desde %.2f €".format(fee)
    }
    return quote.note?.takeIf { it.isNotBlank() }
}

