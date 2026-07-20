package com.approagency.base.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.approagency.base.R
import com.approagency.base.config.ApproConfig
import com.approagency.base.model.ui.LegalConfig
import com.approagency.base.model.ui.LegalSection
import com.approagency.base.theme.LocalBaseActivity
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsBottomSheet(
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
    legalConfig: LegalConfig = koinInject<ApproConfig>().legalConfig,
    title: String = stringResource(R.string.rules),
    lastUpdatedPrefix: String = "آخرین به‌روزرسانی:",
    itemPrefix: String = "• ",
    showLastUpdated: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    contentSpacing: Dp = 8.dp,
    sectionSpacing: Dp = 16.dp,
    bottomSpacing: Dp = 32.dp,
    lastUpdatedStyle: TextStyle = MaterialTheme.typography.labelLarge,
    introStyle: TextStyle = MaterialTheme.typography.labelLarge,
    sectionTitleStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    itemStyle: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    sectionTitleColor: Color = MaterialTheme.colorScheme.primary,
    headerContent: (@Composable ColumnScope.(LegalConfig) -> Unit)? = null,
    sectionTitleContent: @Composable (
        index: Int,
        section: LegalSection
    ) -> Unit = { index, section ->
        Text(
            text = buildAnnotatedString {
                append("${index + 1}. ")
                append(section.title)
            },
            style = sectionTitleStyle,
            color = sectionTitleColor
        )
    },
    itemContent: @Composable (
        section: LegalSection,
        item: AnnotatedString
    ) -> Unit = { _, item ->
        Text(
            text = buildAnnotatedString {
                append(itemPrefix)
                append(item)
            },
            style = itemStyle,
            color = textColor
        )
    }
) {
    val activity = LocalBaseActivity.current

    ApproModalBottomSheet(
        title = title,
        onDismiss = {
            activity.hideTermsBottomSheet()
            onDismiss()
        }
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(contentSpacing)
            ) {
                if (headerContent != null) {
                    headerContent(legalConfig)
                } else {
                    if (
                        showLastUpdated &&
                        legalConfig.lastUpdated.isNotBlank()
                    ) {
                        Text(
                            text = "$lastUpdatedPrefix ${legalConfig.lastUpdated}",
                            style = lastUpdatedStyle,
                            color = textColor
                        )
                    }

                    Text(
                        text = legalConfig.intro,
                        style = introStyle,
                        color = textColor
                    )
                }

                legalConfig.sections.forEachIndexed { index, section ->
                    VerticalSpacer(
                        space = sectionSpacing
                    )

                    sectionTitleContent(
                        index,
                        section
                    )

                    section.items.forEach { item ->
                        itemContent(
                            section,
                            item
                        )
                    }
                }

                VerticalSpacer(
                    space = bottomSpacing
                )
            }
        }
    }
}