package com.wamwene.games

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.content.pm.PackageManager

class MainActivity : Activity() {

    private val bg = Color.rgb(9, 12, 28)
    private val blue = Color.rgb(35, 120, 255)
    private val purple = Color.rgb(125, 65, 220)
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showHome()
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 18, 24, 18)
            setBackgroundColor(bg)
        }

        val header = TextView(this).apply {
            text = "WAMWENE GAMES"
            textSize = 27f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(header)

        val subtitle = TextView(this).apply {
            text = "YOUR GAMING WORLD"
            textSize = 12f
            setTextColor(blue)
            setPadding(0, 5, 0, 15)
        }
        root.addView(subtitle)

        val search = EditText(this).apply {
            hint = "Search your games..."
            setHintTextColor(Color.LTGRAY)
            setTextColor(white)
            textSize = 15f
            setSingleLine(true)
            setPadding(15, 8, 15, 8)
            background = rounded(Color.rgb(25, 31, 55), 14)
        }
        root.addView(search)

        val heading = TextView(this).apply {
            text = "MY GAME LIBRARY"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(0, 18, 0, 10)
        }
        root.addView(heading)

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(
            -1, 0, 1f
        ))

        setContentView(root)

        search.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int,
                    count: Int, after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?, start: Int,
                    before: Int, count: Int
                ) {
                    loadGames(list, s.toString())
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )

        loadGames(list, "")
    }

    private fun rounded(color: Int, radius: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }

    private fun loadGames(list: LinearLayout, query: String) {
        list.removeAllViews()

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val games = packageManager
            .queryIntentActivities(intent, 0)
            .filter {
                it.activityInfo.packageName != packageName &&
                it.loadLabel(packageManager).toString()
                    .contains(query, ignoreCase = true)
            }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy {
                it.loadLabel(packageManager).toString()
            }

        if (games.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No games or apps found."
                textSize = 16f
                setTextColor(white)
                setPadding(10, 25, 10, 10)
            }
            list.addView(empty)
            return
        }

        for (game in games) {
            val packageName = game.activityInfo.packageName

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14, 12, 14, 12)
                background = rounded(
                    Color.rgb(25, 31, 55), 16
                )
            }

            val icon = ImageView(this).apply {
                setImageDrawable(
                    game.loadIcon(packageManager)
                )
            }

            card.addView(icon, LinearLayout.LayoutParams(
                48, 48
            ))

            val name = TextView(this).apply {
                text = game.loadLabel(packageManager)
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(white)
                setPadding(15, 0, 10, 0)
            }

            card.addView(name, LinearLayout.LayoutParams(
                0, -2, 1f
            ))

            val launch = TextView(this).apply {
                text = "PLAY"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(white)
                gravity = Gravity.CENTER
                setPadding(17, 11, 17, 11)
                background = rounded(blue, 12)
            }

            card.addView(launch)

            card.setOnClickListener {
                val open = packageManager
                    .getLaunchIntentForPackage(packageName)

                if (open != null) {
                    startActivity(open)
                } else {
                    Toast.makeText(
                        this,
                        "Cannot open this game",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val params = LinearLayout.LayoutParams(
                -1, -2
            )
            params.bottomMargin = 10
            list.addView(card, params)
        }
    }
}
