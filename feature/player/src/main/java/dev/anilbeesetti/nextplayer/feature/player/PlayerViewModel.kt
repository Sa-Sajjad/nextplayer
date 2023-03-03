package dev.anilbeesetti.nextplayer.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.data.models.PlayerItem
import dev.anilbeesetti.nextplayer.core.data.repository.VideoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val END_POSITION_OFFSET = 5L

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    var playbackPosition = MutableStateFlow<Long?>(null)
        private set

    var currentPlaybackPath = MutableStateFlow<String?>(null)
        private set

    var currentPlayerItems: MutableList<PlayerItem> = mutableListOf()

    val currentPlayerItemIndex: Int
        get() = currentPlayerItems.indexOfFirst { it.path == currentPlaybackPath.value }

    fun setCurrentMedia(path: String?) {
        currentPlaybackPath.value = path
        viewModelScope.launch {
            playbackPosition.value = path?.let { videoRepository.getPosition(it) }
        }
    }

    fun initMedia(path: String?) {
        setCurrentMedia(path)
        currentPlayerItems.addAll(videoRepository.getLocalPlayerItems())
    }

    fun updatePosition(position: Long) {
        playbackPosition.value = position
        viewModelScope.launch {
            currentPlaybackPath.value?.let {
                if (position >= currentPlayerItems[currentPlayerItemIndex].duration - END_POSITION_OFFSET) {
                    videoRepository.updatePosition(it, C.TIME_UNSET)
                } else {
                    videoRepository.updatePosition(it, position)
                }
            }
        }
    }

    fun updatePosition(path: String, position: Long) {
        viewModelScope.launch { videoRepository.updatePosition(path, position) }
    }
}