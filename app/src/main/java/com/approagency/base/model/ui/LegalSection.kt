package com.approagency.base.model.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

@Immutable
data class LegalSection(
    val title: AnnotatedString,
    val items: List<AnnotatedString>
)

@Immutable
data class LegalConfig(
    val lastUpdated: String? = null,
    val supportEmail: String = "approagency@gmail.com",
    val intro: AnnotatedString? = null,
    val defaultSections: List<LegalSection> = defaultLegalSections(supportEmail),
    val customSections: List<LegalSection> = emptyList()
) {
    val sections: List<LegalSection>
        get() = defaultSections + customSections
}

fun legalSection(
    title: String,
    vararg items: String
): LegalSection {
    return LegalSection(
        title = AnnotatedString(title),
        items = items.map(::AnnotatedString)
    )
}

fun defaultLegalIntro(
    appName: String
): AnnotatedString {
    return AnnotatedString(
        "استفاده از اپلیکیشن «$appName» که از این پس «برنامه» نامیده می‌شود، " +
                "به‌منزلهٔ مطالعه و پذیرش کامل شرایط و قوانین زیر است. " +
                "در صورتی که با هر بخشی از این قوانین موافق نیستید، لطفاً از برنامه استفاده نکنید."
    )
}

fun defaultLegalSections(
    supportEmail: String
): List<LegalSection> {
    return listOf(
        legalSection(
            title = "حریم خصوصی و داده‌ها",
            "برای ارائهٔ خدمات، حداقل اطلاعات لازم، از جمله شماره موبایل و وضعیت اشتراک، دریافت و نگه‌داری می‌شود.",
            "اطلاعات خرید و توکن خرید صرفاً برای فعال‌سازی اشتراک به سرور ارسال می‌شود.",
            "اطلاعات کاربران بدون رضایت ایشان در اختیار اشخاص ثالث غیرمرتبط قرار نمی‌گیرد، مگر در مواردی که قانون الزام کند."
        ),
        legalSection(
            title = "تعهدات کاربر",
            "استفاده از برنامه صرفاً برای اهداف قانونی و شخصی مجاز است.",
            "ایجاد اختلال در عملکرد برنامه، مهندسی معکوس یا دور زدن محدودیت‌ها ممنوع است.",
            "بازنشر یا تجاری‌سازی محتوای برنامه بدون اجازه ممنوع است."
        ),
        legalSection(
            title = "مالکیت فکری",
            "تمام حقوق مربوط به برنامه، نام، نشان، رابط کاربری و محتوای آن متعلق به مالک برنامه است و هرگونه استفاده غیرمجاز پیگرد قانونی دارد."
        ),
        legalSection(
            title = "محدودیت مسئولیت",
            "برنامه همان‌گونه که هست ارائه می‌شود و تضمینی نسبت به بی‌نقص بودن، در دسترس بودن دائمی یا صحت کامل اطلاعات داده نمی‌شود.",
            "مالک برنامه در قبال خسارات مستقیم یا غیرمستقیم ناشی از استفاده یا اتکا به اطلاعات برنامه مسئولیتی نمی‌پذیرد."
        ),
        legalSection(
            title = "تعلیق، تغییرات و قانون حاکم",
            "در صورت نقض این قوانین، دسترسی کاربر ممکن است محدود یا مسدود شود.",
            "این شرایط ممکن است به‌روزرسانی شود و ادامه استفاده به‌منزلهٔ پذیرش تغییرات است.",
            "این شرایط تابع قوانین جمهوری اسلامی ایران است."
        ),
        LegalSection(
            title = AnnotatedString("ارتباط با ما"),
            items = listOf(
                supportEmailText(supportEmail)
            )
        )
    )
}

fun supportEmailText(
    email: String
): AnnotatedString {
    return buildAnnotatedString {
        append("در صورت هرگونه پرسش یا مشکل از طریق ایمیل ")

        withLink(
            LinkAnnotation.Url(
                url = "mailto:$email",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(email)
        }

        append(" با ما در تماس باشید.")
    }
}