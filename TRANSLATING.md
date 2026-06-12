# Translating NashiTimer

NashiTimer uses standard Android string resources. No Kotlin changes are needed to
add a translation.

## Add a language

1. Copy `app/src/main/res/values/strings.xml`.
2. Create the locale directory for the new language, such as `values-ko` or
   `values-fr`.
3. Save the translated file as `strings.xml` in that directory.
4. Keep every `name` unchanged and preserve placeholders such as `%1$d`.
5. Add the locale tag to `app/src/main/res/xml/locales_config.xml`.
6. Add the language option to `LanguageOption` in `SettingsScreen.kt`.
7. Run `.\gradlew.bat :app:compileDebugKotlin :app:testDebugUnitTest`.

The default English file is the source of truth. A translation should contain
the same set of string keys so that new and missing text is easy to review.
