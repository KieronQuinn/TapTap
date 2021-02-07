package com.kieronquinn.app.taptap.ui.screens.settings.about

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ItemAboutHeaderBinding
import com.kieronquinn.app.taptap.databinding.ItemAboutItemBinding
import com.kieronquinn.app.taptap.databinding.ItemAboutTitleBinding
import me.saket.bettermovementmethod.BetterLinkMovementMethod

class SettingsAboutAdapter(context: Context, private val viewModel: SettingsAboutViewModel, private val items: Array<AboutItem>): RecyclerView.Adapter<SettingsAboutAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].itemType.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(ItemType.values()[viewType]){
            ItemType.HEADER -> ViewHolder.Header(ItemAboutHeaderBinding.inflate(layoutInflater, parent, false))
            ItemType.TITLE -> ViewHolder.Title(ItemAboutTitleBinding.inflate(layoutInflater, parent, false))
            ItemType.ITEM -> ViewHolder.Item(ItemAboutItemBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when(holder){
            is ViewHolder.Header -> holder.binding.setupHeader()
            is ViewHolder.Title -> holder.binding.setupTitle(item as AboutItem.Title)
            is ViewHolder.Item -> holder.binding.setupItem(item as AboutItem.Item)
        }
    }

    private fun ItemAboutHeaderBinding.setupHeader(){
        aboutChipGithub.setOnClickListener {
            viewModel.onGitHubClicked(it.context)
        }
        aboutChipLibraries.setOnClickListener {
            viewModel.onLibrariesClicked(it.context)
        }
        aboutChipXda.setOnClickListener {
            viewModel.onXDAClicked(it.context)
        }
        aboutHeaderVersion.run {
            text = context.getString(R.string.about_version, BuildConfig.VERSION_NAME)
        }
    }

    private fun ItemAboutTitleBinding.setupTitle(item: AboutItem.Title){
        itemAboutTitleText.setText(item.text)
    }

    private fun ItemAboutItemBinding.setupItem(item: AboutItem.Item){
        title.setText(item.title)
        if(item.isHtml){
            val spannable = HtmlCompat.fromHtml(
                summary.context.getString(item.text),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            summary.text = spannable
            BetterLinkMovementMethod.linkifyHtml(summary).setOnLinkClickListener { textView, urlText ->
                viewModel.onLinkClicked(textView.context, urlText)
                true
            }
        }else{
            summary.setText(item.text)
        }
        icon.setImageResource(item.icon ?: return)
    }

    override fun getItemCount() = items.size

    enum class ItemType {
        HEADER, TITLE, ITEM
    }

    sealed class AboutItem(open val itemType: ItemType) {
        object Header: AboutItem(ItemType.HEADER)
        data class Title(@StringRes val text: Int): AboutItem(ItemType.TITLE)
        data class Item(@StringRes val title: Int, @StringRes val text: Int, @DrawableRes val icon: Int?, val isHtml: Boolean = false): AboutItem(ItemType.ITEM)
    }

    sealed class ViewHolder(open val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        data class Header(override val binding: ItemAboutHeaderBinding): ViewHolder(binding)
        data class Title(override val binding: ItemAboutTitleBinding): ViewHolder(binding)
        data class Item(override val binding: ItemAboutItemBinding): ViewHolder(binding)
    }

}