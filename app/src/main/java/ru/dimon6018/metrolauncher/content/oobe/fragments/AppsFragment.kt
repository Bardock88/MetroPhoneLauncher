package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.apps.App
import ru.dimon6018.metrolauncher.content.data.apps.AppData
import ru.dimon6018.metrolauncher.content.data.apps.AppEntity
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.generateRandomTileSize
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.recompressIcon
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setUpApps
import kotlin.random.Random

class AppsFragment: Fragment() {

    private var recyclerView: RecyclerView? = null
    private var loading: WP7ProgressBar? = null
    private var fragmentContext: Context? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_apps, container, false)
        fragmentContext = requireContext()
        WelcomeActivity.setText(requireActivity(), getString(R.string.configureApps))
        recyclerView = view.findViewById(R.id.oobeRecycler)
        loading = view.findViewById(R.id.oobeAppsLoadingBar)
        loading!!.showProgressBar()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val back: MaterialButton = view.findViewById(R.id.back)
        val next: MaterialButton = view.findViewById(R.id.next)
        val call = AppData.getAppData(fragmentContext!!).getAppDao()
        lifecycleScope.launch(Dispatchers.Default) {
            selectedItems = ArrayList()
            val appList = setUpApps(fragmentContext!!.packageManager)
            val mAdapter = AppAdapter(appList, fragmentContext!!.packageManager, fragmentContext!!)
            val lm = LinearLayoutManager(fragmentContext)
            activity?.runOnUiThread {
                recyclerView?.apply {
                    layoutManager = lm
                    adapter = mAdapter
                    OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
                }
            }
            runBlocking {
                activity?.runOnUiThread {
                    loading!!.hideProgressBar()
                    loading!!.visibility = View.GONE
                }
            }
        }
        back.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, AdFragment(), "oobe")
            }
        }
        next.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                var pos = 0
                for (i in selectedItems!!) {
                    val id = Random.nextLong(1000, 2000000)
                    val entity = AppEntity(pos, id, -1, 0,
                        isSelected = false,
                        tileSize = generateRandomTileSize(false),
                        appLabel = i.appLabel!!,
                        appPackage = i.appPackage!!
                    )
                    Log.d("OOBE", "insets item: ${i.appLabel} , pos: $pos")
                    call.insertItem(entity)
                    pos += 1
                }
                runBlocking {
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, AlmostDoneFragment(), "oobe")
                    }
                }
            }
        }
    }
    companion object {
        var selectedItems: MutableList<App>? = null
        var latestItem: Int? = null
    }
}
class AppAdapter(private var adapterApps: MutableList<App>, private val packageManager: PackageManager, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var iconSize = context.resources.getDimensionPixelSize(R.dimen.iconAppsListSize)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OOBEAppHolder(LayoutInflater.from(parent.context).inflate(R.layout.oobe_app_item, parent, false))
    }

    override fun getItemCount(): Int {
        return adapterApps.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = adapterApps[position]
        holder as OOBEAppHolder
        try {
            val bmp = recompressIcon(packageManager.getApplicationIcon(item.appPackage!!).toBitmap(iconSize, iconSize), 25)
            holder.icon.setImageIcon(bmp)
        } catch (e: PackageManager.NameNotFoundException) {
            holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_os_android))
            adapterApps.remove(item)
            notifyItemRemoved(position)
        }
        holder.label.text = item.appLabel
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            AppsFragment.latestItem = position
            if (isChecked) {
                item.selected = true
                if(!AppsFragment.selectedItems!!.contains(item)) {
                    AppsFragment.selectedItems!!.add(item)
                }
            } else {
                item.selected = false
                if(AppsFragment.selectedItems!!.contains(item)) {
                    AppsFragment.selectedItems!!.remove(item)
                }
            }
        }
        if (AppsFragment.latestItem != null) {
            holder.checkbox.isChecked = AppsFragment.latestItem == position || adapterApps[position].selected
        }
    }
}
class OOBEAppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: ImageView = itemView.findViewById(R.id.app_icon)
    val label: MaterialTextView = itemView.findViewById(R.id.app_label)
    val checkbox: MaterialCheckBox = itemView.findViewById(R.id.app_checkbox)
}