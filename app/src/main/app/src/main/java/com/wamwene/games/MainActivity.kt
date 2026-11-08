package com.wamwene.games

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.*
import android.view.Gravity
import android.view.View
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup

class MainActivity : Activity() {

    private lateinit var gameList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 30, 20, 20)
            setBackgroundColor(Color.rgb(15, 15, 30))
        }

        val title = TextView(this).apply {
            text = "WAMWENE GAMES"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(title)

        val addButton = Button(this).apply {
            text = "+"
            textSize = 25f
            setOnClickListener { addGame() }
        }

        root.addView(addButton)

        val scroll = HorizontalScrollView(this)
        gameList = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        scroll.addView(gameList)
        root.addView(scroll)

        setContentView(root)
    }

    private fun addGame() {
        val edit = EditText(this)
        edit.hint = "Enter game name"

        AlertDialogBuilder(edit)
    }

    private fun AlertDialogBuilder(edit: EditText) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Add a game")
            .setView(edit)
            .setPositiveButton("Add") { _, _ ->
                val name = edit.text.toString().trim()
                if (name.isNotEmpty()) {
                    val game = Button(this).apply {
                        text = name
                        textSize = 16f
                        setOnClickListener {
                            Toast.makeText(
                                this@MainActivity,
                                name,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    gameList.addView(game, LinearLayout.LayoutParams(
                        180,
                        150
                    ).apply {
                        setMargins(8, 10, 8, 10)
                    })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
