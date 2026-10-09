package com.scttech.android.kotlin.openfitness.ui.badges

import androidx.lifecycle.ViewModel
import com.scttech.android.kotlin.openfitness.data.badge.BadgeAwarder
import com.scttech.android.kotlin.openfitness.domain.model.Badge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/** Exposes badges as they're earned, so the app shell can announce them wherever the user is. */
@HiltViewModel
class BadgeAnnouncerViewModel @Inject constructor(
    badgeAwarder: BadgeAwarder,
) : ViewModel() {
    val newlyEarned: SharedFlow<Badge> = badgeAwarder.newlyEarned
}
