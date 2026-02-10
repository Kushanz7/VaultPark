package com.kushan.vaultpark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.util.DataStoreUtils
import kotlinx.coroutines.launch

/**
 * Welcome flow shown only on first app install, before account creation.
 * Includes: Welcome screen, What's included in the app, Privacy policy + I Agree.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun WelcomeFlowScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> WelcomePage()
                1 -> AppFeaturesPage()
                2 -> PrivacyPolicyPage()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(
                                width = if (index == pagerState.currentPage) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .padding(horizontal = 4.dp)
                            .background(
                                if (index == pagerState.currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    when (pagerState.currentPage) {
                        0, 1 -> scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                        2 -> scope.launch {
                            DataStoreUtils.markOnboardingCompleted(context)
                            onComplete()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = when (pagerState.currentPage) {
                        0, 1 -> "Next"
                        2 -> "I Agree"
                        else -> "Next"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Poppins
                )
            }
        }
    }
}

@Composable
private fun WelcomePage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to VaultPark",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontFamily = Poppins
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Premium parking with secure QR access and seamless management. Get started in a few steps.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

@Composable
private fun AppFeaturesPage(modifier: Modifier = Modifier) {
    val features = listOf(
        Icons.Default.QrCode to "Secure QR access — Dynamic codes for safe entry and exit",
        Icons.Default.Lock to "Session tracking — Real-time parking session management",
        Icons.Default.Receipt to "Billing & history — View invoices and payment history",
        Icons.Default.DirectionsCar to "Parking lot maps — Find and navigate to lots easily"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "What's in the app",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = Poppins
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Everything you need for parking and management",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        features.forEach { (icon, text) ->
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = text,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PrivacyPolicyPage(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Policy,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = "Privacy Policy",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = Poppins
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = PRIVACY_POLICY_TEXT,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

private const val PRIVACY_POLICY_TEXT = """
By using VaultPark you agree to the following:

1. Information we collect
We collect information necessary to provide parking and access services, including account details, vehicle and session data, and usage information.

2. How we use your data
Your data is used to manage parking sessions, process payments, send notifications, and improve our services. We do not sell your personal information.

3. Data security
We use industry-standard measures to protect your data. QR codes and access tokens are time-limited for your security.

4. Your choices
You can manage notification preferences and account details in the app. You may request deletion of your data by contacting support.

5. Updates
We may update this policy from time to time. Continued use of the app after changes means you accept the updated policy.

Tap "I Agree" below to continue and create your account.
""".trimIndent()
