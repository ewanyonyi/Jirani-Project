package com.jirani.app.domain.agent

enum class JiraniLanguage {
    English,
    Swahili,
}

data class TranslationRequest(
    val text: String,
    val targetLanguage: JiraniLanguage,
)

data class TranslationResult(
    val translatedText: String,
    val note: String,
)

class TranslationAgent : JiraniAgent<TranslationRequest, TranslationResult> {
    override val name: String = "Translation Agent"

    override fun process(input: TranslationRequest): TranslationResult {
        val text = input.text.trim()
        if (text.isBlank()) {
            return TranslationResult(
                translatedText = "",
                note = "No text provided for translation.",
            )
        }

        return when (input.targetLanguage) {
            JiraniLanguage.English -> TranslationResult(
                translatedText = text,
                note = "English text retained for offline MVP use.",
            )
            JiraniLanguage.Swahili -> TranslationResult(
                translatedText = simpleSwahiliPhrase(text),
                note = "Offline MVP phrase support; full translation can be added later.",
            )
        }
    }

    private fun simpleSwahiliPhrase(text: String): String {
        val lower = text.lowercase()
        return when {
            "water" in lower || "well" in lower -> "Tunahitaji kuzungumza kwa amani kuhusu matumizi ya maji."
            "agreement" in lower -> "Tukubaliane kwa heshima na tuandike makubaliano."
            "safety" in lower || "threat" in lower -> "Tuthibitishe taarifa za usalama kabla ya kuzisambaza."
            else -> text
        }
    }
}
