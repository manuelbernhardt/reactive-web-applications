package models

import javax.inject.Singleton

import play.api.i18n.Lang

import scala.util.Random

case class Vocabulary(sourceLanguage: Lang, targetLanguage: Lang, word: String, translation: String)

