package com.wamwene.games

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*

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

    private fun launcherApps() =
        packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
        ).filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }

    private fun selectedPackages(): MutableSet<String> =
        prefs.getStringSet("selected_games", emptySet())?.toMutableSet() ?: mutableSetOf()

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            setBackgroundColor(bg)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        titleBox.addView(TextView(this).apply {
            text = "WAMWENE GAMES"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        })
        titleBox.addView(TextView(this).apply {
            text = "YOUR GAMING WORLD"
            textSize = 11f
            setTextColor(blue)
            setPadding(0, 3, 0, 0)
        })
        header.addView(titleBox, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(this).apply {
            text = "+"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(white)
            background = rounded(blue, 15)
            setOnClickListener { showAddGames() }
        }, LinearLayout.LayoutParams(52, 48))
        root.addView(header)

        val search = EditText(this).apply {
            hint = "Search my games..."
            setHintTextColor(Color.LTGRAY)
            setTextColor(white)
            textSize = 14f
            setSingleLine(true)
            setPadding(14, 9, 14, 9)
            background = rounded(Color.rgb(25, 31, 55), 13)
        }
        val searchParams = LinearLayout.LayoutParams(-1, -2)
        searchParams.topMargin = 20
        root.addView(search, searchParams)

        root.addView(TextView(this).apply {
            text = "MY GAME LIBRARY  ·  SWIPE LEFT OR RIGHT"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(0, 22, 0, 12)
        })

        val horizontal = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = HorizontalScrollView.OVER_SCROLL_NEVER
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
        }
        horizontal.addView(row)
        root.addView(horizontal, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        fun refresh(query: String) {
            row.removeAllViews()
            val chosen = selectedPackages()
            val games = launcherApps().filter {
                chosen.contains(it.activityInfo.packageName) &&
                    it.loadLabel(packageManager).toString().contains(query, true)
            }
            if (games.isEmpty()) {
                row.addView(TextView(this).apply {
                    text = if (chosen.isEmpty()) "No games yet. Tap + to add your games." else "No games match your search."
                    textSize = 15f
                    setTextColor(Color.LTGRAY)
                    setPadding(8, 24, 8, 12)
                })
                return
            }
            games.forEach { info ->
                val pkg = info.activityInfo.packageName
                val tile = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(12, 14, 12, 12)
                    background = rounded(Color.rgb(25, 31, 55), 17)
                    setOnClickListener {
                        packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
                    }
                    setOnLongClickListener {
                        android.app.AlertDialog.Builder(this@MainActivity)
                            .setTitle("Remove game?")
                            .setMessage("Remove ${info.loadLabel(packageManager)} from your library? The game will stay installed.")
                            .setNegativeButton("CANCEL", null)
                            .setPositiveButton("REMOVE") { _, _ ->
                                val updated = selectedPackages().apply { remove(pkg) }
                                prefs.edit().putStringSet("selected_games", updated).apply()
                                refresh(search.text.toString())
                            }.show()
                        true
                    }
                }
                tile.addView(ImageView(this).apply {
                    setImageDrawable(info.loadIcon(packageManager))
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }, LinearLayout.LayoutParams(66, 66))
                tile.addView(TextView(this).apply {
                    text = info.loadLabel(packageManager)
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(white)
                    gravity = Gravity.CENTER
                    maxLines = 2
                    setPadding(0, 9, 0, 8)
                }, LinearLayout.LayoutParams(112, 48))
                tile.addView(TextView(this).apply {
                    text = "PLAY"
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setTextColor(white)
                    setPadding(18, 8, 18, 8)
                    background = rounded(blue, 10)
                    setOnClickListener {
                        packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
                    }
                })
                val params = LinearLayout.LayoutParams(138, -2)
                params.rightMargin = 12
                row.addView(tile, params)
            }
        }

        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refresh(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        refresh("")
    }

    private fun showAddGames() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            setBackgroundColor(bg)
        }
        root.addView(TextView(this).apply {
            text = "ADD GAMES"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        })
        root.addView(TextView(this).apply {
            text = "Choose only the games you want in your library."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 6, 0, 14)
        })
        val search = EditText(this).apply {
            hint = "Find a game..."
            setHintTextColor(Color.LTGRAY)
            setTextColor(white)
            setSingleLine(true)
            setPadding(14, 10, 14, 10)
            background = rounded(Color.rgb(25, 31, 55), 13)
        }
        root.addView(search)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
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
            launcherApps().filter {
                it.loadLabel(packageManager).toString().contains(query, true)
            }.forEach { info ->
                val pkg = info.activityInfo.packageName
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(10, 10, 10, 10)
                    background = rounded(Color.rgb(25, 31, 55), 13)
                }
                row.addView(ImageView(this).apply {
                    setImageDrawable(info.loadIcon(packageManager))
                }, LinearLayout.LayoutParams(42, 42))
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refresh(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        setContentView(root)
        refresh("")
    }
}
