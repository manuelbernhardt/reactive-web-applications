package controllers

import models.Vocabulary
import play.api.i18n.Lang
import play.api.mvc._

object Import extends Controller {

  def importWord(sourceLanguage: Lang, word: String, targetLanguage: Lang, translation: String) = Action {
    val added = Vocabulary.addVocabulary(Vocabulary(sourceLanguage, targetLanguage, word, translation))
    if (added) Ok else Conflict
  }

}
