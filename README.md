# Zufallszahl

Native Android-App: zieht eine Zufallszahl aus einem frei wählbaren Bereich und merkt sich die letzten zwölf Ergebnisse.

- Kotlin, keine WebView — echte Android-Oberfläche
- Hell- und Dunkelmodus folgen der Systemeinstellung
- Funktioniert vollständig offline, keine Berechtigungen, keine Netzwerkzugriffe
- Eingaben und Verlauf überleben einen Neustart

## APK bauen

Die APK entsteht automatisch per GitHub Actions bei jedem Push auf `main`.
Unter **Actions → APK bauen → letzter Lauf → Artifacts** liegt `zufallszahl-apk` zum Herunterladen.

Lokal mit installiertem Android-SDK:

```bash
./gradlew assembleDebug
# Ergebnis: app/build/outputs/apk/debug/app-debug.apk
```

## Technisches

| | |
|---|---|
| Mindestversion | Android 8.0 (API 26) |
| Zielversion | Android 14 (API 34) |
| Paketname | `de.drpi.zufallszahl` |

Die Zahlen stammen aus `kotlin.random.Random` — gut für Alltagszwecke, nicht für kryptografische Anwendungen.
