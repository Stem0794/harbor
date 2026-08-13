package com.monstera.harbor.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class HarborDesignSystemAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun quickActionUsesVisibleTextWithoutDuplicateIconDescription() {
        composeRule.setContent {
            MaterialTheme {
                HarborQuickActionTile(
                    label = "Send files",
                    icon = HarborIconKind.Send,
                    onClick = {},
                )
            }
        }

        composeRule
            .onAllNodesWithContentDescription("Send files", useUnmergedTree = true)
            .assertCountEquals(0)
        composeRule.onNodeWithText("Send files").assertExists().assertHasClickAction()
    }

    @Test
    fun privacyFactUsesVisibleTextWithoutDuplicateIconDescription() {
        composeRule.setContent {
            MaterialTheme {
                HarborPrivacyPanel(
                    facts = listOf(
                        PrivacyFact(
                            title = "No analytics",
                            body = "Harbor does not collect usage data.",
                            icon = HarborIconKind.Analytics,
                        ),
                    ),
                )
            }
        }

        composeRule
            .onAllNodesWithContentDescription("No analytics", useUnmergedTree = true)
            .assertCountEquals(0)
        composeRule.onNodeWithText("No analytics").assertExists()
    }

    @Test
    fun bottomNavigationUsesVisibleTextWithoutDuplicateIconDescription() {
        composeRule.setContent {
            MaterialTheme {
                HarborBottomBar(
                    active = HarborIconKind.Home,
                    onPersonal = {},
                )
            }
        }

        composeRule
            .onAllNodesWithContentDescription("Personal", useUnmergedTree = true)
            .assertCountEquals(0)
        composeRule.onNodeWithText("Personal").assertExists().assertHasClickAction()
    }
}
