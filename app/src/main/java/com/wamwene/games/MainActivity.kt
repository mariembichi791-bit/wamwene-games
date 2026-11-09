package com.wamwene.games

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private lateinit var gameList: LinearLayout
    private val games = mutableSetOf<String>()
    private val prefs by lazy {
        getSharedPreferences("WamweneGames", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        games.addAll(prefs.getStringSet("games", emptySet()) ?: emptySet())

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(12, 15, 35))
        }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "WAMWENE GAMES"
            textSize = 23f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        top.addView(title, LinearLayout.LayoutParams(0, -2, 1f))

        val addButton = Button(this).apply {
            text = "+"
            textSize = 25f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(35, 110, 230))
            setOnClickListener { showGamePicker() }
        }

        top.addView(addButton)
        root.addView(top)

        val instruction = TextView(this).apply {
            text = "YOUR GAME LIBRARY  ← SWIPE →"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(0, 20, 0, 12)
        }
        root.addView(instruction)

        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        gameList = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        scroll.addView(gameList)
        root.addView(scroll)

        setContentView(root)
        renderGames()
    }

    private fun showGamePicker() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy {
                it.loadLabel(packageManager).toString().lowercase()
            }

        if (apps.isEmpty()) {
            Toast.makeText(this, "No games or apps found", Toast.LENGTH_SHORT).show()
            return
        }

        val available = apps.filter {
            !games.contains(it.activityInfo.packageName)
        }

        if (available.isEmpty()) {
            Toast.makeText(this, "All available apps are already added", Toast.LENGTH_SHORT).show()
            return
        }

        val names = available.map {
            it.loadLabel(packageManager).toString()
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Choose a game or app")
            .setItems(names) { _, which ->
                val selected = available[which].activityInfo.packageName
                games.add(selected)
                saveGames()
                renderGames()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveGames() {
        prefs.edit().putStringSet("games", games.toSet()).apply()
    }

    private fun renderGames() {
        gameList.removeAllViews()

        if (games.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No games added yet. Tap + to add games."
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(12, 20, 12, 20)
            }
            gameList.addView(empty)
        }

        games.toList().forEach { packageName ->
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val appIcon = packageManager.getApplicationIcon(appInfo)

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(14, 14, 14, 14)

                    background = GradientDrawable().apply {
                        setColor(Color.rgb(30, 38, 70))
                        cornerRadius = 22f
                    }
                }

                val icon = ImageView(this).apply {
                    setImageDrawable(appIcon)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }

                card.addView(icon, LinearLayout.LayoutParams(100, 100))

                val name = TextView(this).apply {
                    text = appName
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    maxLines = 2
                    setPadding(0, 10, 0, 10)
                }

                card.addView(name)

                val start = Button(this).apply {
                    text = "PLAY"
                    setOnClickListener {
                        val launch = packageManager.getLaunchIntentForPackage(packageName)
                        if (launch != null) {
                            startActivity(launch)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Cannot open this app",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                card.addView(start)

                val remove = TextView(this).apply {
                    text = "Remove"
                    textSize = 13f
                    setTextColor(Color.RED)
                    gravity = Gravity.CENTER
                    setPadding(8, 10, 8, 4)
                    setOnClickListener {
                        games.remove(packageName)
                        saveGames()
                        renderGames()
                    }
                }

                card.addView(remove)

                val params = LinearLayout.LayoutParams(170, -2)
                params.setMargins(0, 0, 14, 0)
                gameList.addView(card, params)

            } catch (_: Exception) {
                games.remove(packageName)
                saveGames()
            }
        }
    }
}
