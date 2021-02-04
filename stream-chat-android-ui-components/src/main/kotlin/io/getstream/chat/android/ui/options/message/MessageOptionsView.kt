package io.getstream.chat.android.ui.options.message

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessageOptionsViewBinding
import io.getstream.chat.android.ui.utils.extensions.setLeftDrawable
import java.io.Serializable

public class MessageOptionsView : FrameLayout {

    private val binding: StreamUiMessageOptionsViewBinding =
        StreamUiMessageOptionsViewBinding.inflate(context.inflater, this, true)

    public constructor(context: Context) : super(context)

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    internal fun configure(configuration: Configuration, isMessageTheirs: Boolean, syncStatus: SyncStatus) {
        if (isMessageTheirs) {
            configureTheirsMessage(configuration)
        } else {
            configureMineMessage(configuration, syncStatus)
        }
    }

    private fun configureTheirsMessage(configuration: Configuration) {
        val iconsTint = configuration.iconsTint

        binding.replyTV.configureListItem(configuration.replyIcon, iconsTint)

        if (configuration.threadEnabled) {
            binding.threadReplyTV.configureListItem(configuration.threadReplyIcon, iconsTint)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        configureCopyMessage(iconsTint, configuration)

        binding.flagTV.configureListItem(configuration.flagIcon, iconsTint)
        binding.muteTV.configureListItem(configuration.muteIcon, iconsTint)
        binding.blockTV.configureListItem(configuration.blockIcon, iconsTint)
        binding.editTV.isVisible = false
        binding.deleteTV.isVisible = false
    }

    private fun configureMineMessage(configuration: Configuration, syncStatus: SyncStatus) {
        val iconsTint = configuration.iconsTint

        binding.replyTV.configureListItem(configuration.replyIcon, iconsTint)

        if (configuration.threadEnabled) {
            binding.threadReplyTV.configureListItem(configuration.threadReplyIcon, iconsTint)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        when (syncStatus) {
            SyncStatus.FAILED_PERMANENTLY -> {
                binding.retryTV.configureListItem(
                    configuration.retryIcon,
                    ContextCompat.getColor(context, R.color.stream_ui_accent_blue)
                )

                binding.retryTV.isVisible = true
                binding.threadReplyTV.isVisible = false
            }
            SyncStatus.COMPLETED -> {
                // Empty
            }
            SyncStatus.SYNC_NEEDED, SyncStatus.IN_PROGRESS -> {
                binding.threadReplyTV.isVisible = false
            }
        }

        configureCopyMessage(iconsTint, configuration)

        binding.editTV.configureListItem(configuration.editIcon, iconsTint)
        binding.flagTV.isVisible = false
        binding.muteTV.isVisible = false
        binding.blockTV.isVisible = false
        binding.deleteTV.run {
            configureListItem(configuration.deleteIcon, iconsTint)
            setTextColor(ContextCompat.getColor(context, R.color.stream_ui_accent_red))
        }
    }

    private fun configureCopyMessage(iconsTint: Int, configuration: Configuration) {
        if (configuration.copyTextEnabled) {
            binding.copyTV.isVisible = true
            binding.copyTV.configureListItem(configuration.copyIcon, iconsTint)
        } else {
            binding.copyTV.isVisible = false
        }
    }

    internal data class Configuration(
        val iconsTint: Int,
        val replyIcon: Int,
        val threadReplyIcon: Int,
        val threadEnabled: Boolean = true,
        val retryIcon: Int,
        val copyIcon: Int,
        val editIcon: Int,
        val flagIcon: Int,
        val muteIcon: Int,
        val blockIcon: Int,
        val deleteIcon: Int,
        val copyTextEnabled: Boolean,
        val deleteConfirmationEnabled: Boolean,
    ) : Serializable

    public fun setReplyListener(onReplyListener: () -> Unit) {
        binding.replyTV.setOnClickListener {
            onReplyListener()
        }
    }

    public fun setThreadListener(onThreadReply: () -> Unit) {
        binding.threadReplyTV.setOnClickListener {
            onThreadReply()
        }
    }

    public fun setRetryListener(onRetry: () -> Unit) {
        binding.retryTV.setOnClickListener {
            onRetry()
        }
    }

    public fun setCopyListener(onCopy: () -> Unit) {
        binding.copyTV.setOnClickListener {
            onCopy()
        }
    }

    public fun setEditMessageListener(onEdit: () -> Unit) {
        binding.editTV.setOnClickListener {
            onEdit()
        }
    }

    public fun setFlagMessageListener(onFlag: () -> Unit) {
        binding.flagTV.setOnClickListener {
            onFlag()
        }
    }

    public fun setDeleteMessageListener(onDelete: () -> Unit) {
        binding.deleteTV.setOnClickListener {
            onDelete()
        }
    }

    public fun setMuteUserListener(onMute: () -> Unit) {
        binding.muteTV.setOnClickListener {
            onMute()
        }
    }

    public fun setBlockUserListener(onBlock: () -> Unit) {
        binding.blockTV.setOnClickListener {
            onBlock()
        }
    }

    private fun TextView.configureListItem(icon: Int, iconTint: Int) {
        this.setLeftDrawable(icon, iconTint)
    }
}
