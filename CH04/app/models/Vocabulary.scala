package models

import play.api.i18n.Lang

case class Vocabulary(sourceLanguage: Lang, targetLanguage: Lang, word: String, translation: String)