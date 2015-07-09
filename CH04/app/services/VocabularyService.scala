package services

import javax.inject.Singleton

import models.Vocabulary
import play.api.i18n.Lang

import scala.util.Random

@Singleton
class VocabularyService() {

  private var allVocabulary = List(
    Vocabulary(Lang("en"), Lang("fr"), "hello", "bonjour"),
    Vocabulary(Lang("en"), Lang("fr"), "play", "jouer")
  )

  def addVocabulary(v: Vocabulary): Boolean = {
    if (!allVocabulary.contains(v)) {
      allVocabulary = v :: allVocabulary
      true
    } else {
      false
    }
  }

  def findRandomVocabulary(sourceLanguage: Lang, targetLanguage: Lang): Option[Vocabulary] = {
    Random.shuffle(allVocabulary.filter { v =>
      v.sourceLanguage == sourceLanguage &&
        v.targetLanguage == targetLanguage
    }).headOption
  }

  def verify(sourceLanguage: Lang,
    word: String,
    targetLanguage: Lang,
    translation: String): Boolean = {
    allVocabulary.contains(
      Vocabulary(sourceLanguage, targetLanguage, word, translation)
    )
  }

}