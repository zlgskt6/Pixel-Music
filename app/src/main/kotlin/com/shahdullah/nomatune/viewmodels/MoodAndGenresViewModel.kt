/*
 * Pixel Music (2026)
 * © Shahdullah — github.com/shahdullah
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Based on ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.shahdullah.nomatune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahdullah.nomatune.innertube.YouTube
import com.shahdullah.nomatune.innertube.pages.MoodAndGenres
import com.shahdullah.nomatune.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodAndGenresViewModel
@Inject
constructor() : ViewModel() {
    val moodAndGenres = MutableStateFlow<List<MoodAndGenres.Item>?>(null)
    private var isFetching = false

    init {
        loadData()
    }

    fun loadData(force: Boolean = false) {
        if (!force && (moodAndGenres.value != null || isFetching)) return
        isFetching = true
        viewModelScope.launch {
            try {
                YouTube
                    .explore()
                    .onSuccess {
                        moodAndGenres.value = it.moodAndGenres
                    }.onFailure {
                        reportException(it)
                    }
            } finally {
                isFetching = false
            }
        }
    }
}
