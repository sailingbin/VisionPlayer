package com.live.visionplay.translation

/**
 * 翻译服务接口
 */
interface TranslationService {
    /**
     * 翻译文本
     * @param text 要翻译的文本
     * @param targetLanguage 目标语言代码
     * @param sourceLanguage 源语言代码（null表示自动检测）
     * @param callback 翻译结果回调
     */
    fun translate(
        text: String,
        targetLanguage: String,
        sourceLanguage: String? = null,
        callback: TranslationCallback
    )

    /**
     * 取消所有待处理的翻译任务
     */
    fun cancelAll()

    interface TranslationCallback {
        fun onSuccess(translatedText: String)
        fun onError(error: String)
    }
}
