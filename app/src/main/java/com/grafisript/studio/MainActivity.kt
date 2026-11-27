package com.grafisript.studio

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {
    private lateinit var editor: EditText
    private lateinit var console: TextView
    private val sample = """Crear_mundo:
    fondo_color = azul

Crear_personaje:
    vida = 100
    velocidad = 5

evento_al_iniciar:
    mostrar "¡Hola desde Grafisript!"
fin"""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 10, 22)
        window.navigationBarColor = Color.rgb(12, 10, 22)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(12,10,22)); setPadding(16,12,16,12) }
        val header = TextView(this).apply { text = "✦  Grafisript Studio"; textSize=23f; setTextColor(Color.rgb(185,70,255)); typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER_VERTICAL; setPadding(4,10,4,12) }
        root.addView(header)
        val bar = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }
        val run = Button(this).apply { text="▶  Ejecutar"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(112,42,220)) }
        val save = Button(this).apply { text="Guardar"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(42,42,65)) }
        val open = Button(this).apply { text="Abrir"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(42,42,65)) }
        bar.addView(run, LinearLayout.LayoutParams(0, -2, 1f)); bar.addView(save, LinearLayout.LayoutParams(0,-2,1f)); bar.addView(open,LinearLayout.LayoutParams(0,-2,1f)); root.addView(bar)
        editor = EditText(this).apply {
            setText(sample); setTextColor(Color.rgb(225,230,255)); setHintTextColor(Color.GRAY); setBackgroundColor(Color.rgb(20,18,32)); gravity=Gravity.TOP; textSize=15f; typeface=Typeface.MONOSPACE
            setPadding(12,12,12,12); inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS; minLines=12
        }
        root.addView(editor, LinearLayout.LayoutParams(-1,0,1f).apply { topMargin=12 })
        val consoleTitle = TextView(this).apply { text="Consola"; textSize=17f; typeface=Typeface.DEFAULT_BOLD; setTextColor(Color.rgb(190,150,255)); setPadding(4,12,4,4) }
        root.addView(consoleTitle)
        console = TextView(this).apply { text="Listo para ejecutar tu Grafisript."; textSize=14f; typeface=Typeface.MONOSPACE; setTextColor(Color.rgb(100,255,150)); setBackgroundColor(Color.rgb(5,7,12)); setPadding(12,12,12,12); minHeight=90 }
        root.addView(console)
        run.setOnClickListener { executeScript(editor.text.toString()) }
        save.setOnClickListener { saveProject() }
        open.setOnClickListener { loadProject() }
        setContentView(root)
    }

    private fun executeScript(code: String) {
        val output = mutableListOf<String>()
        var background: String? = null
        var hp: String? = null
        var speed: String? = null
        code.lineSequence().forEach { raw ->
            val line=raw.trim()
            if (line.startsWith("mostrar ") || line.startsWith("mostrar_mensaje ")) {
                val value=line.substringAfter(' ').trim().trim('"')
                output += value
            }
            if (line.startsWith("fondo_color =")) background=line.substringAfter('=').trim()
            if (line.startsWith("vida =")) hp=line.substringAfter('=').trim()
            if (line.startsWith("velocidad =")) speed=line.substringAfter('=').trim()
        }
        val details=listOfNotNull(background?.let{"Fondo: $it"},hp?.let{"Vida: $it"},speed?.let{"Velocidad: $it"})
        if (output.isEmpty() && details.isEmpty()) output += "Script leído. Añade 'mostrar \"texto\"' para imprimir en consola."
        console.text=(output + details + "\n✓ Script analizado (versión inicial)").joinToString("\n")
    }

    private fun saveProject() {
        try { openFileOutput("proyecto.graf", MODE_PRIVATE).use { it.write(editor.text.toString().toByteArray()) }; console.text="✓ Proyecto guardado como proyecto.graf en almacenamiento privado." }
        catch (e: Exception) { console.text="Error al guardar: ${e.localizedMessage}" }
    }
    private fun loadProject() {
        try { editor.setText(openFileInput("proyecto.graf").bufferedReader().use { it.readText() }); console.text="✓ proyecto.graf abierto." }
        catch (_: Exception) { console.text="Todavía no hay proyecto guardado. Usa Guardar primero." }
    }
}
