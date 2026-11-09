
package com.wamwene.games

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private val games = mutableListOf<Pair<String, String>>()
    private lateinit var gameRow: LinearLayout
    private val cyan = Color.rgb(0, 210, 255)

    private fun bg(color: Int, stroke: Int = cyan): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 22f
            setStroke(2, stroke)
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int = Color.WHITE
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER_VERTICAL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadGames()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 12)
            setBackgroundColor(Color.rgb(6, 9, 17))
        }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }

        val settings = Button(this).apply {
            text = "⚙"
            textSize = 20f
            setTextColor(cyan)
            setBackground(bg(Color.rgb(17, 30, 51)))
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Wamwene Settings")
                    .setMessage("WAMWENE GAMES\nDar-Link Engine")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        val heading = text("WAMWENE GAMES", 20f, cyan).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
        }

        val add = Button(this).apply {
            text = "+"
            textSize = 27f
            setTextColor(cyan)
            setBackground(bg(Color.rgb(17, 30, 51)))
            setOnClickListener { addGameDialog() }
        }

        top.addView(settings, LinearLayout.LayoutParams(52, 52))
        top.addView(heading, LinearLayout.LayoutParams(0, 55, 1f))
        top.addView(add, LinearLayout.LayoutParams(52, 52))
        root.addView(top)

        val subtitle = text("YOUR GAME LIBRARY", 13f, Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 18, 0, 8)
        root.addView(subtitle)

        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = false
        }

        gameRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        scroll.addView(gameRow)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        val engine = text("⚡  DAR-LINK ENGINE", 13f, cyan).apply {
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 8)
        }
        root.addView(engine)

        setContentView(root)
        renderGames()
    }

    private fun addGameDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 12, 24, 4)
        }

        val name = EditText(this).apply {
            hint = "Game name"
            setSingleLine()
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }

        val pkg = EditText(this).apply {
            hint = "Game package name"
            setSingleLine()
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }

        layout.addView(name)
        layout.addView(pkg)

        AlertDialog.Builder(this)
            .setTitle("＋ Add a Game")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val gameName = name.text.toString().trim()
                val packageName = pkg.text.toString().trim()

                if (gameName.isNotEmpty() &&
                    packageName.isNotEmpty()
                ) {
                    games.add(gameName to packageName)
                    saveGames()
                    renderGames()
                } else {
                    Toast.makeText(
                        this,
                        "Enter both fields",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun renderGames() {
        gameRow.removeAllViews()

        if (games.isEmpty()) {
            val empty = text(
                "Your game library is empty.\nTap + to add your first game.",
                16f,
                Color.LTGRAY
            ).apply {
                gravity = Gravity.CENTER
                setPadding(30, 30, 30, 30)
            }
            gameRow.addView(empty)
            return
        }

        games.forEachIndexed { index, game ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
                background = bg(Color.rgb(12, 27, 48))
            }

            val icon = text("🎮", 38f, cyan).apply {
                gravity = Gravity.CENTER
            }

            val title = text(game.first, 17f).apply {
                gravity = Gravity.CENTER
            }

            val start = Button(this).apply {
                text = "▶ START GAME"
                setTextColor(cyan)
                setBackground(bg(Color.rgb(13, 27, 50)))
                setOnClickListener {
                    val intent = packageManager
                        .getLaunchIntentForPackage(game.second)

                    if (intent != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Game not found or not accessible",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            val remove = TextView(this).apply {
                text = "Remove game"
                textSize = 12f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(8, 8, 8, 4)
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage("Remove ${game.first}?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Remove") { _, _ ->
                            games.removeAt(index)
                            saveGames()
                            renderGames()
                        }.show()
                }
            }

            card.addView(icon)
            card.addView(title)
            card.addView(start)
            card.addView(remove)

            val params = LinearLayout.LayoutParams(280, 210)
            params.setMargins(10, 10, 10, 10)
            gameRow.addView(card, params)
        }
    }

    private fun saveGames() {
        val array = JSONArray()
        games.forEach {
            array.put(JSONObject().apply {
                put("name", it.first)
                put("package", it.second)
            })
        }

        getSharedPreferences("wamwene", MODE_PRIVATE)
            .edit()
            .putString("games", array.toString())
            .apply()
    }

    private fun loadGames() {
        val saved = getSharedPreferences("wamwene", MODE_PRIVATE)
            .getString("games", "[]")

        try {
            val array = JSONArray(saved)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                games.add(
                    item.getString("name") to
                    item.getString("package")
                )
            }
        } catch (_: Exception) {
            games.clear()
        }
    }
}
