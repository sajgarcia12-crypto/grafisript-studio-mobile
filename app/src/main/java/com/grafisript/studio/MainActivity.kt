package com.grafisript.studio

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import android.text.InputType
import android.content.Intent
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry

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
        val run = Button(this).apply { text="▶ Ejecutar"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(112,42,220)) }
        val save = Button(this).apply { text="Guardar"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(42,42,65)) }
        val open = Button(this).apply { text="Abrir"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(42,42,65)) }
        bar.addView(run, LinearLayout.LayoutParams(0, -2, 1f)); bar.addView(save, LinearLayout.LayoutParams(0,-2,1f)); bar.addView(open,LinearLayout.LayoutParams(0,-2,1f)); root.addView(bar)
        val export = Button(this).apply { text="📦  Exportar proyecto para generar APK"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(65,38,115)) }
        root.addView(export, LinearLayout.LayoutParams(-1,-2).apply { topMargin=6 })
        editor = EditText(this).apply {
            setText(sample); setTextColor(Color.rgb(225,230,255)); setHintTextColor(Color.GRAY); setBackgroundColor(Color.rgb(20,18,32)); gravity=Gravity.TOP; textSize=15f; typeface=Typeface.MONOSPACE
            setPadding(12,12,12,12); inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS; minLines=10
        }
        root.addView(editor, LinearLayout.LayoutParams(-1,0,1f).apply { topMargin=12 })
        val consoleTitle = TextView(this).apply { text="Consola"; textSize=17f; typeface=Typeface.DEFAULT_BOLD; setTextColor(Color.rgb(190,150,255)); setPadding(4,12,4,4) }
        root.addView(consoleTitle)
        console = TextView(this).apply { text="Listo para ejecutar tu Grafisript."; textSize=14f; typeface=Typeface.MONOSPACE; setTextColor(Color.rgb(100,255,150)); setBackgroundColor(Color.rgb(5,7,12)); setPadding(12,12,12,12); minHeight=90 }
        root.addView(console)
        run.setOnClickListener { executeScript(editor.text.toString()) }
        save.setOnClickListener { saveProject() }
        open.setOnClickListener { loadProject() }
        export.setOnClickListener { exportAndroidProject() }
        setContentView(root)
    }

    private fun executeScript(code: String): String {
        val output = mutableListOf<String>()
        var background: String? = null
        var hp: String? = null
        var speed: String? = null
        code.lineSequence().forEach { raw ->
            val line=raw.trim()
            if (line.startsWith("mostrar ") || line.startsWith("mostrar_mensaje ")) output += line.substringAfter(' ').trim().trim('"')
            if (line.startsWith("fondo_color =")) background=line.substringAfter('=').trim()
            if (line.startsWith("vida =")) hp=line.substringAfter('=').trim()
            if (line.startsWith("velocidad =")) speed=line.substringAfter('=').trim()
        }
        val details=listOfNotNull(background?.let{"Fondo: $it"},hp?.let{"Vida: $it"},speed?.let{"Velocidad: $it"})
        if (output.isEmpty() && details.isEmpty()) output += "Script leído. Añade 'mostrar \"texto\"' para imprimir en consola."
        val result=(output + details + "\n✓ Script analizado (versión inicial)").joinToString("\n")
        console.text=result
        return result
    }

    private fun saveProject() {
        try { openFileOutput("proyecto.graf", MODE_PRIVATE).use { it.write(editor.text.toString().toByteArray()) }; console.text="✓ Proyecto guardado como proyecto.graf en almacenamiento privado." }
        catch (e: Exception) { console.text="Error al guardar: ${e.localizedMessage}" }
    }
    private fun loadProject() {
        try { editor.setText(openFileInput("proyecto.graf").bufferedReader().use { it.readText() }); console.text="✓ proyecto.graf abierto." }
        catch (_: Exception) { console.text="Todavía no hay proyecto guardado. Usa Guardar primero." }
    }

    private fun exportAndroidProject() {
        try {
            val result = executeScript(editor.text.toString())
            val projectName = "GrafisriptGeneratedApp"
            val zipFile = File(cacheDir, "$projectName.zip")
            ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                fun add(path: String, contents: String) {
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(contents.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                add("settings.gradle", "pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }\ndependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }\nrootProject.name='GrafisriptGeneratedApp'\ninclude ':app'\n")
                add("build.gradle", "plugins { id 'com.android.application' version '8.7.3' apply false }\n")
                add("gradle.properties", "org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8\nandroid.useAndroidX=true\n")
                add("app/build.gradle", "plugins { id 'com.android.application' }\nandroid { namespace 'com.generated.grafisript'; compileSdk 35; defaultConfig { applicationId 'com.generated.grafisript'; minSdk 23; targetSdk 35; versionCode 1; versionName '1.0' } }\n")
                add("app/src/main/AndroidManifest.xml", "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"><application android:label=\"Mi app Grafisript\" android:theme=\"@android:style/Theme.Material.Light.NoActionBar\"><activity android:name=\".MainActivity\" android:exported=\"true\"><intent-filter><action android:name=\"android.intent.action.MAIN\"/><category android:name=\"android.intent.category.LAUNCHER\"/></intent-filter></activity></application></manifest>\n")
                val escaped = result.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$")
                add("app/src/main/java/com/generated/grafisript/MainActivity.java", "package com.generated.grafisript;\nimport android.app.Activity; import android.os.Bundle; import android.widget.TextView;\npublic class MainActivity extends Activity { public void onCreate(Bundle b){super.onCreate(b); TextView t=new TextView(this); t.setText(\"$escaped\"); t.setTextSize(20); t.setPadding(24,24,24,24); setContentView(t); } }\n")
                add("README.md", "# Proyecto Android exportado desde Grafisript Studio\n\nEste ZIP contiene un proyecto Android básico. La pantalla de la app muestra el resultado que Grafisript Studio pudo analizar de tu script (prototipo).\n\n## Generar APK en GitHub Actions\n1. Crea un repositorio GitHub y sube todos los archivos de este ZIP.\n2. Abre Actions y habilita los workflows si GitHub te lo solicita.\n3. Ejecuta el workflow Build APK (o espera a que se active al subir a main).\n4. Descarga el artefacto APK desde el run terminado.\n")
                add(".github/workflows/build-apk.yml", "name: Build APK\non:\n  push:\n    branches: [main]\n  workflow_dispatch:\npermissions:\n  contents: read\njobs:\n  build:\n    runs-on: ubuntu-24.04\n    steps:\n      - uses: actions/checkout@v4\n      - uses: actions/setup-java@v4\n        with:\n          distribution: temurin\n          java-version: '17'\n      - name: Install Android SDK\n        run: sdkmanager --install 'platforms;android-35' 'build-tools;35.0.0' && yes | sdkmanager --licenses >/dev/null\n      - uses: gradle/actions/setup-gradle@v4\n      - run: gradle assembleDebug\n      - uses: actions/upload-artifact@v4\n        with:\n          name: APK-debug\n          path: app/build/outputs/apk/debug/app-debug.apk\n")
            }
            console.text="✓ ZIP del proyecto Android listo. Compila el APK en GitHub Actions; esta versión no compila APK dentro del teléfono."
            val share=Intent(Intent.ACTION_SEND).apply { type="application/zip"; putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(zipFile)); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            startActivity(Intent.createChooser(share, "Compartir proyecto Android ZIP"))
        } catch(e: Exception) { console.text="Exportación fallida: ${e.localizedMessage}" }
    }
}
