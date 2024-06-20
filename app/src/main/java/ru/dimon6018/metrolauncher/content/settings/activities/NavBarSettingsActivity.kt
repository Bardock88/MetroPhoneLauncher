package ru.dimon6018.metrolauncher.content.settings.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.radiobutton.MaterialRadioButton
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class NavBarSettingsActivity: AppCompatActivity() {

    private var currentIcon: ImageView? = null
    private var radio: RadioGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.launcher_settings_navbar)
        radio = findViewById(R.id.navbarRadioGroup)
        val dark: MaterialRadioButton = findViewById(R.id.alwaysDark)
        val light: MaterialRadioButton = findViewById(R.id.alwaysLight)
        val accent: MaterialRadioButton = findViewById(R.id.byTheme)
        val hidden: MaterialRadioButton = findViewById(R.id.hidden)
        val auto: MaterialRadioButton = findViewById(R.id.auto)
        radio?.setOnCheckedChangeListener { _, checkedId ->
           when(checkedId) {
               dark.id -> {
                   PREFS!!.setNavBarSetting(0)
               }
               light.id -> {
                   PREFS!!.setNavBarSetting(1)
               }
               accent.id -> {
                   PREFS!!.setNavBarSetting(2)
              }
               hidden.id -> {
                   PREFS!!.setNavBarSetting(3)
               }
               auto.id -> {
                   PREFS!!.setNavBarSetting(4)
               }
           }
            PREFS!!.setPrefsChanged(true)
        }
        when(PREFS!!.navBarColor) {
            0 -> {
                dark.isChecked = true
                light.isChecked = false
                accent.isChecked = false
                hidden.isChecked = false
                auto.isChecked = false
            }
            1 -> {
                dark.isChecked = false
                light.isChecked = true
                accent.isChecked = false
                hidden.isChecked = false
                auto.isChecked = false
            }
            2 -> {
                dark.isChecked = false
                light.isChecked = false
                accent.isChecked = true
                hidden.isChecked = false
                auto.isChecked = false
            }
            3 -> {
                dark.isChecked = false
                light.isChecked = false
                accent.isChecked = false
                hidden.isChecked = true
                auto.isChecked = false
            }
            4 -> {
                dark.isChecked = false
                light.isChecked = false
                accent.isChecked = false
                hidden.isChecked = false
                auto.isChecked = true
            }
        }
        val coord: CoordinatorLayout = findViewById(R.id.coordinator)
        applyWindowInsets(coord)

        currentIcon = findViewById(R.id.currentStartIcon)
        updateCurrentIcon()

        val iconBtn: MaterialButton = findViewById(R.id.choose_start_icon_btn)
        iconBtn.setOnClickListener {
            val bottomSheet = BottomSheetDialog(this)
            bottomSheet.setContentView(R.layout.navbar_icon_choose)
            bottomSheet.dismissWithAnimation = false
            val bottomSheetInternal: View? = bottomSheet.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheetInternal!!).peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_size)
            val icon0 = bottomSheetInternal.findViewById<ImageView>(R.id.icon0)
            val icon1 = bottomSheetInternal.findViewById<ImageView>(R.id.icon1)
            val icon2 = bottomSheetInternal.findViewById<ImageView>(R.id.icon2)
            icon0.setOnClickListener {
                PREFS!!.setNavBarIcon(1)
                updateCurrentIcon()
                PREFS!!.setPrefsChanged(true)
                bottomSheet.dismiss()
            }
            icon1.setOnClickListener {
                PREFS!!.setNavBarIcon(0)
                updateCurrentIcon()
                PREFS!!.setPrefsChanged(true)
                bottomSheet.dismiss()
            }
            icon2.setOnClickListener {
                PREFS!!.setNavBarIcon(2)
                updateCurrentIcon()
                PREFS!!.setPrefsChanged(true)
                bottomSheet.dismiss()
            }
            bottomSheet.show()
        }

        val searchSwitch: MaterialSwitch = findViewById(R.id.searchBarSwitch)
        searchSwitch.isChecked = PREFS!!.isSearchBarEnabled
        searchSwitch.text = if(PREFS!!.isSearchBarEnabled) getString(R.string.on) else getString(R.string.off)
        searchSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setSearchBar(isChecked)
            searchSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            PREFS!!.setPrefsChanged(true)
        }
    }
    private fun updateCurrentIcon() {
        currentIcon?.setImageDrawable(when(PREFS!!.navBarIconValue) {
            0 -> {
                ContextCompat.getDrawable(this, R.drawable.ic_os_windows_8)
            }
            1 -> {
                ContextCompat.getDrawable(this, R.drawable.ic_os_windows)
            }
            2 -> {
                ContextCompat.getDrawable(this, R.drawable.ic_os_android)
            }
            else -> {
                ContextCompat.getDrawable(this, R.drawable.ic_os_windows_8)
            }
        })
    }
}