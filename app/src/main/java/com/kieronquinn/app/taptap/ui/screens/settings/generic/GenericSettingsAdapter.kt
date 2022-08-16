package com.kieronquinn.app.taptap.ui.screens.settings.generic

import android.content.Context
import android.content.res.ColorStateList
import android.text.Html
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.invert
import com.kieronquinn.app.taptap.databinding.*
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel.SettingsItem
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel.SettingsItem.SettingsItemType
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import me.saket.bettermovementmethod.BetterLinkMovementMethod

abstract class GenericSettingsAdapter(
    context: Context,
    recyclerView: RecyclerView,
    private val _items: List<SettingsItem>
) : LifecycleAwareRecyclerView.Adapter<GenericSettingsAdapter.ViewHolder>(recyclerView) {

    init {
        setHasStableIds(true)
    }

    private fun getItems(): List<SettingsItem> {
        return _items.filter { it.isVisible() }
    }

    private var items = getItems()

    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    private val chipBackground by lazy {
        ColorStateList.valueOf(monet.getPrimaryColor(recyclerView.context))
    }

    private val googleSansTextMedium by lazy {
        ResourcesCompat.getFont(recyclerView.context, R.font.google_sans_text_medium)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (SettingsItemType.fromViewType(viewType)) {
            SettingsItemType.TEXT -> ViewHolder.Text(
                ItemSettingsTextItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            SettingsItemType.SWITCH -> ViewHolder.Switch(
                ItemSettingsSwitchItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            SettingsItemType.SLIDER -> ViewHolder.Slider(
                ItemSettingsSliderItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            SettingsItemType.INFO -> ViewHolder.Info(
                ItemSettingsInfoItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            SettingsItemType.ABOUT -> ViewHolder.About(
                ItemSettingsMoreAboutBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            SettingsItemType.HEADER -> ViewHolder.Header(
                ItemSettingsHeaderBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        if (holder !is ViewHolder.Info && item !is SettingsItem.About) {
            holder.binding.setItemEnabled(item)
        }
        when (holder) {
            is ViewHolder.Text -> holder.binding.setupTextItem(
                item as SettingsItem.Text,
                holder.lifecycleScope
            )
            is ViewHolder.Switch -> holder.binding.setupSwitchItem(
                item as SettingsItem.Switch,
                holder.lifecycleScope
            )
            is ViewHolder.Slider -> holder.binding.setupSliderItem(
                item as SettingsItem.Slider,
                holder.lifecycleScope
            )
            is ViewHolder.Info -> holder.binding.setupInfoItem(
                item as SettingsItem.Info,
                holder.lifecycleScope
            )
            is ViewHolder.About -> holder.binding.setupAboutItem(
                item as SettingsItem.About,
                holder.lifecycleScope
            )
            is ViewHolder.Header -> holder.binding.setupHeaderItem(
                item as SettingsItem.Header
            )
        }
    }

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    private fun ItemSettingsTextItemBinding.setupTextItem(
        item: SettingsItem.Text,
        scope: LifecycleCoroutineScope
    ) {
        val context = root.context
        itemSettingsTextTitle.text = when {
            item.title != null -> item.title
            item.titleRes != null -> context.getText(item.titleRes)
            else -> null
        }
        if (item.linkClicked != null) {
            itemSettingsTextContent.setLinkTextColor(monet.getAccentColor(context))
            itemSettingsTextContent.text = when {
                item.content != null -> Html.fromHtml(
                    item.content.invoke().toString(),
                    Html.FROM_HTML_MODE_LEGACY
                )
                item.contentRes != null -> Html.fromHtml(
                    context.getText(item.contentRes).toString(), Html.FROM_HTML_MODE_LEGACY
                )
                else -> null
            }
            Linkify.addLinks(itemSettingsTextContent, Linkify.ALL)
            itemSettingsTextContent.isVisible = !itemSettingsTextContent.text.isNullOrEmpty()
            itemSettingsTextContent.movementMethod =
                BetterLinkMovementMethod.newInstance().setOnLinkClickListener { _, url ->
                    item.linkClicked?.invoke(url)
                    true
                }
        } else {
            itemSettingsTextContent.movementMethod = null
            itemSettingsTextContent.text = when {
                item.content != null -> item.content.invoke()
                item.contentRes != null -> context.getText(item.contentRes)
                else -> null
            }
            itemSettingsTextContent.isVisible = !itemSettingsTextContent.text.isNullOrEmpty()
        }
        val isEnabled = item.isEnabled()
        if(item.onClick != null && isEnabled){
            root.addRipple()
        }else{
            root.removeRipple()
        }
        itemSettingsTextIcon.setImageResource(item.icon)
        scope.launchWhenResumed {
            item.onClick?.let {
                root.onClicked().collect {
                    if (!isEnabled) return@collect
                    item.onClick.invoke()
                }
            }
        }
    }

    private fun ItemSettingsSwitchItemBinding.setupSwitchItem(
        item: SettingsItem.Switch,
        scope: LifecycleCoroutineScope
    ) {
        val context = root.context
        itemSettingsSwitchTitle.text = when {
            item.title != null -> item.title
            item.titleRes != null -> context.getText(item.titleRes)
            else -> null
        }
        itemSettingsSwitchContent.text = when {
            item.content != null -> item.content.invoke()
            item.contentRes != null -> context.getText(item.contentRes)
            else -> null
        }.also {
            itemSettingsSwitchContent.isVisible = !it.isNullOrEmpty()
        }
        itemSettingsSwitchIcon.setImageResource(item.icon)
        val isEnabled = item.isEnabled()
        itemSettingsSwitchSwitch.isEnabled = isEnabled
        itemSettingsSwitchSwitch.isChecked = item.setting.getSync()
        itemSettingsSwitchSwitch.applyMonet()
        itemSettingsSwitchSwitch.alpha = if (isEnabled) 1f else 0.5f
        itemSettingsSwitchTitle.alpha = if (isEnabled) 1f else 0.5f
        itemSettingsSwitchContent.alpha = if (isEnabled) 1f else 0.5f
        itemSettingsSwitchIcon.alpha = if (isEnabled) 1f else 0.5f
        scope.launchWhenResumed {
            item.setting.asFlow().collect {
                itemSettingsSwitchSwitch.isChecked = it
            }
        }
        scope.launchWhenResumed {
            root.onClicked().collect {
                if (!isEnabled) return@collect
                item.setting.invert()
            }
        }
        scope.launchWhenResumed {
            itemSettingsSwitchSwitch.onClicked().collect {
                if (!isEnabled) return@collect
                if (item.setting is TapTapSettings.FakeTapTapSetting) {
                    //Prevent initial change
                    (it as CompoundButton).isChecked = item.setting.get()
                }
                item.setting.invert()
            }
        }
    }

    private fun ItemSettingsSliderItemBinding.setupSliderItem(
        item: SettingsItem.Slider,
        scope: LifecycleCoroutineScope
    ) {
        val context = root.context
        itemSettingsSliderTitle.text = when {
            item.title != null -> item.title
            item.titleRes != null -> context.getText(item.titleRes)
            else -> null
        }
        itemSettingsSliderContent.text = when {
            item.content != null -> item.content.invoke()
            item.contentRes != null -> context.getText(item.contentRes)
            else -> null
        }.also {
            itemSettingsSliderContent.isVisible = !it.isNullOrEmpty()
        }
        itemSettingsSliderIcon.setImageResource(item.icon)
        itemSettingsSliderSlider.run {
            itemSettingsSliderSlider.applyMonet()
            isEnabled = item.isEnabled()
            if (item.stepSize != null) {
                stepSize = item.stepSize
            }
            valueFrom = item.minimumValue
            valueTo = item.maximumValue
            value = item.setting.getSync().toFloat()
            setLabelFormatter(item.labelFormatter)
            setTooltipColor(monet.getAccentColor(context, false))
        }
        scope.launchWhenResumed {
            item.setting.asFlow().collect {
                itemSettingsSliderSlider.value = it.toFloat()
            }
        }
        scope.launchWhenResumed {
            itemSettingsSliderSlider.onChanged().collect {
                item.setting.set(it)
            }
        }
    }

    private fun ItemSettingsInfoItemBinding.setupInfoItem(
        item: SettingsItem.Info,
        scope: LifecycleCoroutineScope
    ) {
        val context = root.context
        Linkify.addLinks(itemSettingsInfoContent, Linkify.ALL)
        if (item.linkClicked != null) {
            itemSettingsInfoContent.setLinkTextColor(monet.getAccentColor(context))
            itemSettingsInfoContent.text = when {
                item.content != null -> Html.fromHtml(
                    item.content.invoke().toString(),
                    Html.FROM_HTML_MODE_LEGACY
                )
                item.contentRes != null -> Html.fromHtml(
                    context.getText(item.contentRes).toString(), Html.FROM_HTML_MODE_LEGACY
                )
                else -> null
            }
            itemSettingsInfoContent.movementMethod =
                BetterLinkMovementMethod.newInstance().setOnLinkClickListener { _, url ->
                    item.linkClicked?.invoke(url)
                    true
                }
        } else {
            itemSettingsInfoContent.text = when {
                item.content != null -> item.content.invoke()
                item.contentRes != null -> context.getText(item.contentRes)
                else -> null
            }
        }
        if (item.onDismissClicked != null) {
            val fallbackBackground =
                if (context.isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
            root.setCardBackgroundColor(
                monet.getBackgroundColorSecondary(context) ?: ContextCompat.getColor(context, fallbackBackground)
            )
            itemSettingsInfoDismiss.isVisible = true
        } else {
            root.setCardBackgroundColor(ColorStateList.valueOf(monet.getPrimaryColor(context)))
            itemSettingsInfoDismiss.isVisible = false
        }
        if(item.icon != null){
            itemSettingsInfoIcon.setImageResource(item.icon)
        }else{
            itemSettingsInfoIcon.setImageResource(R.drawable.ic_about)
        }
        scope.launchWhenResumed {
            itemSettingsInfoDismiss.onClicked().collect {
                item.onDismissClicked?.invoke()
            }
        }
        if(item.onClick != null){
            root.addRippleForeground()
        }else{
            root.removeRippleForeground()
        }
        scope.launchWhenResumed {
            item.onClick?.let {
                root.onClicked().collect {
                    item.onClick.invoke()
                }
            }
        }
    }

    private fun ItemSettingsMoreAboutBinding.setupAboutItem(
        item: SettingsItem.About,
        scope: LifecycleCoroutineScope
    ) {
        val context = root.context
        val content = context.getString(R.string.about_version, BuildConfig.VERSION_NAME)
        itemSettingsMoreAboutContent.text = content
        val fallbackBackground = if (context.isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
        root.setCardBackgroundColor(ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(context) ?: ContextCompat.getColor(
                context,
                fallbackBackground
            )
        ))
        mapOf(
            itemSettingsMoreAboutContributors to item.onContributorsClicked,
            itemSettingsMoreAboutDonate to item.onDonateClicked,
            itemSettingsMoreAboutGithub to item.onGitHubClicked,
            itemSettingsMoreAboutLibraries to item.onLibrariesClicked,
            itemSettingsMoreAboutTwitter to item.onTwitterClicked,
            itemSettingsMoreAboutXda to item.onXdaClicked
        ).forEach { chip ->
            with(chip.key){
                chipBackgroundColor = chipBackground
                typeface = googleSansTextMedium
                scope.launchWhenResumed {
                    onClicked().collect {
                        chip.value()
                    }
                }
            }
        }
    }

    private fun ItemSettingsHeaderBinding.setupHeaderItem(item: SettingsItem.Header) {
        itemSettingsHeaderTitle.setText(item.titleRes)
    }

    private fun ViewBinding.setItemEnabled(item: SettingsItem) {
        val enabled = item.isEnabled()
        root.alpha = if (enabled) 1f else 0.5f
        if (enabled && item !is SettingsItem.Slider) {
            root.addRipple()
        } else {
            root.removeRipple()
        }
    }

    fun refreshVisibleItems(){
        items = getItems()
        notifyDataSetChanged()
    }

    sealed class ViewHolder(open val binding: ViewBinding) : LifecycleAwareRecyclerView.ViewHolder(binding.root) {
        data class Text(override val binding: ItemSettingsTextItemBinding) : ViewHolder(binding)
        data class Switch(override val binding: ItemSettingsSwitchItemBinding) : ViewHolder(binding)
        data class Slider(override val binding: ItemSettingsSliderItemBinding) : ViewHolder(binding)
        data class Info(override val binding: ItemSettingsInfoItemBinding) : ViewHolder(binding)
        data class About(override val binding: ItemSettingsMoreAboutBinding) : ViewHolder(binding)
        data class Header(override val binding: ItemSettingsHeaderBinding) : ViewHolder(binding)
    }

}