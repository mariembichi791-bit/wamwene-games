
package com.wamwene.games

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import android.content.pm.ResolveInfo

class MainActivity : Activity() {
    private val bg = Color.rgb(9, 12, 28)
    private val blue = Color.rgb(35, 120, 255)
    private val white = Color.WHITE
    private val prefs by lazy { getSharedPreferences("wamwene_library", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showHome()
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun launcherApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
    }

    private fun selectedPackages(): MutableSet<String> =
        prefs.getStringSet("selected_games", emptySet())?.toMutableSet() ?: mutableSetOf()

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 18, 22, 18)
            setBackgroundColor(bg)
        }
        root.addView(TextView(this).apply {
            text = "WAMWENE GAMES"
            textSize = 27f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "YOUR GAMING WORLD"
            textSize = 12f
            setTextColor(blue)
            setPadding(0, 5, 0, 15)
        })
        root.addView(TextView(this).apply {
            text = "+  ADD GAMES"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(16, 15, 16, 15)
            background = rounded(blue, 14)
            setOnClickListener { showAddGames() }
        })
        root.addView(TextView(this).apply {
            text = "MY GAME LIBRARY"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(0, 20, 0, 10)
        })
        val search = EditText(this).apply {
            hint = "Search my games..."
            setHintTextColor(Color.LTGRAY)
            setTextColor(white)
            textSize = 15f
            setSingleLine(true)
            setPadding(14, 10, 14, 10)
            background = rounded(Color.rgb(25, 31, 55), 14)
        }
        root.addView(search)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        fun refresh(query: String) {
            list.removeAllViews()
            val selected = selectedPackages()
            val games = launcherApps().filter {
                selected.contains(it.activityInfo.packageName) &&
                    it.loadLabel(packageManager).toString().contains(query, ignoreCase = true)
            }
            if (games.isEmpty()) {
                list.addView(TextView(this).apply {
                    text = if (selected.isEmpty()) "Your library is empty. Tap ADD GAMES to choose games from your phone." else "No games match your search."
                    textSize = 15f
                    setTextColor(Color.LTGRAY)
                    setPadding(8, 24, 8, 12)
                })
                return
            }
            games.forEach { info ->
                val pkg = info.activityInfo.packageName
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(12, 11, 12, 11)
                    background = rounded(Color.rgb(25, 31, 55), 15)
                }
                card.addView(ImageView(this).apply { setImageDrawable(info.loadIcon(packageManager)) }, LinearLayout.LayoutParams(46, 46))
                card.addView(TextView(this).apply {
                    text = info.loadLabel(packageManager)
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(white)
                    setPadding(13, 0, 8, 0)
                }, LinearLayout.LayoutParams(0, -2, 1f))
                card.addView(TextView(this).apply {
                    text = "PLAY"
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(white)
                    gravity = Gravity.CENTER
                    setPadding(15, 11, 15, 11)
                    background = rounded(blue, 11)
                    setOnClickListener {
                        packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
                            ?: Toast.makeText(this@MainActivity, "Cannot open this app", Toast.LENGTH_SHORT).show()
                    }
                })
                card.setOnLongClickListener {
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Remove game?")
                        .setMessage("Remove ${info.loadLabel(packageManager)} from your library? The app will remain installed.")
                        .setNegativeButton("CANCEL", null)
                        .setPositiveButton("REMOVE") { _, _ ->
                            val updated = selectedPackages().apply { remove(pkg) }
                            prefs.edit().putStringSet("selected_games", updated).apply()
                            refresh(search.text.toString())
                        }.show()
                    true
                }
                val params = LinearLayout.LayoutParams(-1, -2)
                params.bottomMargin = 10
                list.addView(card, params)
            }
        }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refresh(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        refresh("")
    }

    private fun showAddGames() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 18, 22, 18)
            setBackgroundColor(bg)
        }
        root.addView(TextView(this).apply {
            text = "ADD GAMES"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        })
        root.addView(TextView(this).apply {
            text = "Choose which installed apps belong in your game library."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 6, 0, 14)
        })
        val search = EditText(this).apply {
            hint = "Find an app..."
            setHintTextColor(Color.LTGRAY)
            setTextColor(white)
            setSingleLine(true)
            setPadding(14, 10, 14, 10)
            background = rounded(Color.rgb(25, 31, 55), 14)
        }
        root.addView(search)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(TextView(this).apply {
            text = "DONE"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(16, 14, 16, 14)
            background = rounded(blue, 14)
            setOnClickListener { showHome() }
        })

        fun refresh(query: String) {
            list.removeAllViews()
            val selected = selectedPackages()
            launcherApps().filter { it.loadLabel(packageManager).toString().contains(query, ignoreCase = true) }.forEach { info ->
                val pkg = info.activityInfo.packageName
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(10, 10, 10, 10)
                    background = rounded(Color.rgb(25, 31, 55), 13)
                }
                row.addView(ImageView(this).apply { setImageDrawable(info.loadIcon(packageManager)) }, LinearLayout.LayoutParams(42, 42))
                row.addView(TextView(this).apply {
                    text = info.loadLabel(packageManager)
                    textSize = 15f
                    setTextColor(white)
                    setPadding(12, 0, 8, 0)
                }, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(TextView(this).apply {
                    text = if (selected.contains(pkg)) "ADDED" else "ADD"
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setTextColor(white)
                    setPadding(14, 10, 14, 10)
                    background = rounded(if (selected.contains(pkg)) Color.DKGRAY else blue, 10)
                    setOnClickListener {
                        val updated = selectedPackages()
                        if (updated.contains(pkg)) updated.remove(pkg) else updated.add(pkg)
                        prefs.edit().putStringSet("selected_games", updated).apply()
                        refresh(search.text.toString())
                    }
                })
                val params = LinearLayout.LayoutParams(-1, -2)
                params.bottomMargin = 8
                list.addView(row, params)
            }
        }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refresh(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        setContentView(root)
        refresh("")
    }
}
